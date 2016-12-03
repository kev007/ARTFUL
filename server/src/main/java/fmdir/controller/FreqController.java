package fmdir.controller;

import fmdir.dao.FreqDao;
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

    @Autowired
    public FreqController(FreqDao repository) {
        this.repository = repository;
    }

    @RequestMapping(value = "/freqs", method = RequestMethod.GET)
    public String getFreqs(@RequestParam(value = "exampleParam", defaultValue = "foo") String param) {
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
