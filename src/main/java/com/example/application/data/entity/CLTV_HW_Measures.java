package com.example.application.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(schema = "dbo", name = "CLTV_HW_MEASURES")
public class CLTV_HW_Measures {


    @Id
    private Long id;

    @NotEmpty
    private Integer Monat_ID ;

    @NotEmpty
    private String Device = "";

    @NotEmpty
    private String Measure_Name = "";

    @NotEmpty
    private String Channel = "";
    @NotEmpty
    private Long Value;

    public Integer getMonat_ID() {
        return Monat_ID;
    }

    public void setMonat_ID(Integer monat_ID) {
        Monat_ID = monat_ID;
    }

    public String getDevice() {
        return Device;
    }

    public void setDevice(String device) {
        Device = device;
    }

    public String getMeasure_Name() {
        return Measure_Name;
    }

    public void setMeasure_Name(String measure_Name) {
        Measure_Name = measure_Name;
    }

    public String getChannel() {
        return Channel;
    }

    public void setChannel(String channel) {
        Channel = channel;
    }

    public Long getValue() {
        return Value;
    }

    public void setValue(Long value) {
        Value = value;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
