package artful.dao;


import artful.entities.CountryFreq;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface CountryFreqDao extends CrudRepository<CountryFreq, Long> {

    List<Object[]> findAllByYearBetween(Integer start, Integer end);

    List<Object[]> findTopTenMentioning(String country, Integer start, Integer end);

}
