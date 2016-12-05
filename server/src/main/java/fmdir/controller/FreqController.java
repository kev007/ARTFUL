package fmdir.controller;

import fmdir.dao.CountryFreqDao;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class FreqController {

    private final CountryFreqDao repository;

    static Logger log = Logger.getLogger(FreqController.class.getName());


    @Autowired
    public FreqController(CountryFreqDao repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/freqs", method = RequestMethod.GET)
    public String getFreqs(@RequestParam(value = "start", required = false, defaultValue = "1900") Integer start,
                           @RequestParam(value = "end", required = false, defaultValue = "2100") Integer end) {
        //TODO get real frequency data from database
        JSONObject response = new JSONObject();
        JSONArray countries = new JSONArray();
        response.put("countries", countries);

        List<Object[]> freqs = repository.findAllByYearBetween(start, end);

        freqs.forEach((record) -> {
            JSONObject currCountry = new JSONObject();
            currCountry.put("name", (String) record[0]);
            currCountry.put("frequency", (Long) record[1]);
            countries.put(currCountry);
        });
        return String.valueOf(response);
    }
}
