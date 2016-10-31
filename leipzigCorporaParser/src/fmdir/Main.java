package fmdir;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

/**
 * TODO: Write the description
 *
 *
 */
public class Main {
    //Global properties
    public static Properties prop = new Properties();

    public static void main(String[] args) {
        readConfig();

        /**
         * Read target directory from config file, replace with default if empty
         */
        String targetDir;
        if(prop.getProperty("corpusPath").isEmpty()) {
            targetDir = System.getProperty("user.dir") + "/resources";
        } else {
            targetDir = prop.getProperty("corpusPath");
        }

        //Alternate to above, needs testing (property key, default value)
        //String targetDir = prop.getProperty("corpusPath", System.getProperty("user.dir") + "/resources");

        //Get all file paths
        ArrayList<String> allFilePaths = getAllPaths(targetDir);

        /**
         * Iterate through all files, get all word frequencies, and pass them on to the database filler
         */
        for (String path: allFilePaths) {
            HashMap<String, Integer> wordFreq = importWords(path);

            //get the file name from the file path (last segment)
            String[] segments = path.split("\\\\");
            String fileName = segments[segments.length-1];

            String year = parseYear(fileName);
            String language = parseLanguage(fileName);


            System.out.println(year + " " + language + ": " + wordFreq.size() + " words");
//            System.out.println("test frequency: " + wordFreq.get("!"));

            fillDatabase(wordFreq, year, language);
        }
    }

    /**
     * Fill the database
     * @param wordFreq
     * @param year
     * @param language
     */
    private static void fillDatabase(HashMap<String, Integer> wordFreq, String year, String language) {
        //TODO: fill the database
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
                if (br != null)br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
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

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
