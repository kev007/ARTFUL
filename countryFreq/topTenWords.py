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

cursor.execute("DELETE FROM top_ten_words")

query = "INSERT INTO top_ten_words SELECT sum(f.freq) total_freq, t.w_id, t.word, t.located_in, f.year " \
            "FROM freq f, translation t WHERE f.translation_id = t.id AND t.located_in = '{}' " \
            "AND year = {} GROUP BY t.w_id, t.located_in, f.year ORDER BY total_freq DESC LIMIT 10"
for locatedIn in locatedIns:
    for year in years:
        cursor.execute(query.format(locatedIn, year))
        connection.commit()
connection.close()
