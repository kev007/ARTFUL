package fmdir.entities;

import javax.persistence.*;

@Entity
@Table(name = "country_freq")
@NamedQuery(name = "CountryFreq.findAllByYearBetween",
        query = "select cf.country, sum(cf.freq) as  freq from CountryFreq cf where cf.year BETWEEN ?1 AND ?2 GROUP BY cf.country")
public class CountryFreq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String country;
    private Integer freq;

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

    private Integer year;

    @Override
    public String toString() {
        return "CountryFreq{" +
                "country='" + country + '\'' +
                ", freq=" + freq +
                ", year=" + year +
                '}';
    }
}
