package fmdir;

import java.io.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.Date;

/**
 * TODO: Write the description
 *
 *
 */
public class Main {
    public static Properties prop = new Properties();

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
        System.out.println(ANSI_CYAN + "Start time: " + new Date() + ANSI_RESET);
        readConfig();
        DatabaseTools.prop = prop;

        /**
         * CAUTION
         * TEST FUNCTION
         *
         * DELETES ALL ROWS
         */
        DatabaseTools.deleteAllRows("words");

        /**
         * Read target directory from config file, replace with default if empty
         */
        String targetDir = prop.getProperty("corpusPath");

        //Get all file paths for the .txt files
        ArrayList<String> allFilePaths = txtTools.getAllPaths(targetDir + "/txt");

        /**
         * Iterate through all files, get all word frequencies, and pass them on to the database filler
         */
        int temp = 0;
        for (String path: allFilePaths) {
            HashMap<String, Integer> wordFreq = txtTools.importWords(path);

            //get the file name from the file path (last segment)
            String[] segments = path.split("\\\\");
            String fileName = segments[segments.length-1];

            String year = parseYear(fileName);
            String language = parseLanguage(fileName);

            temp++;
            System.out.println(ANSI_BLUE + "(" + temp + "/" + allFilePaths.size() + ") - " +  year + " " + language + ": " + wordFreq.size() + " words" + ANSI_RESET);
//            System.out.println("test frequency: " + wordFreq.get("!"));

            DatabaseTools.fillDatabase(wordFreq, year, language);
//            DatabaseTools.updateDatabase(wordFreq, year, language);
        }

        System.out.println(ANSI_CYAN + "End time: " + new Date() + ANSI_RESET);
        System.out.println(ANSI_CYAN + "Duration: " + (float)(System.currentTimeMillis() - startTime)/1000 + " seconds" + ANSI_RESET);

        DatabaseTools.printCount();
//        DatabaseTools.TESTprintDB(5);
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
