package com.example.application.data.repository;

import com.example.application.data.entity.CLTV_HW_Measures;
import com.example.application.data.entity.ProductHierarchie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CLTV_HW_MeasuresRepository  extends JpaRepository<CLTV_HW_Measures, Long> {
    @Query("select c from CLTV_HW_Measures c " +
            "where lower(c.Device) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(c.Measure_Name) like lower(concat('%', :searchTerm, '%'))")


    List<CLTV_HW_Measures> search(@Param("searchTerm") String searchTerm);
}
