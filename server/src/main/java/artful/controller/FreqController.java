package artful.controller;

import artful.dao.CountryFreqDao;
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
    
    
    @RequestMapping(value = "/topTen", method = RequestMethod.GET)
    public String getFreqs(@RequestParam(value = "country") String country,
    					   @RequestParam(value = "start") Integer start,
                           @RequestParam(value = "end") Integer end) {
    	List<Object[]> topTen = repository.findTopTenMentioning(country, start, end);
        JSONObject response = new JSONObject();
        JSONArray countries = new JSONArray();
        response.put("countries", countries);
    	topTen.forEach((record) -> {
    		if(countries.length() <= 10){
            JSONObject currCountry = new JSONObject();
            currCountry.put("name", (String) record[0]);
            currCountry.put("frequency", (Long) record[1]);
            countries.put(currCountry);
    		}
    	});
    	return String.valueOf(response);
    }

    @RequestMapping(value = "/freqs", method = RequestMethod.GET)
    public String getFreqs(@RequestParam(value = "start", required = false, defaultValue = "1900") Integer start,
                           @RequestParam(value = "end", required = false, defaultValue = "2100") Integer end) {
        JSONObject response = new JSONObject();
        JSONArray countries = new JSONArray();
        response.put("countries", countries);

        List<Object[]> outgoing = repository.findAllOutgoingByYearBetween(start, end);
        final Integer[] maxCorporaSize = {0, 0};
        outgoing.forEach((record) -> {
            JSONObject currCountry = new JSONObject();
            String country = (String) record[0];
            currCountry.put("name", country);
            currCountry.put("outgoing frequency", (Long) record[1]);
            currCountry.put("ingoing frequency", (Long) record[2]);
            Integer currCorporaSizeIngoing = (Integer) record[3];
            currCountry.put("avgCorporaSizeIngoing", currCorporaSizeIngoing);
            if(currCorporaSizeIngoing > maxCorporaSize[0]){
                maxCorporaSize[0] = currCorporaSizeIngoing;
            }
            Integer currCorporaSizeOutgoing = (Integer) record[4];
            currCountry.put("avgCorporaSizeOutgoing", currCorporaSizeOutgoing);
            if(currCorporaSizeOutgoing > maxCorporaSize[1]){
                maxCorporaSize[1] = currCorporaSizeOutgoing;
            }
            countries.put(currCountry);
        });
        response.put("max corpora size ingoing", maxCorporaSize[0]);
        response.put("max corpora size outgoing", maxCorporaSize[1]);
        return String.valueOf(response);
    }
}
