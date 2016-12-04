#!/usr/bin/python                                                               

import json

with open('low.geo.json') as json_data:
    user_input = input("Input the geoJSON properties you want to keep, separated by comma: ")
    properties_to_keep = user_input.split(',')
    load = json.load(json_data)
    cleaned_data = load
    features_ = load['features']
    featureCnt = 0
    for item in list(features_):
        featureCnt += 1
        propertyCnt = 0
        for _property in list(item['properties']):
            propertyCnt += 1
            if _property not in properties_to_keep:
                del item['properties'][_property]

    with open('cleaned_geojson.json', 'w') as outfile:
        json.dump(load, outfile)
