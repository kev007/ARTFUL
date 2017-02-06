package main.java.fmdir;

/**
 * Created by kev_s on 08.11.2016.
 */
public class Translation {
    public int id;
    public String citylabel;
    public String locatedIn;
    public String language;
    public String category;

    public Translation(int id, String citylabel, String locatedIn, String language, String category) {
        this.id = id;
        this.citylabel = citylabel;
        this.locatedIn = locatedIn;
        this.language = language;
        this.category = category;
    }
}
