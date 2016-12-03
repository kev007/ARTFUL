package fmdir.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "translation")
public class Translation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer w_id;
    private String word;
    private String language;
    @Column(name = "located_in")
    private String locatedIn;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getW_id() {
        return w_id;
    }

    public void setW_id(Integer w_id) {
        this.w_id = w_id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLocatedIn() {
        return locatedIn;
    }

    public void setLocatedIn(String locatedIn) {
        this.locatedIn = locatedIn;
    }

    @Override
    public String toString() {
        return "Translation{" +
                "id=" + id +
                ", w_id=" + w_id +
                ", word='" + word + '\'' +
                ", language='" + language + '\'' +
                ", locatedIn='" + locatedIn + '\'' +
                '}';
    }
}
