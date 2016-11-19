package fmdir.dao;


import fmdir.entities.Word;
import org.springframework.data.repository.CrudRepository;

public interface WordDao extends CrudRepository<Word, Long> {
}
