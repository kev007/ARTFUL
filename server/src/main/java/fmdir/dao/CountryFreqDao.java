package fmdir.dao;


import fmdir.entities.CountryFreq;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface CountryFreqDao extends CrudRepository<CountryFreq, Long> {

    List<Object[]> findAllByYearBetween(Integer start, Integer end);

}
