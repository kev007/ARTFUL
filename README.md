# ARTFUL
**A**n inte**R**active coun**T**ry re**F**erence freq**U**ency chorop**L**eth mapper

A graphical representation of inter-country entity references in multilingual corpora in webapp form.

## Dependencies

* Java 8
* Maven
* Python 3
* Pip3

## Installation

The web application can simply be run with maven:
```
cd server
mvn clean install 
mvn spring-boot:run -Dtranslation_database=src/main/resources/translations.sqlite
```
If you want to use the included scripts to add new data to the database you need to install some dependencies
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
