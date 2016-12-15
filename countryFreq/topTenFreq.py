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
#print("Deleting records from topten")
#deleteRecordsTopTen = "DELETE FROM `topten`"
#cursor.execute(deleteRecordsTopTen)
#print("Deleting records from toptenResult")
#deleteRecordsTopTenResults = "DELETE FROM `toptenResult`"
#cursor.execute(deleteRecordsTopTenResults)
#connection.commit()

#topTenID = 0

topTenID = 754

for locatedIn in locatedIns:
    if(locatedIn != "Germany" and locatedIn != "Nigeria" and locatedIn != "United Kingdom" and locatedIn != "United States"):
        for beginYear in years:
            for endYear in years:
                if(beginYear <= endYear):
                    print("inserting for : " + locatedIn + "  " + str(beginYear) + "-" + str(endYear)) 
                    insertTopTen = "INSERT INTO 'topten'(id, country, beginYear, endYear) VALUES ('{}','{}','{}','{}');".format(topTenID,locatedIn,beginYear,endYear)
                    print(insertTopTen)
                    cursor.execute(insertTopTen)
                    getFreqs = "SELECT f.corpus, SUM(f.freq) AS freqpercorp FROM freq f, translation t WHERE t.located_in = '{}' AND f.year BETWEEN '{}' AND '{}' GROUP BY f.corpus ORDER BY freqpercorp DESC LIMIT 10".format(locatedIn, beginYear, endYear)
                    cursor.execute(getFreqs)
                    position = 1
                    for row in cursor.fetchall():
                        insertTopResults = "INSERT INTO 'toptenResult'(topten_id, country, freq, position) VALUES('{}','{}','{}','{}');".format(topTenID, row[0], row[1], position)
                        print(insertTopResults)
                        cursor.execute(insertTopResults)
                        position = position + 1
                    topTenID = topTenID + 1
                    connection.commit()
connection.close()
