package fmdir.service;

import fmdir.dao.TranslationDao;
import fmdir.entities.Translation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TranslationServiceImpl implements TranslationService {

    private final TranslationDao translationDao;

    @Autowired
    public TranslationServiceImpl(TranslationDao translationDao) {
        this.translationDao = translationDao;
    }


    public List<Translation> getAllTranslations() {
        return (List<Translation>) translationDao.findAll();
    }

}
