package fmdir.controller;

import fmdir.dao.FreqDao;
import fmdir.entities.Freq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class FreqController {

    private final FreqDao repository;

    @Autowired
    public FreqController(FreqDao repository) {
        this.repository = repository;
    }

    @RequestMapping("/freqs")
    public Iterable<Freq> getFreqs(@RequestParam(value = "exampleParam", defaultValue = "foo") String param) {
        return repository.findAll();
    }
}
