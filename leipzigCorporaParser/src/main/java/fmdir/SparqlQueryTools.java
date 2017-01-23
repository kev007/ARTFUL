package main.java.fmdir;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;

import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;

/**
 * 
 * @author Daniel Obraczka
 *
 *	This class handles everything sparql related, e.g. getting city names in different translations from dbpedia
 */
public class SparqlQueryTools {

	public static final String parRegex = "\\(([^)]+)\\)";
    public static final String DBPEDIA_ENDPOINT = "http://dbpedia.org/sparql";
    public static final String GET_ALL_CITIES_QUERY_HEAD = "PREFIX dbo: <http://dbpedia.org/ontology/>"
    												+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
    												+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
    												+ "PREFIX umbel: <http://umbel.org/umbel/rc/>"
    												+ "SELECT DISTINCT ?id ?citylabel ?country lang(?citylabel)";
//    												+ "SELECT DISTINCT ?city"
//    public static final String GET_ALL_CITIES_POP_PLACE = "WHERE { ?city rdf:type dbo:PopulatedPlace ; rdfs:label ?citylabel ; dbo:country ?locatedIn ; dbo:populationTotal  ?population ; dbo:wikiPageID ?id . ?locatedIn rdfs:label ?country . FILTER (?population >= 500000 ) FILTER (lang(?country)='en')}"; 

    //Gets all settlements fo dbpedia and uses paging to get all results
    public static final String GET_ALL_CITIES_SETTLEMENT_1 = "WHERE { ?city rdf:type dbo:Settlement ; rdfs:label ?citylabel ; dbo:country ?locatedIn ; dbo:populationTotal  ?population ; dbo:wikiPageID ?id . ?locatedIn rdfs:label ?country . FILTER (?population >= 500000 ) FILTER (lang(?country)='en')}ORDER BY ?city LIMIT 10000 OFFSET 0"; 
    public static final String GET_ALL_CITIES_SETTLEMENT_2 = "WHERE { ?city rdf:type dbo:Settlement ; rdfs:label ?citylabel ; dbo:country ?locatedIn ; dbo:populationTotal  ?population ; dbo:wikiPageID ?id . ?locatedIn rdfs:label ?country . FILTER (?population >= 500000 ) FILTER (lang(?country)='en')}ORDER BY ?city LIMIT 10000 OFFSET 10000"; 
    public static final String GET_ALL_CITIES_SETTLEMENT_3 = "WHERE { ?city rdf:type dbo:Settlement ; rdfs:label ?citylabel ; dbo:country ?locatedIn ; dbo:populationTotal  ?population ; dbo:wikiPageID ?id . ?locatedIn rdfs:label ?country . FILTER (?population >= 500000 ) FILTER (lang(?country)='en')}ORDER BY ?city LIMIT 10000 OFFSET 20000"; 
    public static final String GET_ALL_CITIES_CITY = "WHERE { ?city rdf:type dbo:City ; rdfs:label ?citylabel ; dbo:country ?locatedIn ; dbo:populationTotal  ?population ; dbo:wikiPageID ?id . ?locatedIn rdfs:label ?country . FILTER (?population >= 500000 ) FILTER (lang(?country)='en')}"; 
    public static final String GET_ALL_CITIES_TOWN = "WHERE { ?city rdf:type dbo:Town ; rdfs:label ?citylabel ; dbo:country ?locatedIn ; dbo:populationTotal  ?population ; dbo:wikiPageID ?id . ?locatedIn rdfs:label ?country . FILTER (?population >= 500000 ) FILTER (lang(?country)='en')}"; 
    public static final String GET_ALL_CITIES_UMBEL = "WHERE { ?city rdf:type umbel:City ; rdfs:label ?citylabel ; dbo:country ?locatedIn ; dbo:populationTotal  ?population ; dbo:wikiPageID ?id . ?locatedIn rdfs:label ?country . FILTER (?population >= 500000 ) FILTER (lang(?country)='en')}"; 
//    												+ "WHERE { ?city rdf:type dbo:City .}"; 
    
    
    public static final String GET_POLITICIANS_QUERY = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?id ?leadername ?countrylabel lang(?leadername) WHERE { ?country dbo:leader ?leader ; rdfs:label ?countrylabel . ?x dbo:country ?country . ?leader dbo:wikiPageID ?id ; rdfs:label ?leadername .  FILTER (lang(?countrylabel)='en')    }ORDER BY ?leadername";
    
    public static void main(String[] args){
    	getPoliticianData();
    }
    
    public static void getPoliticianData(){
    	String resultStr = "";
		int c = 0;
		HashSet<QuerySolution> finalRes = new HashSet<QuerySolution>();
		ResultSet results = querySelect(GET_POLITICIANS_QUERY, DBPEDIA_ENDPOINT, null, null);
		while(results.hasNext()){
			finalRes.add(results.next());
		}
		for(QuerySolution qs: finalRes){
			c++;
			String id = qs.getLiteral("?id").getString();
			String leadertmp = qs.getLiteral("?leadername").getString();
			//Clean city labels like "Wayanad (district)" by removing everything enclosed in brackets
			String leaderlabel = leadertmp.replaceAll(parRegex,"").trim();
			String countrytmp = qs.getLiteral("?countrylabel").getString();
			String country = countrytmp.replaceAll(parRegex, "").trim();
			String language = qs.getLiteral("?callret-3").getString();
			resultStr += id + "\t" + leaderlabel + "\t" + country + "\t" + language + "\n";
		}
		System.out.println("\n\n == " + c + " == \n\n");
//		System.out.println(resultStr);
    		try{
    			PrintWriter writer = new PrintWriter(new File("leipzigCorporaParser/resources/translations/politicians.csv"));
    			writer.write(resultStr);
    			writer.close();
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    }
    
    public static void getCityData(){
    	String resultStr = "";
		int c = 0;
		//Contains all results from settlements, towns, cities
		HashSet<QuerySolution> finalRes = new HashSet<QuerySolution>();
		ResultSet results = querySelect(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_SETTLEMENT_1, DBPEDIA_ENDPOINT, null, null);
		while(results.hasNext()){
			finalRes.add(results.next());
		}
		results = querySelect(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_SETTLEMENT_2, DBPEDIA_ENDPOINT, null, null);
		while(results.hasNext()){
			finalRes.add(results.next());
		}
		results = querySelect(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_SETTLEMENT_3, DBPEDIA_ENDPOINT, null, null);
		while(results.hasNext()){
			finalRes.add(results.next());
		}
		results = querySelect(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_CITY, DBPEDIA_ENDPOINT, null, null);
		while(results.hasNext()){
			finalRes.add(results.next());
		}
		results = querySelect(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_TOWN, DBPEDIA_ENDPOINT, null, null);
		while(results.hasNext()){
			finalRes.add(results.next());
		}
		results = querySelect(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_UMBEL, DBPEDIA_ENDPOINT, null, null);
		while(results.hasNext()){
			finalRes.add(results.next());
		}
		for(QuerySolution qs: finalRes){
			c++;
			String id = qs.getLiteral("?id").getString();
			String citytmp = qs.getLiteral("?citylabel").getString();
			//Clean city labels like "Wayanad (district)" by removing everything enclosed in brackets
			String citylabel = citytmp.replaceAll(parRegex,"").trim();
			String countrytmp = qs.getLiteral("?country").getString();
			String locatedIn = countrytmp.replaceAll(parRegex, "").trim();
			String language = qs.getLiteral("?callret-3").getString();
			resultStr += id + "\t" + citylabel + "\t" + locatedIn + "\t" + language + "\n";
		}
		System.out.println("\n\n == " + c + " == \n\n");
//		System.out.println(resultStr);
    		try{
    			PrintWriter writer = new PrintWriter(new File("leipzigCorporaParser/resources/translations/citiesExtendedCleaned.csv"));
    			writer.write(resultStr);
    			writer.close();
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    }

    /**
     * executes query
     * @param query query
     * @param endpoint endpoint
     * @param graph graph
     * @param model model
     * @return ResultSet
     */
    public static ResultSet querySelect(String query, String endpoint, String graph, Model model) {
        try {
            ResultSet results = queryExecution(query, graph, endpoint, model).execSelect();
            return results;
        } catch (RuntimeException e) {
            throw new RuntimeException("Error with query \"" + query + "\" at endpoint \"" + endpoint + "\" and graph \"" + graph + "\"", e);
        }
    }

	/**
     * creates a new object of QueryExecution
     * @param query query
     * @param graph graph
     * @param endpoint endpoint
     * @param model model
     * @return QueryExecution object
     */
    public static QueryExecution queryExecution(String query, String graph, String endpoint, Model model) {
        ARQ.setNormalMode();
        Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
        QueryExecution qexec;

        if (model == null) {
            if (graph != null) {
                qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery, graph);
            } 
            else {
                qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
            }
        } else {
            System.out.println("Query to Model...");
            qexec = QueryExecutionFactory.create(sparqlQuery, model);
        }

        return qexec;
    }
}
