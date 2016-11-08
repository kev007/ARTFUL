package fmdir;

import java.io.*;
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

        /**
         * CAUTION
         * TEST FUNCTION
         *
         * DELETES ALL ROWS
         */
        DatabaseTools.deleteAllRows("words");

        //Import translations CSV
        HashMap<String, ArrayList<Translation>> allTranslations = FileTools.importCSVbyLang(prop.getProperty("corpusPath") + "/sparql.csv");

        //Get all file paths for the .txt files
        ArrayList<String> allFilePaths = FileTools.getAllPathsFrom(prop.getProperty("corpusPath") + "/txt");

        //Iterate through all files, get all word frequencies, and pass them on to the database filler
        int currentCorpora = 0;
        for (String path: allFilePaths) {
            currentCorpora++;
            HashMap<String, Integer> wordFreq = FileTools.importWordFrequencies(path);

            //get the file name from the file path (last segment)
            String[] segments = path.split("\\\\");
            String fileName = segments[segments.length-1];

            String year = FileTools.parseYear(fileName);
            String language = FileTools.parseLanguage(fileName);

            String tempLang = "";
            if (language.equals("eng")) {
                tempLang = "en";
            }
            if (language.equals("deu")) {
                tempLang = "de";
            }

            if(allTranslations.containsKey(tempLang)) {
                ArrayList<Translation> translations = allTranslations.get(tempLang);

                System.out.println(ANSI_BLUE + "(" + currentCorpora + "/" + allFilePaths.size() + ") - " +  year + " " + language + ": " + wordFreq.size() + " words, " + translations.size() + " translations" + ANSI_RESET);

                DatabaseTools.fillDatabase(translations, wordFreq, year, language);
            } else {
                System.out.println(ANSI_YELLOW + "Language missing or mismatch: " + language + ANSI_RESET);
            }

//            DatabaseTools.writeAllWordFrequencies(wordFreq, year, language);
//            DatabaseTools.updateWordFrequencies(wordFreq, year, language);
        }

        System.out.println(ANSI_CYAN + "End time: " + new Date() + ANSI_RESET);
        System.out.println(ANSI_CYAN + "Duration: " + (float)(System.currentTimeMillis() - startTime)/1000 + " seconds" + ANSI_RESET);

        DatabaseTools.printCount();
//        DatabaseTools.TESTprintDB(5);
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
            prop.setProperty("dbPath", "C:/workspace/2016-FMdIR-Thema1/database/translations.sqlite");
        }
    }
}
