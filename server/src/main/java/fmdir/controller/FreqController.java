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
    public String getFreqs(@RequestParam(value = "exampleParam", defaultValue = "foo") String param) {
        Iterable<Freq> freqs = repository.findAll();
        return freqs.toString();
    }
}
