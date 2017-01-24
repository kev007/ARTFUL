#!/usr/bin/python3

import collections
import configparser
import csv
import json
import sqlite3

config = configparser.ConfigParser()
config.read('config.conf')


class CaseInsensitiveDict(collections.Mapping):
    def __init__(self, d):
        self._d = d
        self._s = dict((k.lower(), k) for k in d)

    def __contains__(self, k):
        return k.lower() in self._s

    def __len__(self):
        return len(self._s)

    def __iter__(self):
        return iter(self._s)

    def __getitem__(self, k):
        return self._d[self._s[k.lower()]]

    def actual_key_case(self, k):
        return self._s.get(k.lower())


connection = sqlite3.connect(config['database']['file'])
cursor = connection.cursor()

corporaQuery = "SELECT DISTINCT corpus FROM freq;"
cursor.execute(corporaQuery)
corpora = [i[0] for i in (list(cursor.fetchall()))]

country_codes = {}
with open('country-code.csv', mode='r') as infile:
    reader = csv.reader(infile, delimiter=';')
    for rows in reader:
        country_codes.update({rows[1]: rows[0] for rows in reader})

with open('country-code.csv', mode='r') as infile:
    reader = csv.reader(infile, delimiter=';')
    for rows in reader:
        country_codes.update({rows[2]: rows[0] for rows in reader})

country_codes_case_insensitive = CaseInsensitiveDict(country_codes)
mappings = {}
for corpus in corpora:
    if '-' in corpus:
        split = corpus.split('-')
        mappings.update({corpus: country_codes_case_insensitive.get(split[1])})
    else:
        mappings.update({corpus: country_codes_case_insensitive.get(corpus)})
print(mappings)

with open(config['server']['language-mappings-file'], "w+") as result_file:
    result_file.write(json.dumps(mappings))
