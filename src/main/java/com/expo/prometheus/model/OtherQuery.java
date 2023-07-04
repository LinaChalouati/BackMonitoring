package com.expo.prometheus.model;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherQuery {

    private Map<String, String> query;


    public Map<String, String> getQuery() {
        return query;
    }

    public OtherQuery() {
        query = getTheMap(constructTheIndice(),constructTheExpressions());

    }


    public ArrayList<String> constructTheIndice(){
        ArrayList<String> indice = new ArrayList<>(List.of(
                                                                "httprequests", //ok
                                                                "errorrates",//no
                                                                "starttime",//ok
                                                                "uptime",//ok
                                                                "status",//ok
                                                                "buildstatus",//no
                                                                "builderrors",//no
                                                                "buildtime",//no
                                                                "coverage",//no
                                                                "dup",//no
                                                                "vun",//ok
                                                                "bugs",//ok
                                                                "smells",//ok
                                                                "jdbcactive",//ok
                                                                "jdbcidle",//ok
                                                                "jdbcmin",//ok
                                                                "jdbcmax",//ok
                                                                "junit",//no
                                                                "httpavg" ,//ok
                                                                "heapmemo",
                                                                "nonheap",
                                                                "garbage",
                                                                "pausedurations",
                                                                "jvmtotal",
                                                                "cpujvm"



        ));
        return indice;

    }
    public ArrayList<String> constructTheExpressions(){
        ArrayList<String> expr = new ArrayList<>(List.of(
                "http_requests_total{instance=\"%s\"}",
                "errorrates",
                "process_start_time_seconds{instance=\"%s\"}*1000",
                "process_uptime_seconds{instance=\"%s\"}",
                "up{instance=\"%s\"}", //cbon
                "buildstatus", //no
                "builderrors",//no
                "buildtime",//no
                "sonarqube_coverage{key=\"%s\"}", //no
                "dup", //no
                "sonarqube_vulnerabilities{key=\"%s\"}", //no
                "sonarqube_bugs{key=\"%s\"}", //no
                "sonarqube_code_smells{key=\"%s\"}", //no
                "jdbc_connections_active{instance=\"%s\"}",
                "jdbc_connections_idle{instance=\"%s\"}",
                "jdbc_connections_min{instance=\"%s\"}",
                "jdbc_connections_max{instance=\"%s\"}",
                "junit", //no
                "sum(irate(http_server_requests_seconds_sum{instance=\"%s\"}[1m])) / sum(irate(http_server_requests_seconds_count{instance=\"%s\"}[1m]))",
                "jvm_memory_used_bytes{instance=\"%s\"}",
                "sum(jvm_memory_used_bytes{ instance=\"%s\",area=nonheap})*100/sum(jvm_memory_max_bytes{ instance=\"%s\",area=nonheap})",
                "rate(jvm_gc_pause_seconds_count{instance=\"%s\"}[1m])",
                "rate(jvm_gc_pause_seconds_sum{instance=\"%s\"}[1m])/rate(jvm_gc_pause_seconds_count{instance=\"%s\"}[1m])",
                "sum(jvm_memory_used_bytes{instance=\"%s\"})",
                "system_cpu_usage{instance=\"%s\"}"


                ));
        return expr;

    }
    public Map<String,String> getTheMap(ArrayList<String> indice,ArrayList<String> expr){
        Map<String,String> indice_expr=new HashMap<>();
        for (int i=0;i<Math.min(indice.size(),expr.size());i++){
            indice_expr.put(indice.get(i),expr.get(i));
        }
        return indice_expr;
    }

}
