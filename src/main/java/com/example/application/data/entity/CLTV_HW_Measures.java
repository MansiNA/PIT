package com.example.application.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(schema = "dbo", name = "CLTV_HW_MEASURES")
public class CLTV_HW_Measures {


    @Id
    private Integer id;

    @NotEmpty
    private Integer monat_ID ;

    @NotEmpty
    private String device = "";

    @NotEmpty
    private String measure_Name = "";

    @NotEmpty
    private String channel = "";
    @NotEmpty
    private String value;

    public CLTV_HW_Measures(Integer id, Integer monat_ID, String device, String measure_Name, String channel, String value) {
        this.id = id;
        this.monat_ID = monat_ID;
        this.device = device;
        this.measure_Name = measure_Name;
        this.channel = channel;
        this.value = value;
    }

    public CLTV_HW_Measures() {

    }

    public Integer getMonat_ID() {
        return monat_ID;
    }

    public void setMonat_ID(Integer monat_ID) {
        monat_ID = monat_ID;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        device = device;
    }

    public String getMeasure_Name() {
        return measure_Name;
    }

    public void setMeasure_Name(String measure_Name) {
        measure_Name = measure_Name;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        channel = channel;
    }

    public String getValue() {
        //return value.toString();
        return value;
    }

    public void setValue(String value) {
  //      value = String.valueOf(Long.valueOf(value));
        value=value;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}
