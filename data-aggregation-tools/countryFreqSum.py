#!/usr/bin/python

import configparser
import sqlite3

config = configparser.ConfigParser()
config.read('config.conf')

connection = sqlite3.connect(config['database']['file'])

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
    print("Inserting frequencies for " + locatedIn + "...")
    for year in years:
        insert = "INSERT INTO `country_freq`(country, freq, year)" \
                 " SELECT located_in AS country, sum(freq) AS freq, f.year" \
                 " FROM freq f, translation t" \
                 " WHERE f.translation_id = t.id AND t.located_in = '{}' AND f.year = {};".format(locatedIn, year)
        cursor.execute(insert)
        connection.commit()

connection.close()
