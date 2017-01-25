#!/usr/bin/python3

import configparser
import csv
import json
import sqlite3

config = configparser.ConfigParser()
config.read('config.conf')

connection = sqlite3.connect(config['database']['file'])
cursor = connection.cursor()

corporaQuery = "SELECT DISTINCT corpus FROM freq;"
cursor.execute(corporaQuery)
corpora = [i[0] for i in (list(cursor.fetchall()))]

country_codes = {}
with open('country-code.csv', mode='r') as infile:
    reader = csv.reader(infile, delimiter=';')
    for rows in reader:
        country_codes.update({rows[1].lower(): rows[0].lower() for rows in reader})

with open('country-code.csv', mode='r') as infile:
    reader = csv.reader(infile, delimiter=';')
    for rows in reader:
        country_codes.update({rows[2].lower(): rows[0].lower() for rows in reader})

mappings = {}
for corpus in corpora:
    if '-' in corpus:
        split = corpus.split('-')
        mappings.update({corpus: country_codes.get(split[1])})
    else:
        mappings.update({corpus: country_codes.get(corpus)})

inv_mapping = {v: k for k, v in mappings.items()}
print(inv_mapping)

with open(config['server']['language-country-mapping'], "w+") as result_file:
    result_file.write("var language_country_mapping = " + json.dumps(mappings))

with open(config['server']['country-language-mapping'], "w+") as result_file:
    result_file.write("var country_language_mapping = " + json.dumps(inv_mapping))
