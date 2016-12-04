#!/usr/bin/python                                                               

import json

with open('low.geo.json') as json_data:
    load = json.load(json_data)
    cleaned_data = load
    features_ = load['features']
    featureCnt = 0
    for item in list(features_):
        featureCnt += 1
        propertyCnt = 0
        for _property in list(item['properties']):
            propertyCnt += 1
            if _property != 'name':
                del item['properties'][_property]

                #propertyToDelete = cleaned_data['features'][featureCnt]['properties']
                #print(propertyToDelete)

    print(load)
    with open('cleaned_geojson.json', 'w') as outfile:
        json.dump(load, outfile)


#with open('custom.geo.json') as json_data:
#    data = json.load(json_data)
#    clean_data = [item for item in data['features'] if not item['name']]
