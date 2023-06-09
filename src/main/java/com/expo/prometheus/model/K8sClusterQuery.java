package com.expo.prometheus.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class K8sClusterQuery {




    private Map<String, String> query;

    public Map<String, String> getQuery() {
        return query;
    }





    public K8sClusterQuery() {
        query = getTheMap(constructTheIndice(),constructTheExpressions());
       // System.out.println(query);
    }

    public ArrayList<String> constructTheIndice(){
        ArrayList<String> indice = new ArrayList<>(List.of(
                "clusterpodusage", //ok
                "clustercpuusage",//ok
                "clustermemoryusage",//ok
                "clusterdiskusage",//ok
                "clusterpodcapacity",//ok
                "clustercpucapacity",//no
                "clustermemorycapacity",//no
                "clusterdiskcapacity",//no
                "replicauptodate",//no
                "replica",//no
                "replicaupdated",//no
                "replicaunavailable",//no
                "nbnodes",//no
                "nodesoutofdisk",//ok
                "nodesunavailable",//ok
                "podsrunning",//ok
                "podspending",//ok
                "podsfailed",//no
                "podsuccess" ,//ok
                "podsunkown",
                "containerrun",
                "containerwait",
                "containertermn",
                "containerrestart",
                "containercpu",
                "containermemo",
                "jobsuccess",
                "jobsfailed"


        ));
        return indice;

    }

    public ArrayList<String> constructTheExpressions(){
        ArrayList<String> expr = new ArrayList<>(List.of(
                "sum(kube_pod_info{node=~\"$node\"}) / sum(kube_node_status_allocatable_pod s{node=~\".*\"})",
                "sum(kube_pod_container_resource_requests_cpu_cores{node=~\"$node\"}) / sum(kube_node_status_allocatable_cpu_cores{node=~\"$node\"})",
                "sum(kube_pod_container_resource_requests_memory_bytes{node=~\"$node\"}) / sum(kube_node_status_allocatable_memory_bytes{node=~\"$node\"})",
                "(sum (node_filesystem_size{nodename=~\"$node\"}) - sum (node_filesystem_free{nodename=~\"$node\"})) / sum (node_filesystem_size{nodename=~\"$node\"})\n",
                "sum(kube_node_status_allocatable_pods{node=~\"$node\"})",
                "sum(kube_node_status_capacity_cpu_cores{node=~\"$node\"})",
                "sum(kube_node_status_allocatable_memory_bytes{node=~\"$node\"})",
                "sum(node_filesystem_size{nodename=~\"$node\"}) - sum(node_filesystem_free{nodename=~\"$node\"})\n",
                "kube_deployment_status_replicas{namespace=~\"$namespace\"}",
                "sum(kube_deployment_status_replicas{namespace=~\"$namespace\"})",
                "sum(kube_deployment_status_replicas_updated{namespace=~\"$namespace\"})",
                "sum(kube_deployment_status_replicas_unavailable{namespace=~\"$namespace\"})",
                "sum(kube_node_info{node=~\"$node\"})", //no
                "sum(kube_node_status_condition{condition=\"OutOfDisk\", node=~\"$node\", status=\"true\"})\n",
                "sum(kube_node_spec_unschedulable{node=~\"$node\"})",
                "sum(kube_node_info{node=~\"$node\"})",
                "sum(kube_pod_status_phase{namespace=~\"$namespace\", phase=\"Pending\"})\n",
                "sum(kube_pod_status_phase{namespace=~\"$namespace\", phase=\"Failed\"})", //no
                "sum(irate(http_server_requests_seconds_sum{ }[1m])) / sum(irate(http_server_requests_seconds_count{}[1m]))",
                "sum(kube_pod_status_phase{namespace=~\"$namespace\", phase=\"Succeeded\"})",
                "sum(kube_pod_status_phase{namespace=~\"$namespace\", phase=\"Unknown\"})\n",
                "sum(kube_pod_container_status_running{namespace=~\"$namespace\"})",
                "sum(kube_pod_container_status_waiting{namespace=~\"$namespace\"})",
                "sum(kube_pod_container_status_terminated{namespace=~\"$namespace\"})",
                "sum(delta(kube_pod_container_status_restarts{namespace=\"kube-system\"}[30m]))",
                "sum(kube_pod_container_resource_requests_cpu_cores{namespace=~\"$namespace\", node=~\"$node\"})",
                "sum(kube_pod_container_resource_requests_memory_bytes{namespace=~\"$namespace\", node=~\"$node\"})",
                "sum(kube_job_status_succeeded{namespace=~\"$namespace\"})",
                "sum(kube_job_status_failed{namespace=~\"$namespace\"})"



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
