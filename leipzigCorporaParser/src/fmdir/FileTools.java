package fmdir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FileTools {
    /**
     * TODO: Write description
     * @param path
     */
    public static HashMap<String, ArrayList<Translation>> importCSVbyLang(String path) {
        HashMap<String, ArrayList<Translation>> translations = new HashMap<>();

        BufferedReader br = null;

        int found = 0;

        try {
            br = new BufferedReader(new FileReader(path));
            String currentLine;
            //Skip line
            br.readLine();
            while ((currentLine = br.readLine()) != null) {
                String[] segments = currentLine.split(",");
                String lang = "";

                //comma in translation workaround
                if (segments.length > 4) {
                    segments[1] = segments[1] + segments[2];
                    segments[2] = segments[3];
                    lang = segments[4];
                } else {
                    lang = segments[3];
                }
                //TODO: proper language thingamajig
                lang = doLangTranslation(lang);

                //TODO: make below code readable
                ArrayList<Translation> translation = new ArrayList<>();
                if (!translations.containsKey(lang)){
                    translations.put(lang, translation);
                } else {
                    translation = translations.get(lang);
                }
                translation.add(new Translation(Integer.parseInt(segments[0]),segments[1],segments[2],lang));
                translations.put(lang, translation);

                found++;
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

        System.out.println(found + " translations imported");

        return translations;
    }

    private static String doLangTranslation(String lang) {

        lang = lang.replace("\"", "");

        return lang;
    }

    /**
     * Parses the word frequency from the specified file
     * @param path the path to the text file
     * @return the Key,Value word frequency map
     */
    public static HashMap<String, Integer> importWordFrequencies(String path) {
        HashMap<String, Integer> wordFreq = new HashMap<>();

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(path));
            String currentLine;
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
    public static ArrayList<String> getAllPathsFrom(String targetDir) {
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
     * Gets the language from the filename
     * @param filename the file name
     * @return the language
     */
    public static String parseLanguage(String filename) {
        String language = "";

        String[] segments = filename.split("_");
        language = segments[0];

        if ("".equals(language)) {
            System.out.println("No valid language found: " + filename);
        } else {
//            System.out.println("Language: " + language);
        }

        //TODO: language thingamabob
        return language;
    }

    /**
     * Gets the year from the filename
     * @param filename the file name
     * @return the year
     */
    public static String parseYear(String filename) {
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
}
