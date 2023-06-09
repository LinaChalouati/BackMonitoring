package com.expo.prometheus.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VmQuery {


    private Map<String, String> query;


    public Map<String, String> getQuery() {
        return query;
    }

    public VmQuery() {
        query = getTheMap(constructTheIndice(),constructTheExpressions());

    }


    public ArrayList<String> constructTheIndice(){
        ArrayList<String> indice = new ArrayList<>(List.of(
                "cachesize",
                "hikaricpconnectionstimeout",
                "jdbcconnectionsactive",
                "processfilesopenfiles",
                "hikaricpconnections",
                "vmmemototalused",
                "vmmemopoolsmetaspace",
                "vmcpuload"




        ));
        return indice;

    }
    public ArrayList<String> constructTheExpressions(){
        ArrayList<String> expr = new ArrayList<>(List.of(
                "cache_size{instance=\"%s\"}",
                "hikaricp_connections_timeout_total{instance=\"%s\"}",
                "jdbc_connections_active{instance=\"%s\"}",
                "process_files_open_files{instance=\"%s\"}",
                "hikaricp_connections{instance=\"%s\"}", //cbon
                "avg(vm_memory_total_used_window_1h{instance=\"%s\"})", //no
                "avg(vm_memory_pools_Metaspace_used_window_1h{instance=\"%s\"})",//no
                "avg(vm_cpu_load_x100_window_1h{instance=\"%s\"})"//no

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
