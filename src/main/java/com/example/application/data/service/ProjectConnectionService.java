package com.example.application.data.service;

import com.example.application.data.entity.CLTV_HW_Measures;
import com.example.application.data.entity.Project;
import com.example.application.data.entity.ProjectConnection;
import com.example.application.data.repository.ProjectConnectionRepository;
import com.example.application.data.repository.ProjectRepository;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProjectConnectionService {
    private final ProjectConnectionRepository repository;

    private JdbcTemplate jdbcTemplate;

    public ProjectConnectionService(ProjectConnectionRepository repository) {
        this.repository = repository;
    }

    public Optional<ProjectConnection> findByName(String name) {
        return repository.findByName(name);
    }

    public List<ProjectConnection> findAll() {
        return repository.findAll();
    }

    @Primary
    public DataSource getDataSource(String selectedDatabase) {

        System.out.println("selectedDatabase ............"+selectedDatabase);
        // Load connection details from the ProjectConnection entity
        Optional<ProjectConnection> projectConnection = repository.findByName(selectedDatabase);

        if (projectConnection.isPresent()) {
            System.out.println("jdbc:sqlserver://"+projectConnection.get().getHostname() + ";databaseName="+projectConnection.get().getDbName()+";encrypt=true;trustServerCertificate=true");
            System.out.println("gooo .........datasource");
            DataSource dataSource = DataSourceBuilder
                    .create()
                    .url("jdbc:sqlserver://"+projectConnection.get().getHostname() + ";databaseName="+projectConnection.get().getDbName()+";encrypt=true;trustServerCertificate=true")
                    .username(projectConnection.get().getUsername())
                    .password(projectConnection.get().getPassword())
                    .build();
            System.out.println("gooo .........datasource....done...."+dataSource);
            return dataSource;
        }

        throw new RuntimeException("Database connection not found: " + selectedDatabase);
    }

    public List<CLTV_HW_Measures> fetchDataFromDatabase(String selectedDatabase) {
        DataSource dataSource = getDataSource(selectedDatabase);

        System.out.println(dataSource+"gooo .........fetch");
        jdbcTemplate = new JdbcTemplate(dataSource);

        // Define your SQL query to fetch data
        String sqlQuery = "SELECT * FROM CLTV_HW_Measures"; // Replace with your actual table name

        // Create a RowMapper to map the query result to a CLTV_HW_Measures object
        RowMapper<CLTV_HW_Measures> rowMapper = (rs, rowNum) -> {
            CLTV_HW_Measures measure = new CLTV_HW_Measures();
            measure.setId(rs.getInt("id")); // Replace with your actual column names
            measure.setMonat_ID(rs.getInt("monat_id"));
            measure.setDevice(rs.getString("device"));
            measure.setMeasure_Name(rs.getString("measure_name"));
            measure.setChannel(rs.getString("channel"));
            measure.setValue(rs.getString("value"));
            return measure;
        };

        // Execute the query and map the results to CLTV_HW_Measures objects
        List<CLTV_HW_Measures> fetchedData = jdbcTemplate.query(sqlQuery, rowMapper);

        return fetchedData;
    }

    public String write2DB(List<CLTV_HW_Measures> data, String selectedDatabase) {
        DataSource dataSource = getDataSource(selectedDatabase);
        jdbcTemplate = new JdbcTemplate(dataSource);
        try {
            // Define your SQL update statement  id, monat_id, device, measure_name, channel, [value]
            String sqlUpdate = "UPDATE CLTV_HW_Measures SET monat_id = ?, device = ?, measure_name = ?, channel = ?, value = ? WHERE id = ?";

            // Loop through the data and update each record based on some condition (e.g., by ID)
            for (CLTV_HW_Measures item : data) {
                jdbcTemplate.update(
                        sqlUpdate,
                        item.getId(),
                        item.getMonat_ID(),
                        item.getDevice(),
                        item.getMeasure_Name(),
                        item.getChannel(),
                        item.getValue()
                );
            }

            return "ok"; // Update was successful
        } catch (Exception e) {
            e.printStackTrace();
            return "Error during update: " + e.getMessage();
        }
    }

}
