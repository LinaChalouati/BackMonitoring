package com.expo.grafana.service;

import com.expo.prometheus.service.PrometheusQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OverViewPanelsService {

    private final PrometheusQuery prometheusQuery;
    private final GrafanaClient grafanaClient;
    private final PanelClient panelClient;

    @Autowired
    public OverViewPanelsService(PrometheusQuery prometheusQuery, GrafanaClient grafanaClient, PanelClient panelClient) {
        this.prometheusQuery = prometheusQuery;
        this.grafanaClient = grafanaClient;
        this.panelClient = panelClient;
    }

    public void addPanel(String dashboardTitle, String ip, String port, String appType) throws Exception {
        String instance = ip + ":" + port;
        String deploymentWhere = prometheusQuery.getDeploymentWhere(ip, port);
       // String deploymentWhere="VM";
      //  String deploymentWhere="K8s";

        Map<String, String> expressions;
        ArrayList<String> chartTypes;

        if (deploymentWhere.equals("VM")) {
            if (appType.equals("Monolithic App")) {
                expressions = getExpressionsMonolithicVM(instance);
                chartTypes = getChartMonolithicVM();
            } else if (appType.contains("Microservice")) {
                expressions = getExpressionsMsVM(instance);
                chartTypes = getChartMsVM();
            } else {
                throw new IllegalArgumentException("Invalid app type");
            }
        } else {
            if (appType.equals("Monolithic App")) {
                expressions = getExpressionsMonolithicK8s(instance);
                chartTypes = getChartMonolithicK8s();
                System.out.println("hereeeeeeee");
            } else if (appType.contains("Microservice")) {
                expressions = getExpressionsMsK8s(instance);
                chartTypes = getChartMsK8s();
            } else {
                throw new IllegalArgumentException("Invalid app type");
            }
        }

        //List<String> panels = grafanaClient.getAllPanelIds(dashboardTitle);
        //if (!panels.isEmpty()) {
          //  Collections.sort(panels);
            int id =  1;

            int chartIndex = 0;
            for (Map.Entry<String, String> entry : expressions.entrySet()) {
                System.out.println("entry"+entry);
                System.out.println("expressions.entrySet()"+expressions.entrySet());

                String metricName = entry.getKey();
                String metricExpression = entry.getValue();
                String chartType = chartTypes.get(chartIndex);
                chartIndex++;

                panelClient.addPanel(dashboardTitle, metricName, metricExpression, chartType, id, 0);
                System.out.println("added");
               /* if (metricExpression.contains("time")) {
                    panelClient.setFormat(dashboardTitle, id);
                    System.out.println("ok mrigl");
                }
*/
                id++;

        }
    }

    private Map<String, String> getExpressionsMonolithicVM(String instance) {
        Map<String, String> expressions = new HashMap<>();
        expressions.put("CPU Usage Over Time", String.format("avg(vm_cpu_load_x100_window_1h{instance=\"%s\"})", instance));
        expressions.put("Memory Usage Over Time", String.format("avg(vm_memory_total_used_window_1h{instance=\"%s\"})", instance));
        expressions.put("Network Traffic", String.format("sum(http_requests_total{instance=\"%s\"})", instance));
        expressions.put("Disk Usage Distribution", String.format("avg(cache_size{instance=\"%s\"})", instance));
        return expressions;
    }

    private ArrayList<String> getChartMonolithicVM() {
        return new ArrayList<>(Arrays.asList("timeseries", "timeseries", "barchart", "piechart"));
    }

    private Map<String, String> getExpressionsMsVM(String instance) {
        Map<String, String> expressions = new HashMap<>();
        expressions.put("CPU Usage Over Time", String.format("sum(http_requests_total{instance=\"%s\"}) by (service)", instance));
        expressions.put("Memory Usage Over Time", String.format("avg(vm_memory_total_used_window_1h{instance=\"%s\"}) by (service)", instance));
        expressions.put("Network Traffic", String.format("sum(http_server_requests_seconds_count{instance=\"%s\"}) by (service)", instance));
        expressions.put("Disk Usage Distribution", String.format("avg(vm_disk_usage_percentage{instance=\"%s\"})", instance));
        expressions.put("HTTP Requests", String.format("sum(http_requests_total{instance=\"%s\"})", instance));
        return expressions;
    }

    private ArrayList<String> getChartMsVM() {
        return new ArrayList<>(Arrays.asList("timeseries", "timeseries", "barchart", "piechart", "timeseries"));
    }

    private Map<String, String> getExpressionsMonolithicK8s(String instance) {
        Map<String, String> expressions = new HashMap<>();
        expressions.put("CPU Usage Over Time", String.format("sum(kube_pod_container_resource_requests_cpu_cores{namespace=\"%s\"})", instance));
        expressions.put("Memory Usage Over Time", String.format("sum(kube_pod_container_resource_requests_memory_bytes{namespace=\"%s\"})", instance));
        expressions.put("Network Traffic", String.format("sum(kube_pod_container_network_transmit_bytes_total{namespace=\"%s\"})", instance));
        expressions.put("Disk Usage Distribution", String.format("sum(node_filesystem_size{nodename=\"%s\"}) - sum(node_filesystem_free{nodename=\"%s\"})", instance, instance));
        expressions.put("HTTP Requests", String.format("http_requests_total{instance=\"%s\"}", instance));
        return expressions;
    }

    private ArrayList<String> getChartMonolithicK8s() {
        return new ArrayList<>(Arrays.asList("timeseries", "timeseries", "barchart", "piechart", "timeseries"));
    }

    private Map<String, String> getExpressionsMsK8s(String instance) {
        Map<String, String> expressions = new HashMap<>();
        expressions.put("CPU Usage Over Time", String.format("sum(http_requests_total{deployment=\"%s\"})", instance));
        expressions.put("Memory Usage Over Time", String.format("avg(container_memory_usage_bytes{deployment=\"%s\"})", instance));
        expressions.put("Network Traffic", String.format("sum(http_server_requests_seconds_count{deployment=\"%s\"})", instance));
        expressions.put("Disk Usage Distribution", String.format("sum(node_filesystem_size{nodename=~\"%s\"}) - sum(node_filesystem_free{nodename=~\"%s\"})", instance, instance));
        expressions.put("HTTP Requests", String.format("sum(rate(http_requests_total{instance=\"%s\"}[1m]))", instance));
        return expressions;
    }

    private ArrayList<String> getChartMsK8s() {
        return new ArrayList<>(Arrays.asList("timeseries", "timeseries", "barchart", "piechart", "timeseries"));
    }
}
