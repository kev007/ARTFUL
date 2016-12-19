package artful.controller;

import artful.dao.TranslationDao;
import artful.entities.Translation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class TranslationController {

    private final TranslationDao repository;

    @Autowired
    public TranslationController(TranslationDao repository) {
        this.repository = repository;
    }

    @RequestMapping("/translations")
    public String getWords(@RequestParam(value = "exampleParam", defaultValue = "foo") String param) {
        Iterable<Translation> translations = repository.findAll();
        return translations.toString();
    }
}
