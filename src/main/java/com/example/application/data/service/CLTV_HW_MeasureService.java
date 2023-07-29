package com.example.application.data.service;

import com.example.application.data.entity.CLTV_HW_Measures;
import com.example.application.data.entity.ProductHierarchie;
import com.example.application.data.repository.CLTV_HW_MeasuresRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CLTV_HW_MeasureService {

    private final CLTV_HW_MeasuresRepository cLTVHwMeasuresRepository;
    @Autowired
    private JdbcTemplate template;

    public CLTV_HW_MeasureService(CLTV_HW_MeasuresRepository cLTV_HW_MeasuresRepository) {

        cLTVHwMeasuresRepository = cLTV_HW_MeasuresRepository;
    }

    public List<CLTV_HW_Measures> findAllProducts(String stringFilter) {
        if (stringFilter == null || stringFilter.isEmpty()) {


            return cLTVHwMeasuresRepository.findAll();
        } else {
            return cLTVHwMeasuresRepository.search(stringFilter);
        }
    }

    public List<CLTV_HW_Measures> findAllProductsbyMonat(String stringFilter) {

        if (stringFilter == null || stringFilter.isEmpty()) {
            return cLTVHwMeasuresRepository.findAll();
        }

        Integer intFilter=Integer.parseInt(stringFilter);

        if (intFilter == null || intFilter==0) {
            System.out.println("Filter ist null!!");
            return cLTVHwMeasuresRepository.findAll();
        } else {
            return cLTVHwMeasuresRepository.searchMonat(intFilter);
        }
    }

}
