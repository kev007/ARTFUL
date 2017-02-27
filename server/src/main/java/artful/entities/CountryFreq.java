package artful.entities;

import javax.lang.model.element.Name;
import javax.persistence.*;

@Entity
@Table(name = "country_freq")
@NamedQueries({
        @NamedQuery(name = "CountryFreq.findAllByYearBetween",
                query = "SELECT cf.country, SUM(cf.freq) AS freq, cf.avgCorporaSizeIngoing, cf.avgCorporaSizeOutgoing FROM CountryFreq cf WHERE cf.year BETWEEN ?1 AND ?2 GROUP BY cf.country"),
        @NamedQuery(name = "CountryFreq.findTopTenMentioning",
                query = "SELECT f.corpus, SUM(f.freq) AS freqpercorp FROM Freq f, Translation t WHERE f.id = t.id AND t.locatedIn = ?1 AND f.year BETWEEN ?2 AND ?3 GROUP BY f.corpus ORDER BY freqpercorp DESC")
})
public class CountryFreq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String country;
    private Integer freq;

    @Column(name = "avg_corpora_size_ingoing")
    private Integer avgCorporaSizeIngoing;

    @Column(name = "avg_corpora_size_outgoing")
    private Integer avgCorporaSizeOutgoing;
    private Integer year;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
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

    public Integer getAvgCorporaSizeIngoing() {
        return avgCorporaSizeIngoing;
    }

    public void setAvgCorporaSizeIngoing(Integer avgCorporaSizeIngoing) {
        this.avgCorporaSizeIngoing = avgCorporaSizeIngoing;
    }


    public Integer getAvgCorporaSizeOutgoing() {
        return avgCorporaSizeOutgoing;
    }

    public void setAvgCorporaSizeOutgoing(Integer avgCorporaSizeOutgoing) {
        this.avgCorporaSizeOutgoing = avgCorporaSizeOutgoing;
    }

    @Override
    public String toString() {
        return "CountryFreq{" +
                "id=" + id +
                ", country='" + country + '\'' +
                ", freq=" + freq +
                ", avgCorporaSizeIngoing=" + avgCorporaSizeIngoing +
                ", avgCorporaSizeOutgoing=" + avgCorporaSizeOutgoing +
                ", year=" + year +
                '}';
    }
}
