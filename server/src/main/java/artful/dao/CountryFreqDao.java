package artful.dao;


import artful.entities.CountryFreq;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface CountryFreqDao extends CrudRepository<CountryFreq, Long> {

    List<Object[]> findAllOutgoingByYearBetween(Integer start, Integer end);

    List<Object[]> getAvgCorpusSizes(Integer start, Integer end);

}
