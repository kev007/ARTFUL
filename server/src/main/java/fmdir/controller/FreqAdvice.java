package fmdir.controller;

import fmdir.dao.FreqDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class FreqAdvice {

    private final FreqDao repository;

    @Autowired
    public FreqAdvice(FreqDao repository) {
        this.repository = repository;
    }

    @ModelAttribute("message")
    public String message() {
        return String.format("There are %d freq entries.",repository.count() );
    }

}
