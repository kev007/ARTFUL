#!/usr/bin/python3

import configparser
import sqlite3
import json

"""
Generate the average size of all corpora
Input: database entities: freq, corpora
Output: average-corpus-size-file (see config.conf)
"""

config = configparser.ConfigParser()
config.read('config.conf')
connection = sqlite3.connect(config.get('database', 'file'))
cursor = connection.cursor()

corporaQuery = "SELECT DISTINCT corpus FROM freq;"
cursor.execute(corporaQuery)
corpora = [i[0] for i in (list(cursor.fetchall()))]

year_query = "SELECT DISTINCT year FROM freq;"
cursor.execute(year_query)
years = [i[0] for i in (list(cursor.fetchall()))]

# Calculate the average corpora size for every corpus for every year
avgCorporaSizesPerYears = {"corpora": {}}
for corpus in corpora:
    located_in_query = "SELECT year, round(sum(size) * 1.0 / count(*)) FROM corpora WHERE lang = '{}' GROUP BY year" \
        .format(corpus)
    cursor.execute(located_in_query)
    results = cursor.fetchall()
    curr_corpus = {}
    for result in results:
        curr_corpus[result[0]] = int(result[1])
    if corpus not in avgCorporaSizesPerYears["corpora"]:
        avgCorporaSizesPerYears["corpora"][corpus] = {}
    avgCorporaSizesPerYears["corpora"][corpus]['average corpus size'] = curr_corpus

with open(config.get('server', 'average-corpus-size-file'), "w+") as result_file:
    result_file.write("var avgCorpusSizes = " + json.dumps(avgCorporaSizesPerYears) + ";")
