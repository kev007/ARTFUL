package artful.service;

import artful.dao.FreqDao;
import artful.entities.Freq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class FreqServiceImpl implements FreqService {

    private final FreqDao freqDao;

    @Autowired
    public FreqServiceImpl(FreqDao freqDao) {
        this.freqDao = freqDao;
    }


    public List<Freq> getAllFreqs() {
        return (List<Freq>) freqDao.findAll();
    }

}
