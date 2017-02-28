#!/usr/bin/python

import configparser
import csv
import sqlite3

"""
Aggregate the references for each country to and from each country.
Outgoing references from country A to B are based on the corpus of country A with all references(entries in freq)
where located_in = country B.
Ingoing references from country A to B are based on the corpus of country B with all references(entries in freq)
where located_in = country B.

Input: database entities: freq, translation
Output: Inserts the generated values into the country_freq table.
"""

config = configparser.ConfigParser()
config.read('config.conf')

connection = sqlite3.connect(config.get('database', 'file'))

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

for locatedIn in locatedIns:
    print("Inserting frequencies for " + locatedIn + "...")
    if locatedIn.lower() not in inv_mapping:
        print("missing language code for: " + locatedIn)
    else:
        for year in years:
            avgIngoingCorporaSizeQuery = "SELECT round(sum(size) * 1.0 / count(*)) FROM corpora " \
                                         "WHERE id IN " \
                                         "(SELECT DISTINCT corporaID FROM freq f, translation t " \
                                         "WHERE f.translation_id = t.id AND t.located_in = '{}' AND f.year = {})" \
                .format(locatedIn, year)
            cursor.execute(avgIngoingCorporaSizeQuery)
            avgIngoingCorporaSize = cursor.fetchone()[0]
            lang = inv_mapping[locatedIn.lower()]
            avgOutgoingReferencesCorpusSizeQuery = "SELECT round(sum(size) * 1.0 / count(*)) FROM corpora " \
                                                   "WHERE lang = '{}' AND year = {}" \
                .format(lang, year)
            cursor.execute(avgOutgoingReferencesCorpusSizeQuery)
            avgOutgoingReferencesCorpusSize = cursor.fetchone()[0]
            if avgOutgoingReferencesCorpusSize is None:
                avgOutgoingReferencesCorpusSize = 0
            if avgIngoingCorporaSize is None:
                avgIngoingCorporaSize = 0
            corpus = inv_mapping[locatedIn.lower()]
            outgoingReferencesQuery = "SELECT sum(freq) AS freq_outgoing FROM freq f, translation t " \
                                      "WHERE f.translation_id = t.id AND f.corpus = '{}' AND f.year = {}" \
                .format(corpus, year)
            cursor.execute(outgoingReferencesQuery)
            outgoingReferences = cursor.fetchone()[0]
            if outgoingReferences is None:
                outgoingReferences = 0
            insert = "INSERT INTO `country_freq`(country, freq_outgoing, freq_ingoing, year, " \
                     "avg_corpora_size_ingoing, avg_corpora_size_outgoing)" \
                     " SELECT located_in AS country, {}, sum(freq) AS freq_outgoing, f.year, {}, {} " \
                     " FROM freq f, translation t" \
                     " WHERE f.translation_id = t.id AND t.located_in = '{}' AND f.year = {};" \
                .format(outgoingReferences, avgIngoingCorporaSize, avgOutgoingReferencesCorpusSize, locatedIn, year)
            cursor.execute(insert)
            connection.commit()
connection.close()
