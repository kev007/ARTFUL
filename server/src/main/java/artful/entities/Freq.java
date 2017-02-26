package artful.entities;

import javax.persistence.*;

@Entity
@Table(name = "freq")
public class Freq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer translation_id;
    private Integer freq;
    private String corpus;
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

    public String getCorpus() {
        return corpus;
    }

    public void setCorpus(String corpus) {
        this.corpus = corpus;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Freq{" +
                "id=" + id +
                ", translation_id=" + translation_id +
                ", freq=" + freq +
                ", corpus='" + corpus + '\'' +
                ", year=" + year +
                '}';
    }
}
