#!/usr/bin/python3

import configparser
import json
import sqlite3

import numpy
from pandas import DataFrame


def get_located_ins(c):
    located_in_query = "SELECT DISTINCT located_in FROM translation;"
    c.execute(located_in_query)
    return [i[0] for i in (list(c.fetchall()))]


def get_years(c):
    year_query = "SELECT DISTINCT year FROM freq;"
    c.execute(year_query)
    return [i[0] for i in (list(c.fetchall()))]


config = configparser.ConfigParser()
config.read('config.conf')
connection = sqlite3.connect(config.get('database', 'file'))
connection_in_memory = sqlite3.connect(':memory:')
query = "".join(line for line in connection.iterdump())
connection_in_memory.executescript(query)
cursor = connection_in_memory.cursor()

locatedIns = get_located_ins(cursor)
years = get_years(cursor)

corporaQuery = "SELECT DISTINCT corpus FROM freq;"
cursor.execute(corporaQuery)
corpora = [i[0] for i in (list(cursor.fetchall()))]

dataframes = []
for i in years:
    year_dataframe_pair = [i, DataFrame(0, index=locatedIns, columns=corpora)]
    dataframes.append(year_dataframe_pair)

# Calculate the average corpora size for every corpus for every year
avgCorporaSizesPerYears = {"corpora": {}}
max_corpora_size = 0
for corpus in corpora:
    located_in_query = "SELECT year, round(sum(size) * 1.0 / count(*)) FROM corpora WHERE lang = '{}' GROUP BY year" \
        .format(corpus)
    cursor.execute(located_in_query)
    results = cursor.fetchall()
    curr_corpus = {}
    for result in results:
        curr_corpus_size = result[1]
        curr_corpus[result[0]] = curr_corpus_size
        if curr_corpus_size > max_corpora_size:
            max_corpora_size = curr_corpus_size
    avgCorporaSizesPerYears["corpora"][corpus] = curr_corpus

# Calculate corpus size multiplier
corpora_size_multiplier = {"corpora": {}}
for year in years:
    for corpus in corpora:
        if year in avgCorporaSizesPerYears['corpora'][corpus]:
            curr_corpus_avg_size = avgCorporaSizesPerYears['corpora'][corpus][year]
            if year not in corpora_size_multiplier['corpora']:
                corpora_size_multiplier['corpora'][year] = {}
            corpora_size_multiplier['corpora'][year][corpus] = max_corpora_size / curr_corpus_avg_size
        else:
            print(str(year) + "; " + corpus + " not found")

query = "SELECT sum(f.freq) total_freq, year FROM freq f, translation t WHERE f.translation_id = t.id " \
        "AND t.located_in = '{}' AND f.corpus = '{}' GROUP BY t.located_in, year, corpus;"
for locatedIn in locatedIns:
    print("Generating reference values for " + locatedIn)
    for corpus in corpora:
        cursor.execute(query.format(locatedIn, corpus))
        connection.commit()
        result = cursor.fetchall()
        for entry in result:
            year = entry[1]
            value = entry[0] * float(corpora_size_multiplier['corpora'][year][corpus])
            for dataframe in dataframes:
                if dataframe[0] == year:
                    dataframe[1].set_value(locatedIn, corpus, value)

connection.close()
columns = dataframes[0][1].axes[1].tolist()
uncollected_year_country_list = []

for i in range(len(dataframes)):
    # For every year (dataframe/matrix)
    df = dataframes[i]
    rows = df[1].axes[0].tolist()
    curr_year = df[0]
    for index, row in df[1].iterrows():
        # For every country (row)
        country = index
        values = list(row.values)
        uncollected_year_country_list.append({str(curr_year): {country: values}})

# Add the column headings (corpora) to the json object
countries_collected_by_year = {"languages": columns}

# Collect all countries by year
for entry in uncollected_year_country_list:
    for key, value in entry.items():
        if str(key) in countries_collected_by_year.keys():
            for country, values in value.items():
                countries_collected_by_year[str(key)][country] = values
        else:
            countries_collected_by_year[str(key)] = value


class NumPyEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, numpy.integer):
            return int(obj)
        elif isinstance(obj, numpy.floating):
            return float(obj)
        elif isinstance(obj, numpy.ndarray):
            return obj.tolist()
        else:
            return super(NumPyEncoder, self).default(obj)


with open(config.get('server', 'country-references-file'), "w+") as result_file:
    result_file.write("var countryReferences = " + json.dumps(countries_collected_by_year, cls=NumPyEncoder) + ";")
