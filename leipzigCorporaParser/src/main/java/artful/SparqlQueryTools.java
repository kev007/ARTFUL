package main.java.artful;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
 *         This class handles everything sparql related, e.g. getting city names
 *         in different translations from dbpedia
 * 
 */
public class SparqlQueryTools {
	private static FileInputStream fis;
	private static InputStreamReader isr;
	private static BufferedReader br;
	

	/**
	 * Used to delete strings inside brackets
	 */
	public static final String parRegex = "\\(([^)]+)\\)";
	public static final String DBPEDIA_ENDPOINT = "http://dbpedia.org/sparql";
	public static final String queriesBasePath = "resources/SPARQLQueries/";
	public static final String translationsBasePath = "resources/translations/";

	/**
	 * Reads queries seperated by empty lines from arg[0], queries dbpedia,
	 * combines the results of all queries into a set and writes the result into
	 * args[1] as tabseperated csv
	 * 
	 * @param args
	 *            queriesFile located in resources/SPARQLQueries, output.csv to
	 *            be written to resources/translation
	 */
	public static void main(String[] args) {

		try {
			for (Path p : FileTools.getAllPathsFrom(queriesBasePath)) {
				System.out.println(p);
				List<String> queries = getQueriesFromFile(p.toFile());
				String resultStr = getResults(queries);
				writeToFile(new File(translationsBasePath + p.getFileName() + ".csv"), resultStr);
			}
		fis.close();
		isr.close();
		br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Executes and integrates all queries and returns the results as
	 * tabseperated string
	 * 
	 * @param queries
	 *            List of SPARQL queries
	 * @return resultStr tabseperated result string
	 */
	public static String getResults(List<String> queries) {
		String resultStr = "";
		int c = 0;
		HashSet<QuerySolution> finalRes = executeAndIntegrateQueries(queries, DBPEDIA_ENDPOINT, null, null);
		System.out.println("Parsing results to .csv format");
		for (QuerySolution qs : finalRes) {
			c++;
			String id = qs.getLiteral("?id").getString();
			String citytmp = qs.getLiteral("?entitylabel").getString();
			// Clean abels like "Wayanad (district)" by removing everything
			// enclosed in brackets
			String citylabel = citytmp.replaceAll(parRegex, "").trim();
			String countrytmp = qs.getLiteral("?country").getString();
			String locatedIn = countrytmp.replaceAll(parRegex, "").trim();
			String language = qs.getLiteral("?callret-3").getString();
			resultStr += id + "\t" + citylabel + "\t" + locatedIn + "\t" + language + "\n";
		}
		System.out.println("\n\n == Final Result: " + c + " lines == \n\n");
		return resultStr;
	}

	/**
	 * Gets SPARQL queries that are seperated by empty lines from the given
	 * file. The select statement in each query has to be SELECT DISTINCT ?ID
	 * ?ENTITYLABEL ?COUNTRY LANG(?ENTITYLABEL) else an error will be thrown
	 * 
	 * @param queriesFile
	 *            File containing SPARQL queries seperated by empty lines
	 * @return queries List of query strings
	 * @throws IOException
	 */
	public static List<String> getQueriesFromFile(File queriesFile) throws IOException {
		System.out.println("Getting queries from " + queriesFile.toString());
		fis = new FileInputStream(queriesFile);
		isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		br = new BufferedReader(isr);
		String line, currentQuery = "";
		List<String> queries = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			// if line is empty a new query begins
			if (line.trim().isEmpty()) {
				if (!currentQuery.toUpperCase()
						.contains("SELECT DISTINCT ?ID ?ENTITYLABEL ?COUNTRY LANG(?ENTITYLABEL)")) {
					System.err.println(
							"=== FATAL ERROR === \nQuery has to contain the select statement:\n SELECT DISTINCT ?ID ?ENTITYLABEL ?COUNTRY LANG(?ENTITYLABEL)");
					System.exit(0);
				}
				queries.add(currentQuery);
				currentQuery = "";
			}
			currentQuery += "\n" + line;
		}
		return queries;
	}

	/**
	 * Writes the resultStr to file
	 * 
	 * @param outputFile
	 *            File where resultStr will be written to
	 * @param resultStr
	 *            String that will be written to file
	 * @throws FileNotFoundException
	 */
	public static void writeToFile(File outputFile, String resultStr) throws FileNotFoundException {
		System.out.println("Writing results to " + outputFile.getAbsolutePath());
		PrintWriter writer = new PrintWriter(outputFile);
		writer.write(resultStr);
		writer.close();
		System.out.println("Finished successfully");
	}

	/**
	 * Execute each query and integrates all the results into a HashSet
	 * 
	 * @param queries
	 *            SPARQL queries
	 * @param endpoint
	 *            (dbpedia) endpoint
	 * @param graph
	 *            graph of the endpoint or null
	 * @param model
	 *            model of the endpoint or null
	 * @return finalRes HashSet containing results
	 */
	public static HashSet<QuerySolution> executeAndIntegrateQueries(List<String> queries, String endpoint, String graph,
			Model model) {
		HashSet<QuerySolution> finalRes = new HashSet<QuerySolution>();
		for (String q : queries) {
			System.out.println("Running query: " + q);
			ResultSet results = querySelect(q, endpoint, graph, model);
			int count = 0;
			while (results.hasNext()) {
				count++;
				finalRes.add(results.next());
			}
			System.out.println("Retrieved " + count + "results");
		}

		return finalRes;
	}

	/**
	 * executes query
	 * 
	 * @param query
	 *            query
	 * @param endpoint
	 *            endpoint
	 * @param graph
	 *            graph
	 * @param model
	 *            model
	 * @return ResultSet
	 */
	public static ResultSet querySelect(String query, String endpoint, String graph, Model model) {
		try {
			ResultSet results = queryExecution(query, graph, endpoint, model).execSelect();
			return results;
		} catch (RuntimeException e) {
			throw new RuntimeException(
					"Error with query \"" + query + "\" at endpoint \"" + endpoint + "\" and graph \"" + graph + "\"",
					e);
		}
	}

	/**
	 * creates a new object of QueryExecution
	 * 
	 * @param query
	 *            query
	 * @param graph
	 *            graph
	 * @param endpoint
	 *            endpoint
	 * @param model
	 *            model
	 * @return QueryExecution object
	 */
	public static QueryExecution queryExecution(String query, String graph, String endpoint, Model model) {
		ARQ.setNormalMode();
		Query sparqlQuery = QueryFactory.create(query, Syntax.syntaxARQ);
		QueryExecution qexec;

		if (model == null) {
			if (graph != null) {
				qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery, graph);
			} else {
				qexec = QueryExecutionFactory.sparqlService(endpoint, sparqlQuery);
			}
		} else {
			System.out.println("Query to Model...");
			qexec = QueryExecutionFactory.create(sparqlQuery, model);
		}

		return qexec;
	}
}
