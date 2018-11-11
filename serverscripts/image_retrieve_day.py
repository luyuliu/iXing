#!/usr/bin/python
import urllib.request
import sqlite3
import json
from datetime import datetime,timedelta
# contiunously retrieve daily data, run by every 5 minutes, daytime data only (7:00-20:00)
# connect to the database

with open("cameras.geojson.js") as f:
    d = json.load(f)
    insertvalues = []
    for feature in d["features"]:
        # read image in feature["properties"]["SmallImage"]
        link = feature["properties"]["SmallImage"]
        camera = feature["id"]
        currentDT = datetime.now()
        path = str(camera) + "_" + str(currentDT)
        imagefile = urllib.request.urlopen(link)        
        
        newvalues = (path,camera,feature["geometry"]["coordinates"][1],feature["geometry"]["coordinates"][0],currentDT,sqlite3.Binary(imagefile.read()),str(currentDT).split()[0],str(currentDT).split()[1][0:6]+"00")
        insertvalues.append(newvalues)
                  
        # read the file 
        # file = c.execute('select image  from dayimages where camera=1').fetchone()

# store the image into sqlite
conn = sqlite3.connect('historicimages/dayimages.db')
c = conn.cursor()
c.execute('CREATE TABLE IF NOT EXISTS dayimages (path TEXT PRIMARY KEY, camera INT, latitude DOUBLE, longitude DOUBLE, time DATETIME, image BLOB, dateindex TEXT, timeindex TEXT)')
for values in insertvalues:
  c.execute('insert into dayimages values (?,?,?,?,?,?,?,?)', values)

timedayago = str(datetime.now() - timedelta(days=1))
c.execute('delete from dayimages where time < ?',(timedayago,))
  
conn.commit()
conn.close()
