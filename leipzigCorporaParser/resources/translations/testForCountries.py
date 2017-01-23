import sys
#checks if all countries are in the .csv and if not prints all that are missing
#csv must be seperated by tabs and country in the third column
def checkCountries():
    uniqueCountries = set()
    with open(sys.argv[1]) as f:
        content = f.readlines()
    for line in content:
        country = line.split('\t')[2]
        uniqueCountries.add(country)
    with open(sys.argv[2]) as f:
        countriesList = f.readlines()
    print("MISSING: ")
    for c in countriesList:
        if c.strip() not in uniqueCountries:
            print(c.strip())

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print("python testForCountries.py <*.csv> <reference>")
    else:
        checkCountries()
