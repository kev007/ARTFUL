#!/usr/bin/python3

import configparser
import csv
import json
import sqlite3

config = configparser.ConfigParser()
config.read('config.conf')

connection = sqlite3.connect(config.get('database', 'file'))
cursor = connection.cursor()

corporaQuery = "SELECT DISTINCT corpus FROM freq;"
cursor.execute(corporaQuery)
corpora = [i[0] for i in (list(cursor.fetchall()))]

# Get country language mapping
country_codes_short = {}
country_codes_middle = {}
country_codes_long = {}
with open('country-code.csv', mode='r') as infile:
    reader = csv.reader(infile, delimiter=';')
    for rows in reader:
        country_codes_short.update({rows[1].lower(): rows[0].lower() for rows in reader})

with open('country-code.csv', mode='r') as infile:
    reader = csv.reader(infile, delimiter=';')
    for rows in reader:
        country_codes_middle.update({rows[2].lower(): rows[0].lower() for rows in reader})

with open('country-code.csv', mode='r') as infile:
    reader = csv.reader(infile, delimiter=';')
    for rows in reader:
        if len(rows) > 3 and rows[3] != '':
            country_codes_long.update({rows[3].lower(): rows[0].lower()})

mappings = {}
for corpus in corpora:
    if corpus not in mappings and corpus in country_codes_long:
        long_get_corpus_ = {corpus: country_codes_long.get(corpus)}
        mappings.update(long_get_corpus_)
    if corpus not in mappings and corpus in country_codes_middle:
        get_corpus_ = {corpus: country_codes_middle.get(corpus)}
        mappings.update(get_corpus_)
    if corpus not in mappings and corpus in country_codes_short:
        corpus_ = {corpus: country_codes_short.get(corpus)}
        mappings.update(corpus_)

inv_mapping = {v: k for k, v in mappings.items()}

language_country_properties = "# This file is generated, all changes will be overwritten!" \
                              " Change country-code.csv instead.\n"
for language in inv_mapping:
    language_country_properties += language.replace(' ', '\u0020') + "=" + inv_mapping[language] + "\n"

with open(config.get('server', 'language-country-mapping'), "w+") as result_file:
    result_file.write("var language_country_mapping = " + json.dumps(mappings) + ";")

with open(config.get('server', 'country-language-mapping'), "w+") as result_file:
    result_file.write("var country_language_mapping = " + json.dumps(inv_mapping) + ";")

with open("../server/src/main/resources/language-country-mapping.properties", "w+") as result_file:
    result_file.write(language_country_properties)
