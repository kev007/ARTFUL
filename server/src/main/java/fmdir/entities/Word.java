package fmdir.entities;

import javax.persistence.*;

@Entity
@Table(name = "word")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer translation_id;
    private Integer freq;
    private Integer year;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTranslation_id() {
        return translation_id;
    }

    public void setTranslation_id(Integer translation_id) {
        this.translation_id = translation_id;
    }

    public Integer getFreq() {
        return freq;
    }

    public void setFreq(Integer freq) {
        this.freq = freq;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", translation_id=" + translation_id +
                ", freq=" + freq +
                ", year=" + year +
                '}';
    }
}
