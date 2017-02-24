# ARTFUL
**A**n inte**R**active coun**T**ry re**F**erence freq**U**ency chorop**L**eth mapper

TODO: Write a project description

## Motivation

TODO: Write motivation

## Goals

TODO: Write goals

## Installation

TODO: Describe the installation process

```
sudo pip3 install -r data-aggregation-tools/requirements.txt 
```

## Datasource
- GeoJSON Data to colorize the countries is from <https://geojson-maps.kyd.com.au/>
- Word frequencies are from <http://corpora.uni-leipzig.de/de>

## Usage
**Note**: You have to specify the database path via *JAVA_OPTS*: 
```
-Dtranslation_database=/path/to/database/translations.sqlite
```

### Development
Start the server with:
```
cd server
mvn clean spring-boot:run
```


### Deployment

Tested with Tomcat 7

```
mvn package
cp artful.war /usr/share/tomcat/webapps
```

**Note**: The tomcat path may be different. 

## Credits

TODO: Write credits

## License

TODO: Write license
