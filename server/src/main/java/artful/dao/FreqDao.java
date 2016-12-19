package artful.dao;


import artful.entities.Freq;
import org.springframework.data.repository.CrudRepository;

public interface FreqDao extends CrudRepository<Freq, Long> {
}
