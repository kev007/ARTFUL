package fmdir.service;

import fmdir.dao.FreqDao;
import fmdir.entities.Freq;
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
