package com.example.application.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;

import java.sql.Date;

@Entity
@Table(schema = "dbo", name = "job_status")
public class AgentJobs {


    private String name;

    private Date date_created;

    private Integer step_id;

    private String step_name;

    private String next_time_run;

    private String time_run;

    private String job_status;

    private String result;

    @Id
    private String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate_created() {
        return date_created;
    }

    public void setDate_created(Date date_created) {
        this.date_created = date_created;
    }

    public Integer getStep_id() {
        return step_id;
    }

    public void setStep_id(Integer step_id) {
        this.step_id = step_id;
    }

    public String getStep_name() {
        return step_name;
    }

    public void setStep_name(String step_name) {
        this.step_name = step_name;
    }

    public String getNext_time_run() {
        return next_time_run;
    }

    public void setNext_time_run(String next_time_run) {
        this.next_time_run = next_time_run;
    }

    public String getTime_run() {
        return time_run;
    }

    public void setTime_run(String time_run) {
        this.time_run = time_run;
    }

    public String getJob_status() {
        return job_status;
    }

    public void setJob_status(String job_status) {
        this.job_status = job_status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
