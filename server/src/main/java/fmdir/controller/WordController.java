package fmdir.controller;

import fmdir.dao.WordDao;
import fmdir.entities.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class WordController {

    private final WordDao repository;

    @Autowired
    public WordController(WordDao repository) {
        this.repository = repository;
    }

    @RequestMapping("/words")
    public String getWords(@RequestParam(value = "exampleParam", defaultValue = "foo") String param) {
        Iterable<Word> words = repository.findAll();
        return words.toString();
    }
}
