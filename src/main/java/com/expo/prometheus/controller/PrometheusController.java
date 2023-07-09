package com.expo.prometheus.controller;


import com.expo.prometheus.service.PrometheusConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prometheus")

@CrossOrigin(origins = "*")
public class PrometheusController {
    @Autowired
    private PrometheusConfig prometheusConfig;

    @PostMapping("/datasource")
    public void addDatasource(@RequestParam (value ="name")String name ,
                              @RequestParam(value="ipaddr") String ipAddr) throws JsonProcessingException {

        prometheusConfig.addPrometheus(name,ipAddr);
    }
    @PostMapping("/datasourceId")
    public String getDataSourceId(@RequestParam (value ="name")String name
                            ) throws JsonProcessingException {

        return prometheusConfig.getDataSourceIdByName(name);
    }
    @PostMapping("/datasourceUid")
    public String getDataSourceUid(@RequestParam (value ="name")String name
    ) throws JsonProcessingException {

        return prometheusConfig.getDataSourceUidByName(name);
    }
    @PostMapping("/getdatasource")
    public JsonNode getDataSourceByName(@RequestParam (value ="name")String name
    ) throws JsonProcessingException {

        return prometheusConfig.getDataSourceByName(name);
    }


    //fiha mochkla matemchich
    @PostMapping("/modifydatasource")
    public void modifyDataSource(@RequestParam (value ="name")String name,
                                     @RequestParam(value = "newurl", required = false) String newurl,
                                     @RequestParam(value = "version", required = false) String version,
                                     @RequestParam(value = "newname", required = false) String newname) throws JsonProcessingException {

         prometheusConfig.modifyDataSource(name,newurl,version,newname);
    }
    @PostMapping("/deletedatasource")
    public void deleteDatasource(@RequestParam (value ="name")String name
                                 )throws JsonProcessingException {

         prometheusConfig.deleteDataSource(name);
    }
    @PostMapping("/getdatasourcehealth")
    public void getDataSourceHealth(@RequestParam (value ="name")String name
    )throws JsonProcessingException {

        prometheusConfig.getDataSourceHealth(name);
    }
}
