#!/usr/bin/python
import urllib.request
import sqlite3
import json
from datetime import datetime,timedelta
# contiunously retrieve current data (recent 10 minutes), run by every 10 seconds
# connect to the database

with open("cameras.geojson.js") as f:
    d = json.load(f)
    insertvalues=[]
    for feature in d["features"]:
        # read image in feature["properties"]["SmallImage"]
        link = feature["properties"]["SmallImage"]
        camera = feature["id"]
        currentDT = datetime.now()
        path = str(camera) + "_" + str(currentDT)
        imagefile = urllib.request.urlopen(link)
        newvalues = (path,camera,feature["geometry"]["coordinates"][1],feature["geometry"]["coordinates"][0],currentDT,sqlite3.Binary(imagefile.read()),str(currentDT).split()[0],str(currentDT).split()[1][0:8])
        insertvalues.append(newvalues)
        
# store the images into sqlite
# print('begin conn:',datetime.now())
errorid=0
while True:
  try:
    conn = sqlite3.connect('currentimages/currentimages.db')
    print("start connection:")
    c = conn.cursor()
    c.execute('CREATE TABLE IF NOT EXISTS currentimages (path TEXT PRIMARY KEY, camera INT, latitude DOUBLE, longitude DOUBLE, time DATETIME, image BLOB,dateindex TEXT,timeindex TEXT)')
    for values in insertvalues:
      c.execute('insert into currentimages values (?,?,?,?,?,?,?,?)', values)
    moment = str(datetime.now() - timedelta(minutes=10))
    c.execute('delete from currentimages where time < ?',(moment,))
    conn.commit()
    break
  except sqlite3.OperationalError as e:
    print("connect error",errorid,e)
    errorid+=1
  finally:
    conn.close()
    print("end connection")
# print('end conn:',datetime.now())