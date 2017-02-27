package main.java.fmdir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.annotation.ThreadSafe;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
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
	public static final String queriesBasePath = "resources/SPARQLQueries/";
	public static final String translationsBasePath = "resources/translations/";
    
    public static void main(String[] args){
    	if(args.length != 2){
    		System.err.println("Usage: SparqlQueryTools <queriesFile> <output.csv>\n ");
    	}
    	try {
			List<String> queries = getQueriesFromFile(new File(queriesBasePath + args[0]));
			String resultStr = getResults(queries);
			writeToFile(new File(translationsBasePath + args[1]), resultStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static String getResults(List<String> queries){
    	String resultStr = "";
		int c = 0;
		HashSet<QuerySolution> finalRes = executeAndIntegrateQueries(queries, DBPEDIA_ENDPOINT, null, null);
		System.out.println("Parsing results to .csv format");
		for(QuerySolution qs: finalRes){
			c++;
			String id = qs.getLiteral("?id").getString();
			String citytmp = qs.getLiteral("?entitylabel").getString();
			//Clean abels like "Wayanad (district)" by removing everything enclosed in brackets
			String citylabel = citytmp.replaceAll(parRegex,"").trim();
			String countrytmp = qs.getLiteral("?country").getString();
			String locatedIn = countrytmp.replaceAll(parRegex, "").trim();
			String language = qs.getLiteral("?callret-3").getString();
			resultStr += id + "\t" + citylabel + "\t" + locatedIn + "\t" + language + "\n";
		}
		System.out.println("\n\n == Final Result: " + c + " lines == \n\n");
		return resultStr;
    }
    
    public static List<String> getQueriesFromFile(File queriesFile) throws IOException{
        InputStream fis = new FileInputStream(queriesFile);
        InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line, currentQuery = "";
       	List<String> queries = new ArrayList<String>();
        while((line = br.readLine()) != null ){
        	//if line is empty a new query begins
        	if(line.trim().isEmpty()){
        		if(!currentQuery.toUpperCase().contains("SELECT DISTINCT ?ID ?ENTITYLABEL ?COUNTRY LANG(?ENTITYLABEL)")){
        			System.err.println("=== FATAL ERROR === \nQuery has to contain the select statement:\n SELECT DISTINCT ?ID ?ENTITYLABEL ?COUNTRY LANG(?ENTITYLABEL)");
        			System.exit(0);
        		}
        		queries.add(currentQuery);
        		currentQuery = "";
        	}
        	currentQuery += "\n" + line;
        }
        fis.close();
        isr.close();
        br.close();
    	return queries;
    }
    
    public static void writeToFile(File outputFile, String resultStr) throws FileNotFoundException{
    	System.out.println("Writing results to "+outputFile.getAbsolutePath());
    	PrintWriter writer = new PrintWriter(outputFile);
    	writer.write(resultStr);
    	writer.close();
    	System.out.println("Finished successfully");
    }
    
//    public static void getPoliticianData(){
//    	String resultStr = "";
//		int c = 0;
//		HashSet<QuerySolution> finalRes = new HashSet<QuerySolution>();
//		ResultSet results = querySelect(GET_POLITICIANS_QUERY, DBPEDIA_ENDPOINT, null, null);
//		while(results.hasNext()){
//			finalRes.add(results.next());
//		}
//		for(QuerySolution qs: finalRes){
//			c++;
//			String id = qs.getLiteral("?id").getString();
//			String leadertmp = qs.getLiteral("?leadername").getString();
//			//Clean city labels like "Wayanad (district)" by removing everything enclosed in brackets
//			String leaderlabel = leadertmp.replaceAll(parRegex,"").trim();
//			String countrytmp = qs.getLiteral("?countrylabel").getString();
//			String country = countrytmp.replaceAll(parRegex, "").trim();
//			String language = qs.getLiteral("?callret-3").getString();
//			resultStr += id + "\t" + leaderlabel + "\t" + country + "\t" + language + "\n";
//		}
//		System.out.println("\n\n == " + c + " == \n\n");
////		System.out.println(resultStr);
//    		try{
//    			PrintWriter writer = new PrintWriter(new File("leipzigCorporaParser/resources/translations/politicians.csv"));
//    			writer.write(resultStr);
//    			writer.close();
//    		}catch(Exception e){
//    			e.printStackTrace();
//    		}
//    }
//    
//    public static void getCityData(){
//    	String resultStr = "";
//		int c = 0;
//		List<String> cityQueries = new ArrayList<String>();
//		cityQueries.add(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_SETTLEMENT_1);
//		cityQueries.add(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_SETTLEMENT_2);
//		cityQueries.add(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_SETTLEMENT_3);
//		cityQueries.add(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_CITY);
//		cityQueries.add(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_TOWN);
//		cityQueries.add(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_UMBEL);
//		cityQueries.add(GET_ALL_CITIES_QUERY_HEAD + GET_ALL_CITIES_CITY_OF);
//		HashSet<QuerySolution> finalRes = executeAndIntegrateQueries(cityQueries, DBPEDIA_ENDPOINT, null, null);
//		for(QuerySolution qs: finalRes){
//			c++;
//			String id = qs.getLiteral("?id").getString();
//			String citytmp = qs.getLiteral("?citylabel").getString();
//			//Clean city labels like "Wayanad (district)" by removing everything enclosed in brackets
//			String citylabel = citytmp.replaceAll(parRegex,"").trim();
//			String countrytmp = qs.getLiteral("?country").getString();
//			String locatedIn = countrytmp.replaceAll(parRegex, "").trim();
//			String language = qs.getLiteral("?callret-3").getString();
//			resultStr += id + "\t" + citylabel + "\t" + locatedIn + "\t" + language + "\n";
//		}
//		System.out.println("\n\n == " + c + " == \n\n");
////		System.out.println(resultStr);
//    		try{
//    			PrintWriter writer = new PrintWriter(new File("leipzigCorporaParser/resources/translations/cities.csv"));
//    			writer.write(resultStr);
//    			writer.close();
//    		}catch(Exception e){
//    			e.printStackTrace();
//    		}
//    }
    
    public static HashSet<QuerySolution> executeAndIntegrateQueries(List<String> queries, String endpoint, String graph, Model model){
		HashSet<QuerySolution> finalRes = new HashSet<QuerySolution>();
    	for(String q : queries){
    		System.out.println("Running query: " + q);
            ResultSet results = querySelect(q, endpoint, graph, model);
            int count = 0;
            while(results.hasNext()){
            	count ++;
                finalRes.add(results.next());
            }
            System.out.println("Retrieved " + count + "results");
    	}
    	
    	return finalRes;
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
