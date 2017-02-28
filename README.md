# ARTFUL
**A**n inte**R**active coun**T**ry re**F**erence freq**U**ency chorop**L**eth mapper

Shows graphically how countries mention entities of other countries

## Installation

The application can simply be run with maven:
```
cd server
mvn clean spring-boot:run -Dtranslation_database=src/main/resources/translations.sqlite
```
If you want to use the scripts to enhance the database you might have to install some dependencies
```
cd data-aggregation-tools/
sudo pip3 install -r requirements.txt 
```

## Datasource
- GeoJSON Data to colorize the countries is from <https://geojson-maps.kyd.com.au/>
- Word frequencies are from <http://corpora.uni-leipzig.de/de>


### Deployment

Tested with Tomcat 7

```
mvn package
cp artful.war /usr/share/tomcat/webapps
```

**Note**: The tomcat path may be different. 
