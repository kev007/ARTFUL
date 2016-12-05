#!/usr/bin/python

import sqlite3

connection = sqlite3.connect("../database/translations.sqlite")
cursor = connection.cursor()
sql = "SELECT DISTINCT located_in FROM translation;"
cursor.execute(sql)
results = [i[0] for i in (list(cursor.fetchall()))]
print("Deleting records from country_freq...")
deleteRecords = "DELETE FROM `country_freq`"
cursor.execute(deleteRecords)
connection.commit()
for result in results:
    print("Inserting frequencies for " + result + "...")
    insert = "INSERT INTO `country_freq` SELECT located_in AS country, sum(freq) AS freq" \
             " FROM freq f, translation t WHERE t.located_in = '" + result + "';"
    cursor.execute(insert)
    connection.commit()

connection.close()
