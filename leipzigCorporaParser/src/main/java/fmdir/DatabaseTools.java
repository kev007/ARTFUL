package main.java.fmdir;

import java.io.File;
import java.nio.file.Path;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by kev_s on 08.11.2016.
 */
public class DatabaseTools {
    public static final String driverName= "org.sqlite.JDBC";
    public static int wordID = 0;
    public static int translationID = 0;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static HashMap<String, String> languageKeys;

    /**
     * Fill the database
     * @param translations
     * @param wordFreq
     * @param year
     * @param language
     */
    public static void fillDatabase(ArrayList<Translation> translations, HashMap<String, Integer> wordFreq, String year, String language, Path path) {
        try {
            Class.forName(driverName).newInstance();
//            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Main.prop.getProperty("dbPath"));

            Connection connection = newInMemoryDatabase();

            connection.setAutoCommit(true);


            try {
                long updateStart = System.currentTimeMillis();

                PreparedStatement insertTranslation = connection.prepareStatement("" +
                        "insert into translation (id, w_id, word, language, located_in, category) values (?1, ?2, ?3, ?4, ?5, ?6);");
                PreparedStatement insertWord = connection.prepareStatement("" +
                        "insert into freq (id, translation_id, corpus, freq, year, corporaID) values (?1, ?2, ?3, ?4, ?5, ?6);");
                PreparedStatement insertCorpora = connection.prepareStatement("" +
                        "insert into corpora (fullname, lang, size, year, source) values (?1, ?2, ?3, ?4, ?5);");

                PreparedStatement getTranslation = connection.prepareStatement("SELECT id FROM translation WHERE word = ?1 and language = ?2");
                PreparedStatement getCorpora = connection.prepareStatement("SELECT id FROM corpora WHERE lang = ?1 and year = ?2 and source = ?3");

                //PreparedStatement update = connection.prepareStatement("update word set " + year + " = ?1 WHERE id= ?2");

                String[] segments = path.toString().split("_");
                String corporaSource = segments[1];

                int currentCorporaID = 0;

                insertCorpora.setString(1, path.getFileName().toString());
                insertCorpora.setString(2, language);
                insertCorpora.setInt(3, getCorporaSize(path));
                insertCorpora.setInt(4, Integer.parseInt(year));
                insertCorpora.setString(5, corporaSource);
                insertCorpora.execute();

                getCorpora.setString(1, language);
                getCorpora.setInt(2, Integer.parseInt(year));
                getCorpora.setString(3, corporaSource);
                ResultSet rs1 = getCorpora.executeQuery();
                if (rs1.next()) {
                    currentCorporaID = rs1.getInt(1);
                }

                int foundCount = 0;
                for (Translation translation: translations) {
                    int w_id = translation.id;
                    String word = translation.citylabel;
//                    String language = translation.language;
                    String locatedIn = translation.locatedIn;
                    String category = translation.category;
                    int currentTranslationID;

                    if (wordFreq.containsKey(word)) {
                        foundCount++;
                        getTranslation.setString(1, word);
                        getTranslation.setString(2, language);
                        ResultSet rs2 = getTranslation.executeQuery();
                        if (rs2.next()) {
                            //get existing translation primary key
                            currentTranslationID = rs2.getInt(1);
                        } else {
                            //create new translation
                            translationID++;
                            currentTranslationID = translationID;
                            insertTranslation.setInt(1, translationID);
                            insertTranslation.setInt(2, w_id);
                            insertTranslation.setString(3, word);
                            insertTranslation.setString(4, language);
                            insertTranslation.setString(5, locatedIn);
                            insertTranslation.setString(6, category);
                            insertTranslation.execute();
                        }
                        wordID++;
                        insertWord.setInt(1, wordID);
                        insertWord.setInt(2, currentTranslationID);
                        insertWord.setString(3, language);
                        insertWord.setInt(4, wordFreq.get(word));
                        insertWord.setInt(5, Integer.parseInt(year));
                        insertWord.setInt(6, currentCorporaID);
                        insertWord.execute();
                    }
                }

                System.out.println(currentCorporaID + ": " + foundCount + " translations queried, compared, and written to DB in \t " + (float)(System.currentTimeMillis()-updateStart)/1000 + " seconds");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(ANSI_YELLOW + "ERROR. Rolling back changes." + ANSI_RESET);
                connection.rollback();
            } finally {
                saveDatabaseToFile(connection);
//                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getCorporaSize (Path path) {
        int corporaSize;
        int sizeRank = FileTools.getCorporaSizeRank(path.toString());
        switch (sizeRank) {
            case 5: corporaSize = 10000; break;
            case 10: corporaSize = 30000; break;
            case 15: corporaSize = 100000; break;
            case 20: corporaSize = 300000; break;
            case 25: corporaSize = 1000000; break;
            case 30: corporaSize = 3000000; break;
            default: corporaSize = 0; break;
        }

        return corporaSize;
    }

    /**
     * Creates an in-memory database using the on file database
     * @return
     * @throws Exception
     */
    public static Connection newInMemoryDatabase () throws Exception {
        //Create an in-memory database
        Class.forName(driverName).newInstance();
        Connection connection = DriverManager.getConnection("jdbc:sqlite:");

        Statement statement  = connection.createStatement();

        File dbFile = new File(Main.prop.getProperty("dbPath"));
        if (dbFile.exists()) {
//            System.out.println("File exists: " + Main.prop.getProperty("dbPath"));
            statement.executeUpdate("restore from '" + dbFile.getAbsolutePath() + "'");
            statement.close();
        }

        return connection;
    }

    /**
     * Writes the in-memory database to file
     * @param connection
     * @throws Exception
     */
    public static void saveDatabaseToFile (Connection connection) throws Exception {
        Statement statement  = connection.createStatement();

        File dbFile = new File(Main.prop.getProperty("dbPath"));
        if (dbFile.exists()) {
//            System.out.println("File exists: " + Main.prop.getProperty("dbPath"));
            statement.executeUpdate("backup to '" + dbFile.getAbsolutePath() + "'");
            statement.close();
        }
    }

    /**
     * Runs the SQLite VACUUM command on the database file for compaction
     */
    public static void compactDatabase () {
        try {
            Class.forName(driverName).newInstance();
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Main.prop.getProperty("dbPath"));

            Statement statement  = connection.createStatement();
            statement.execute("PRAGMA auto_vacuum = 1");
            statement.execute("VACUUM");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * OLD: Fill the database
     * @param wordFreq
     * @param year
     * @param language
     */
    public static void writeAllWordFrequencies(HashMap<String, Integer> wordFreq, String year, String language) {
        try {
            Class.forName(driverName).newInstance();
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Main.prop.getProperty("dbPath"));
            connection.setAutoCommit(false);

            try {
                PreparedStatement insert = connection.prepareStatement("insert into freq (id, w_id, word, language, located_in," + year + ") values (?1, ?2, ?3, ?4, ?5, ?6);");

                int found = 0;
                for (Object kvPair : wordFreq.entrySet()) {

                    Map.Entry pair = (Map.Entry) kvPair;
                    String word = pair.getKey().toString();
                    int freq = (int) pair.getValue();

                    found++;
                    translationID++;
                    insert.setInt(1, translationID);
                    insert.setInt(2, 0);
                    insert.setString(3, word);
                    insert.setString(4, language);
                    insert.setString(5, null);
                    insert.setInt(6, freq);
                    insert.addBatch();
                }

                long commitStart = System.currentTimeMillis();
                insert.executeBatch();
                connection.commit();
                System.out.println(found + " word frequencies added in\t" + (float)(System.currentTimeMillis()-commitStart)/1000 + " seconds");
            } catch (Exception e) {
//                e.printStackTrace();
                System.out.println(ANSI_YELLOW + "ERROR. Rolling back changes." + ANSI_RESET);
                connection.rollback();
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * OLD: fills the database, updates existing records
     * @param wordFreq
     * @param year
     * @param language
     */
    public static void updateWordFrequencies(HashMap<String, Integer> wordFreq, String year, String language) {
        try {
            Class.forName(driverName).newInstance();
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Main.prop.getProperty("dbPath"));
            connection.setAutoCommit(false);

            try {
                long updateStart = System.currentTimeMillis();

                Statement s = connection.createStatement();
                ResultSet rs = s.executeQuery("SELECT freq, language, " + year + " FROM freq WHERE language='" + language + "';");
                PreparedStatement update = connection.prepareStatement("update freq set " + year + " = ?1 WHERE word = ?2 and language = ?3");

                int searched = 0;
                int found = 0;
                while (rs.next() && searched<3){
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

                long commitStart = System.currentTimeMillis();
                System.out.println(searched + " words queried and compared in\t" + (float)(commitStart-updateStart)/1000 + " seconds");
                update.executeBatch();
                connection.commit();
                System.out.println(found + " word frequencies updated in\t" + (float)(System.currentTimeMillis()-commitStart)/1000 + " seconds");
            } catch (Exception e) {
//                e.printStackTrace();
                System.out.println(ANSI_YELLOW + "ERROR. Rolling back changes." + ANSI_RESET);
                connection.rollback();
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * DEBUG function
     * @param rowCount
     */
    public static void TESTprintDB(int rowCount) {
        try {
            Class.forName(driverName).newInstance();
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Main.prop.getProperty("dbPath"));

            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM freq;");
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

    public static void deleteAllRows(String tableName) {
        System.out.println("Deleting table contents: " + tableName);
        try {
            Class.forName(driverName).newInstance();
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Main.prop.getProperty("dbPath"));

            Statement s = connection.createStatement();

            String query = "DELETE FROM " + tableName;
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

    public static void printCount() {
        try {
            Class.forName(driverName).newInstance();
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + Main.prop.getProperty("dbPath"));
            ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM translation;");
            System.out.println(ANSI_CYAN + "Translations: " + NumberFormat.getNumberInstance(Locale.US).format(rs.getInt(1)) + " rows" + ANSI_RESET);
            rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM freq;");
            System.out.println(ANSI_CYAN + "Frequencies: " + NumberFormat.getNumberInstance(Locale.US).format(rs.getInt(1)) + " rows" + ANSI_RESET);
            rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM corpora;");
            System.out.println(ANSI_CYAN + "Corpora: " + NumberFormat.getNumberInstance(Locale.US).format(rs.getInt(1)) + " rows" + ANSI_RESET);
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
