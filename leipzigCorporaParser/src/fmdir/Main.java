package fmdir;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class Main {
    public static Properties prop = new Properties();

    public static void main(String[] args) {
        readConfig();

        String targetDir;
        if(prop.getProperty("corpusPath").isEmpty()) {
            targetDir = System.getProperty("user.dir") + "/resources";
        } else {
            targetDir = prop.getProperty("corpusPath");
        }

        ArrayList<String> allFilePaths = getAllPaths(targetDir);

        for (String path: allFilePaths) {
            HashMap<String, Integer> wordFreq = importWords(path);

            String[] segments = path.split("\\\\");
            String fileName = segments[segments.length-1];

            String year = parseYear(fileName);
            String language = parseLanguage(fileName);


            System.out.println(year + " " + language + ": " + wordFreq.size() + " words");
//            System.out.println("test frequency: " + wordFreq.get("!"));

            fillDatabase(wordFreq, year, language);
        }
    }

    private static void fillDatabase(HashMap<String, Integer> wordFreq, String year, String language) {
        //TODO: fill the database
    }

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
