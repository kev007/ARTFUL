package main.java.fmdir;

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
        DatabaseTools.deleteAllRows("word");
        DatabaseTools.deleteAllRows("translation");

        //Import translations CSV
        ArrayList<String> allTranslationPaths = FileTools.getAllPathsFrom(prop.getProperty("translationPath"));
        HashMap<String, ArrayList<Translation>> allTranslations = FileTools.importCSVbyLang(allTranslationPaths);

        //Get all file paths for the .txt files
        ArrayList<String> allFreqPaths = FileTools.getAllPathsFrom(prop.getProperty("corpusPath"));

        /**
         * TEST FUNCTION
         *
         * Deletes SMALLER CORPORA WITH THE SAME YEAR AND LANGUAGE
         */
        allFreqPaths = FileTools.deleteSmaller(allFreqPaths);

        //Iterate through all files, get all word frequencies, and pass them on to the database filler
        int currentCorpora = 0;
        for (String path: allFreqPaths) {
            currentCorpora++;
            long parseStart = System.currentTimeMillis();

            String[] segments = path.split("\\\\");
            String fileName = segments[segments.length-1];

            String year = FileTools.parseYear(fileName);
            String language = FileTools.parseLanguage(fileName);

            String tempLang = "";
            if (language.contains("eng")) {
                tempLang = "en";
            } else if (language.contains("deu")) {
                tempLang = "de";
            }

            if(allTranslations.containsKey(tempLang)) {
                HashMap<String, Integer> wordFreq = FileTools.importWordFrequencies(path);

                ArrayList<Translation> translations = allTranslations.get(tempLang);

                System.out.println(ANSI_BLUE + "(" + currentCorpora + "/" + allFreqPaths.size() + ") - " +  year + " " + language + ": " + wordFreq.size() + " words imported in \t\t " + (float)(System.currentTimeMillis()-parseStart)/1000 + " seconds" + ANSI_RESET);

                DatabaseTools.fillDatabase(translations, wordFreq, year, language);
            } else {
//                System.out.println(ANSI_BLUE + "(" + currentCorpora + "/" + allFreqPaths.size() + ") - " +  year + " " + ANSI_YELLOW + "\t Unknown Language: " + ANSI_RED + language + ANSI_RESET);
            }
        }

        System.out.println(ANSI_CYAN + "End time: " + new Date() + ANSI_RESET);
        System.out.println(ANSI_CYAN + "Duration: " + (float)(System.currentTimeMillis() - startTime)/1000 + " seconds" + ANSI_RESET);

        DatabaseTools.printCount();
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
        String defaultPath = "";
        if(prop.getProperty("corpusPath") == null || prop.getProperty("corpusPath").isEmpty()) {

            defaultPath = System.getProperty("user.dir") + "/resources" + "/txt";

            System.out.println("corpusPath empty! Using default path: " + ANSI_GREEN + defaultPath + ANSI_RESET);
            prop.setProperty("corpusPath", defaultPath);
        }
        if(prop.getProperty("translationPath") == null || prop.getProperty("translationPath").isEmpty()) {

            defaultPath = System.getProperty("user.dir") + "/resources/translations";

            System.out.println("translationPath empty! Using default path: " + ANSI_GREEN + defaultPath + ANSI_RESET);
            prop.setProperty("translationPath", defaultPath);
        }
        if(prop.getProperty("dbPath") == null || prop.getProperty("dbPath").isEmpty()) {

            defaultPath = "C:/workspace/2016-FMdIR-Thema1/database/translations.sqlite";

            System.out.println("dbPath empty! Using default path: " + ANSI_GREEN + defaultPath + ANSI_RESET);
            prop.setProperty("dbPath", defaultPath);
        }

    }
}