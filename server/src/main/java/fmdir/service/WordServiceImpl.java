package fmdir.service;

import fmdir.dao.WordDao;
import fmdir.entities.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class WordServiceImpl implements WordService {

    private final WordDao wordDao;

    @Autowired
    public WordServiceImpl(WordDao wordDao) {
        this.wordDao = wordDao;
    }


    public List<Word> getAllWords() {
        return (List<Word>) wordDao.findAll();
    }

}
