package main.java.fmdir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class FileTools {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    /**
     * TODO: Write description
     * @param allTranslationPaths
     */
    public static HashMap<String, ArrayList<Translation>> importCSVbyLang(ArrayList<String> allTranslationPaths) {
        long importStartTime = System.currentTimeMillis();

        HashMap<String, ArrayList<Translation>> translationMap = new HashMap<>();

        BufferedReader br = null;

        int found = 0;

        for (String path: allTranslationPaths) {
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
                    lang = parseCSVStuff(lang);
                    int id = Integer.parseInt(segments[0]);
                    String citylabel = parseCSVStuff(segments[1]);
                    String locatedIn = parseCSVStuff(segments[2]);

                    //TODO: make below code readable
                    ArrayList<Translation> translations = new ArrayList<>();
                    if (!translationMap.containsKey(lang)){
                        translationMap.put(lang, translations);
                    } else {
                        translations = translationMap.get(lang);
                    }
                    translations.add(new Translation(id, citylabel, locatedIn, lang));
                    translationMap.put(lang, translations);

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
        }

        System.out.println(found + " translations imported from " + allTranslationPaths.size() + " files in \t\t" + (float)(System.currentTimeMillis()-importStartTime)/1000 + " seconds");

        return translationMap;
    }

    private static String parseCSVStuff(String lang) {

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
                try {
                    String[] segments = currentLine.split("\t");
                    wordFreq.put(segments[segments.length-2], Integer.parseInt(segments[segments.length-1]));
                }  catch (Exception e) {
                    System.out.println(ANSI_YELLOW + "ERROR in document: " + path + " - '" + currentLine + "'" + ANSI_RESET);
                }
            }

        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println(ANSI_YELLOW + "ERROR in document: " + path + ANSI_RESET);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
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

//        year = "freq_" + year;
        return year;
    }

    public static ArrayList<String> deleteSmaller(ArrayList<String> allFilePaths) {
        HashMap<String, String> largestRightPath = new HashMap<>();
        ArrayList<String> oldFilePaths = new ArrayList<>();
        ArrayList<String> newFilePaths = new ArrayList<>();

        String folder = "";

        for (String path: allFilePaths) {

            //get the file name from the file path (last segment)
            String[] segments = path.split("\\\\");
            folder = path.split(segments[segments.length-1])[0];
            String fileName = segments[segments.length-1];

            segments = fileName.split("_");
            if (segments.length < 4) {
                System.out.println(ANSI_YELLOW + segments.length + " - SIZE ERROR " + fileName + ANSI_RESET);
            } else {
                String leftPath = fileName.split(segments[3])[0];
                String rightPath = segments[3];

                int sizeRankNew = getCorporaSize(fileName);

                if (sizeRankNew == 0) {
                    System.out.println(ANSI_WHITE + "bad file syntax: " + fileName + ANSI_RESET);
                } else if (sizeRankNew < 0) {
                    //compressed file. do nothing. for now.
                } else if (largestRightPath.containsKey(leftPath)) {
                    int sizeRankOld = getCorporaSize(largestRightPath.get(leftPath));

                    if (sizeRankNew > sizeRankOld) {
                        oldFilePaths.add(folder + leftPath + largestRightPath.get(leftPath));

                        largestRightPath.put(leftPath, rightPath);
                        System.out.println(sizeRankNew + " " + sizeRankOld);
                    } else if (sizeRankNew > sizeRankOld) {
                        System.out.println("duplicate found: " + fileName + " == " + leftPath + largestRightPath.get(leftPath));
                    } else {
                        oldFilePaths.add(path);
                    }
                } else {
                    largestRightPath.put(leftPath, rightPath);
                }
            }
        }

        Iterator it = largestRightPath.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            newFilePaths.add(folder + pair.getKey()+ "" + pair.getValue());
        }

        if (oldFilePaths.size() > 0) {
            deleteFiles(oldFilePaths);
        }

        return newFilePaths;
    }

    private static void deleteFiles(ArrayList<String> allFilePaths) {
        for (String path: allFilePaths) {
            System.out.println("Found a larger Corpus, adding to delete list: " + ANSI_YELLOW + path + ANSI_RESET);
        }
        System.out.println(allFilePaths.size() + " items found");

        System.out.println();
        System.out.println(ANSI_RED + "   ************************CAUTION*************************   " + ANSI_RESET);
        System.out.println(ANSI_RED + "   **                                                    **   " + ANSI_RESET);
        System.out.println(ANSI_RED + "   **  Would you like to delete the above listed files?  **   " + ANSI_RESET);
        System.out.println(ANSI_RED + "   **                                                    **   " + ANSI_RESET);
        System.out.println(ANSI_RED + "   ************************CAUTION*************************   " + ANSI_RESET);
        System.out.println();

        Scanner scanner = new Scanner(System.in);
        String userInput = scanner.next();
        if(userInput.equalsIgnoreCase("y") || userInput.equalsIgnoreCase("yes")) {
            System.out.println("This will be fun");
            for (String path: allFilePaths) {
                new File(path).delete();
            }
        } else {
            System.out.println("Maybe next time");
        }
    }

    private static int getCorporaSize(String path) {
        String[] segments = path.split("_");
        String size = segments[segments.length-1];

        int sizeRank;
        switch (size) {
            case "10K-words.txt": sizeRank = 5; break;
            case "30K-words.txt": sizeRank = 10; break;
            case "100K-words.txt": sizeRank = 15; break;
            case "300K-words.txt": sizeRank = 20; break;
            case "1M-words.txt": sizeRank = 25; break;
            case "3M-words.txt": sizeRank = 30; break;

            case "10K-text.tar.gz": sizeRank = 4; break;
            case "30K-text.tar.gz": sizeRank = 9; break;
            case "100K-text.tar.gz": sizeRank = 14; break;
            case "300K-text.tar.gz": sizeRank = 19; break;
            case "1M-text.tar.gz": sizeRank = 24; break;
            case "3M-text.tar.gz": sizeRank = 29; break;

            case "10K.tar.gz": sizeRank = 3; break;
            case "30K.tar.gz": sizeRank = 8; break;
            case "100K.tar.gz": sizeRank = 13; break;
            case "300K.tar.gz": sizeRank = 18; break;
            case "1M.tar.gz": sizeRank = 23; break;
            case "3M.tar.gz": sizeRank = 29; break;

            case "10K-mysql.tar.gz": sizeRank = 1; break;
            case "30K-mysql.tar.gz": sizeRank = 3; break;
            case "100K-mysql.tar.gz": sizeRank = 5; break;
            case "300K-mysql.tar.gz": sizeRank = 7; break;
            case "1M-mysql.tar.gz": sizeRank = 9; break;
            case "3M-mysql.tar.gz": sizeRank = 11; break;


            default: sizeRank = 0; break;
        }

        return sizeRank;
    }
}
