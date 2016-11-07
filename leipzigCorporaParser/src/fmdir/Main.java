package fmdir;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * TODO: Write the description
 *
 *
 */
public class Main {
    //Global properties
    public static Properties prop = new Properties();
    public static int id = 0;
    public static final String driverName= "org.sqlite.JDBC";

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.out.println(ANSI_CYAN + "Starting time: " + new Date() + ANSI_RESET);

        readConfig();

        /**
         * Read target directory from config file, replace with default if empty
         */
        String targetDir = prop.getProperty("corpusPath");

        //Get all file paths
        ArrayList<String> allFilePaths = getAllPaths(targetDir);

        /**
         * CAUTION
         * TEST FUNCTION
         *
         * DELETES ALL ROWS
         */
//        deleteAllRows("words");

        /**
         * Iterate through all files, get all word frequencies, and pass them on to the database filler
         */
        int temp = 0;
        for (String path: allFilePaths) {
            HashMap<String, Integer> wordFreq = importWords(path);

            //get the file name from the file path (last segment)
            String[] segments = path.split("\\\\");
            String fileName = segments[segments.length-1];

            String year = parseYear(fileName);
            String language = parseLanguage(fileName);

            temp++;
            System.out.println(ANSI_BLUE + "(" + temp + "/" + allFilePaths.size() + ") - " +  year + " " + language + ": " + wordFreq.size() + " words" + ANSI_RESET);
//            System.out.println("test frequency: " + wordFreq.get("!"));

//            fillDatabase(wordFreq, year, language);
            updateDatabase(wordFreq, year, language);
        }

        System.out.println(ANSI_CYAN + "End time: " + new Date() + ANSI_RESET);
        System.out.println(ANSI_CYAN + "Duration: " + (float)(System.currentTimeMillis() - startTime)/1000 + " seconds" + ANSI_RESET);


//        TESTprintDB(5);
    }

    /**
     * DEBUG function
     * @param rowCount
     */
    private static void TESTprintDB(int rowCount) {
        System.out.println("Testing database read: ");
        try {
            Class.forName(driverName).newInstance();
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + prop.getProperty("dbPath"));

            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM words;");
            int columnCount = rs.getMetaData().getColumnCount();

            int temp = 0;
            while (rs.next() && temp<rowCount){
                temp++;
                String allColumns = "";
                for (int i=1; i<=columnCount; i++) allColumns += rs.getString(i) + "\t";
                System.out.println(allColumns);
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteAllRows(String dbName) {
        System.out.println("Deleting table contents: " + dbName);
        try {
            Class.forName(driverName).newInstance();
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + prop.getProperty("dbPath"));

            Statement s = connection.createStatement();

            String query = "DELETE FROM " + dbName;
            int deletedRows=s.executeUpdate(query);
            if(deletedRows>0){
                System.out.println("Deleted all rows in the table successfully...");
            }else{
                System.out.println("Table already empty.");
            }
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateDatabase(HashMap<String, Integer> wordFreq, String year, String language) {
        try {
            long updateStart = System.currentTimeMillis();

            Class.forName(driverName).newInstance();
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + prop.getProperty("dbPath"));

            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT word, language, " + year + " FROM words WHERE language='" + language + "';");
//            int columnCount = rs.getMetaData().getColumnCount();
//            int rowCount = 3;
//            int temp = 0;
//            while (rs.next() && temp<rowCount){
//                temp++;
//                String allColumns = "";
//                for (int i=1; i<=columnCount; i++) allColumns += rs.getString(i) + "\t";
//                System.out.println(allColumns);
//            }

            PreparedStatement update = connection.prepareStatement("update words set " + year + " = ?1 WHERE word = ?2 and language = ?3");

            int searched = 0;
            int found = 0;
            while (rs.next() && searched<9){
                searched++;

                String word = rs.getString(1);

                if (wordFreq.containsKey(word)) {
                    found++;

                    update.setInt(1, wordFreq.get(word));
                    update.setString(2, word);
                    update.setString(3, language);
                    update.addBatch();
                }
            }
//            for (Object kvPair : wordFreq.entrySet()) {
//                Map.Entry pair = (Map.Entry) kvPair;
//                String word = pair.getKey().toString();
//                int freq = (int) pair.getValue();
//
//                update.setInt(1, freq);
//                update.setString(2, word);
//                update.setString(3, language);
//                update.addBatch();
//            }

            long commitStart = System.currentTimeMillis();
            System.out.println(searched + " words queried and compared in\t" + (float)(commitStart-updateStart)/1000 + " seconds");

            connection.setAutoCommit(false);
            update.executeBatch();

            connection.commit();
            connection.close();

            System.out.println(found + " word frequencies updated in\t" + (float)(System.currentTimeMillis()-commitStart)/1000 + " seconds");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fill the database
     * @param wordFreq
     * @param year
     * @param language
     */
    private static void fillDatabase(HashMap<String, Integer> wordFreq, String year, String language) {
        try {
            Class.forName(driverName).newInstance();
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + prop.getProperty("dbPath"));
            PreparedStatement insert = connection.prepareStatement("insert into words (id, w_id, word, language, located_in," + year + ") values (?1, ?2, ?3, ?4, ?5, ?6);");

            for (Object kvPair : wordFreq.entrySet()) {
                Map.Entry pair = (Map.Entry) kvPair;
                String word = pair.getKey().toString();
                int freq = (int) pair.getValue();

                id++;
                insert.setInt(1, id);
                insert.setInt(2, 0);
                insert.setString(3, word);
                insert.setString(4, language);
                insert.setString(5, null);
                insert.setInt(6, freq);
                insert.addBatch();
            }

            connection.setAutoCommit(false);
            insert.executeBatch();

            connection.commit();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the language from the filename
     * @param filename the file name
     * @return the language
     */
    private static String parseLanguage(String filename) {
        String language = "";

        String[] segments = filename.split("_");
        language = segments[0];

        if ("".equals(language)) {
            System.out.println("No valid language found: " + filename);
        } else {
//            System.out.println("Language: " + language);
        }
        return language;
    }

    /**
     * Gets the year from the filename
     * @param filename the file name
     * @return the year
     */
    private static String parseYear(String filename) {
        String year = "";

        String[] segments = filename.split("_");
        year = segments[2];

        if ("".equals(year)) {
            System.out.println("No valid year found: " + filename);
        } else {
//            System.out.println("Year: " + year);
        }

        if(year.contains("-")) {
            year = year.replace("-","_");
        }

        year = "freq_" + year;
        return year;
    }

    /**
     * Parses the word frequency from the specified file
     * @param path the path to the text file
     * @return the Key,Value word frequency map
     */
    private static HashMap<String, Integer> importWords(String path) {
        HashMap<String, Integer> wordFreq = new HashMap<>();

        BufferedReader br = null;

        try {
            String currentLine;

            br = new BufferedReader(new FileReader(path));

            while ((currentLine = br.readLine()) != null) {

                String[] segments = currentLine.split("\t");

                wordFreq.put(segments[segments.length-2], Integer.parseInt(segments[segments.length-1]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return wordFreq;
    }

    /**
     * Finds all files in a given directory
     * @param targetDir the target directory
     * @return the paths of all files in ArrayList form
     */
    private static ArrayList<String> getAllPaths(String targetDir) {
        ArrayList<String> allPaths = new ArrayList<>();

        File dir = new File(targetDir);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                allPaths.add(child.getPath());
//                System.out.println(child.getPath());
            }
        } else {
            System.out.println(targetDir + " is not a directory");
        }

        return allPaths;
    }

    /**
     * Reads the config file into the global properties variable
     */
    private static void readConfig() {
        InputStream input = null;

        try {
            input = new FileInputStream("config.properties");

            // load a properties file
            prop.load(input);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Read property, replace with default if empty
         */
        if(prop.getProperty("corpusPath").isEmpty()) {
            System.out.println("corpusPath empty! Using default path");
            prop.setProperty("corpusPath", System.getProperty("user.dir") + "/resources");
        }
        if(prop.getProperty("dbPath").isEmpty()) {
            System.out.println("dbPath empty! Using default path");
            prop.setProperty("dbPath", "C:/workspace/2016-FMdIR-Thema1/database/AllFreq.sqlite");
        }
    }
}
