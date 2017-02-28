package artful.entities;

import javax.persistence.*;

@Entity
@Table(name = "country_freq")
@NamedQueries({
        @NamedQuery(name = "CountryFreq.findAllOutgoingByYearBetween",
                query = "SELECT cf.country, SUM(cf.freq_ingoing) AS freq_ingoing, SUM(cf.freq_outgoing) AS freq_outgoing, avg(cf.avgCorporaSizeIngoing) FROM CountryFreq cf WHERE cf.year BETWEEN ?1 AND ?2 GROUP BY cf.country"),
        @NamedQuery(name = "CountryFreq.getAvgCorpusSizes",
                query = "SELECT lang, avg(size) FROM Corpora WHERE year BETWEEN ?1 AND ?2 GROUP BY lang"),
})
public class CountryFreq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String country;
    private Integer freq_ingoing;
    private Integer freq_outgoing;

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

    public Integer getFreq_ingoing() {
        return freq_ingoing;
    }

    public void setFreq_ingoing(Integer freq_ingoing) {
        this.freq_ingoing = freq_ingoing;
    }

    public Integer getFreq_outgoing() {
        return freq_outgoing;
    }

    public void setFreq_outgoing(Integer freq_outgoing) {
        this.freq_outgoing = freq_outgoing;
    }

    @Override
    public String toString() {
        return "CountryFreq{" +
                "id=" + id +
                ", country='" + country + '\'' +
                ", freq_ingoing=" + freq_ingoing +
                ", freq_outgoing=" + freq_outgoing +
                ", avgCorporaSizeIngoing=" + avgCorporaSizeIngoing +
                ", avgCorporaSizeOutgoing=" + avgCorporaSizeOutgoing +
                ", year=" + year +
                '}';
    }
}
