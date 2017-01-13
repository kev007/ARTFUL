#!/usr/bin/python3

import sqlite3

from pandas import DataFrame

connection = sqlite3.connect("../database/translations.sqlite")
cursor = connection.cursor()

locatedInQuery = "SELECT DISTINCT located_in FROM translation;"
cursor.execute(locatedInQuery)
locatedIns = [i[0] for i in (list(cursor.fetchall()))]

yearQuery = "SELECT DISTINCT year FROM freq;"
cursor.execute(yearQuery)
years = [i[0] for i in (list(cursor.fetchall()))]

corporaQuery = "SELECT DISTINCT corpus FROM freq;"
cursor.execute(corporaQuery)
corpora = [i[0] for i in (list(cursor.fetchall()))]

dataframes = []
for i in years:
    year_dataframe_pair = [i, DataFrame(0, index=locatedIns, columns=corpora)]
    dataframes.append(year_dataframe_pair)

query = "SELECT sum(f.freq) total_freq, year FROM freq f, translation t WHERE f.translation_id = t.id " \
        "AND t.located_in = '{}' AND f.corpus = '{}' GROUP BY t.located_in, year, corpus;"
for locatedIn in locatedIns:
    print("Generating reference values for " + locatedIn)
    for corpus in corpora:
        cursor.execute(query.format(locatedIn, corpus))
        connection.commit()
        result = cursor.fetchall()
        for entry in result:
            value = entry[0]
            year = entry[1]

            for dataframe in dataframes:
                if dataframe[0] == year:
                    dataframe[1].set_value(locatedIn, corpus, value)
                    break

connection.close()

for df in dataframes:
    with open("results.json", "a") as result_file:
        result_file.write("Year: " + str(df[0]) + "\n")
        result_file.write(df[1].to_json())
        result_file.write("\n\n\n")
