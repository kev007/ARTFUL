package fmdir.dao;


import fmdir.entities.Translation;
import org.springframework.data.repository.CrudRepository;

public interface TranslationDao extends CrudRepository<Translation, Long> {
}
