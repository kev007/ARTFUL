package main.java.fmdir;

/**
 * Created by kev_s on 08.11.2016.
 */
public class Translation {
    public int id;
    public String citylabel;
    public String locatedIn;
    public String language;

    public Translation(int id, String citylabel, String locatedIn, String language) {
        this.id = id;
        this.citylabel = citylabel;
        this.locatedIn = locatedIn;
        this.language = language;
    }
}
