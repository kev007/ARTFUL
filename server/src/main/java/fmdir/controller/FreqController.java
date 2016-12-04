package fmdir.controller;

import fmdir.dao.FreqDao;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class FreqController {

    private final FreqDao repository;

    static Logger log = Logger.getLogger(FreqController.class.getName());


    @Autowired
    public FreqController(FreqDao repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/freqs", method = RequestMethod.GET)
    public String getFreqs(@RequestParam(value = "start", required = false, defaultValue = "1900") Integer start,
                           @RequestParam(value = "end", required = false, defaultValue = "2100") Integer end) {
        //TODO get real frequency data from database
        JSONObject response = new JSONObject();
        JSONArray countries = new JSONArray();
        JSONObject germany = new JSONObject();
        germany.put("name", "Germany");
        germany.put("frequency", 50);
        countries.put(germany);
        JSONObject france = new JSONObject();
        france.put("name", "France");
        france.put("frequency", 90);
        countries.put(france);
        JSONObject poland = new JSONObject();
        poland.put("name", "Poland");
        poland.put("frequency", 200);
        countries.put(poland);

        response.put("countries", countries);
        return String.valueOf(response);
    }
}
