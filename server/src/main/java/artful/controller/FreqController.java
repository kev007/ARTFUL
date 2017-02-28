package artful.controller;

import artful.dao.CountryFreqDao;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


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
                           @RequestParam(value = "end", required = false, defaultValue = "2100") Integer end) throws IOException {
        JSONObject response = new JSONObject();
        JSONArray countries = new JSONArray();
        response.put("countries", countries);

        List<Object[]> outgoing = repository.findAllOutgoingByYearBetween(start, end);
        List<Object[]> avgCorpusSizes = repository.getAvgCorpusSizes(start, end);
        final Double[] maxCorporaSize = {0.0};
        HashMap<String, Double> avgCorpusSizesMap = new HashMap<>();
        for (Object[] corpus : avgCorpusSizes) {
            avgCorpusSizesMap.put((String) corpus[0], (Double) corpus[1]);
        }
        Resource resource = new ClassPathResource("language-country-mapping.properties");
        Properties props = PropertiesLoaderUtils.loadProperties(resource);
        outgoing.forEach((record) -> {
            JSONObject currCountry = new JSONObject();
            String country = (String) record[0];
            currCountry.put("name", country);
            currCountry.put("ingoing frequency", (Long) record[1]);
            currCountry.put("outgoing frequency", (Long) record[2]);
            Double currCorporaSizeIngoing = (Double) record[3];
            String language = props.getProperty(country.toLowerCase());
            currCountry.put("avgCorporaSizeOutgoing", avgCorpusSizesMap.get(language));
            if (currCorporaSizeIngoing > maxCorporaSize[0]) {
                maxCorporaSize[0] = currCorporaSizeIngoing;
            }
           // Integer currSumCorporaSizeOutgoing = (Integer) record[4];
            currCountry.put("avgCorporaSizeIngoing", currCorporaSizeIngoing);
            countries.put(currCountry);
        });
        response.put("max corpora size", maxCorporaSize[0]);
        return String.valueOf(response);
    }
}
