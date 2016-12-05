#!/usr/bin/python

import sqlite3

connection = sqlite3.connect("../database/translations.sqlite")
cursor = connection.cursor()
locatedInQuery = "SELECT DISTINCT located_in FROM translation;"
cursor.execute(locatedInQuery)
locatedIns = [i[0] for i in (list(cursor.fetchall()))]
yearQuery = "SELECT DISTINCT year FROM freq;"
cursor.execute(yearQuery)
years = [i[0] for i in (list(cursor.fetchall()))]
print("Deleting records from country_freq...")
deleteRecords = "DELETE FROM `country_freq`"
cursor.execute(deleteRecords)
connection.commit()
for locatedIn in locatedIns:
    for year in years:
        print("Inserting frequencies for " + locatedIn + "...")
        insert = "INSERT INTO `country_freq` SELECT located_in AS country, sum(freq) AS freq, f.year" \
                 " FROM freq f, translation t WHERE t.located_in = '{}' AND f.year = {};".format(locatedIn, year)
        print(insert)
        cursor.execute(insert)
        connection.commit()

connection.close()
