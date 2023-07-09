package com.expo.healthcheck.controller;

import com.expo.healthcheck.service.HealthCheckService;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.RestartContainerCmd;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DefaultDockerClientConfig;

import static com.google.common.base.Predicates.equalTo;

@RestController
@RequestMapping("/api/healthcheck")
@CrossOrigin("*")
public class HealthCheck {

        @GetMapping("/health") //json
        public ResponseEntity<String> healthCheck() {
        String prometheusUrl = "http://localhost:9090";
        String grafanaUrl = "http://localhost:3000";


        boolean isGrafanaUp = isGrafanaServiceUp(grafanaUrl);
        boolean isPrometheusUp = isPrometheusServiceUp(prometheusUrl);

        if (isGrafanaUp && isPrometheusUp) {
            return new ResponseEntity<>("Grafana and Prometheus are up", HttpStatus.OK);
        }
        if (isGrafanaUp && !isPrometheusUp) {
            return new ResponseEntity<>("Prometheus down", HttpStatus.SERVICE_UNAVAILABLE);
        }
        if (!isGrafanaUp && isPrometheusUp) {
            return new ResponseEntity<>("Grafana down", HttpStatus.SERVICE_UNAVAILABLE);
        } else {
            return new ResponseEntity<>("Grafana and Prometheus down", HttpStatus.SERVICE_UNAVAILABLE);
        }

    }


    private boolean isGrafanaServiceUp(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            httpClient.execute(request);
            System.out.println("graf" + httpClient.execute(request));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isPrometheusServiceUp(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            httpClient.execute(request);
            System.out.println(httpClient.execute(request));

            return true;
        } catch (Exception e) {
            return false;
        }
    }


    @GetMapping("/prometheusexists")
    public Boolean checkPrometheusExists() {
        String image = "prom/prometheus";
        String command = "docker image inspect " + image;

        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println(line);
            }

            if (output.toString().equals("[]\n")) {
                // return "Prometheus image does not exist";
                return false;
            } else {
                //return "Prometheus image exists";
                return true;
            }
        } catch (IOException e) {
            //  return "Error checking Prometheus image: " + e.getMessage();
            return false;
        }
    }

    @GetMapping("/grafanaexists")
    public Boolean checkGrafanaExists() {
        String image = "prom/prometheus";
        String command = "docker image inspect " + image;
        //  String command="docker ps --a";

        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println(line);

            }

            if (output.toString().equals("[]\n")) {
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    @GetMapping("/prometheusup") //nzid -
    public String checkPrometheusUp() {
        String command = "docker ps";

        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                System.out.println(line);

            }
            //  System.out.println(output.toString().contains("prometheus"));
            // System.out.println(output.toString().contains("grafana"));


            if (output.toString().contains("prometheus") && output.toString().contains("Up")) {
                return "Prometheus Container UP";
            } else {
                if (checkPrometheusExists()) {
                    return "Prometheus Container DOWN ";
                } else {
                    return "Prometheus Container Does Not exist ";
                }

            }
        } catch (IOException e) {
            return "Error checking Prometheus Container: " + e.getMessage();
        }

    }

    @GetMapping("/grafanaup")
    public String checkGrafanaUp() {
        String command = "docker ps";

        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                //System.out.println(line);

            }
            //  System.out.println(output.toString().contains("prometheus"));
            // System.out.println(output.toString().contains("grafana"));


            if (output.toString().contains("grafana") && output.toString().contains("Up")) {
                return "Grafana Container UP";
            } else {
                if (checkGrafanaExists()) {
                    return "Grafana Container DOWN ";
                } else {
                    return "Grafana Container Does Not exist ";
                }

            }
        } catch (IOException e) {
            return "Error checking Grafana Container: " + e.getMessage();
        }

    }

    /*    @GetMapping("/imageid")
    public String GetImageId(String image) {
        String command="docker ps";

        try {
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                //System.out.println(line);

            }
            //  System.out.println(output.toString().contains("prometheus"));
            // System.out.println(output.toString().contains("grafana"));


            if (output.toString().contains(image)) {

                return "";
            }
        }
        catch (IOException e) {
            return "Error checking Grafana image: " + e.getMessage();
        }

    }*/

    /*  public static void restartContainer(String containerName) throws Exception {
          HttpResponse<String> response = Unirest.post("http://localhost:2375/containers/" + containerName + "/restart")
                  .header("Content-Type", "application/json")
                  .asString();
          if (response.getStatus() == 204) {
              System.out.println("Container " + containerName + " restarted successfully.");
          } else {
              System.out.println("Failed to restart container " + containerName + ". Status code: " + response.getStatus());
          }
      }
      @PostMapping("/prometheusrestart")
      public void restartPrometheus() throws Exception{
          restartContainer("prometheus");
      }
      @PostMapping("/grafanarestart")
      public void restartGrafana() throws Exception{
          restartContainer("grafana");
      }
  */
   /*  @GetMapping("/restartimage")
   public void restartImage(){
      String containerId = "1920b11a8f40";
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        System.out.println("OK");
        RestartContainerCmd restartContainerCmd = dockerClient.restartContainerCmd(containerId);
       // restartContainerCmd.withTimeout(10);
        restartContainerCmd.exec();
        //    DockerClient dockerClient = DockerClientBuilder.getInstance().build();
       DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        System.out.println(config);
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();
        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        System.out.println(dockerClient);
        DockerHttpClient.Request request = DockerHttpClient.Request.builder()
                .method(DockerHttpClient.Request.Method.GET)
                .path("/_ping")
                .build();
   /*     try (DockerHttpClient.Response response = httpClient.execute(request)) {
            assertThat(response.getStatusCode(), equalTo(200));
            assertThat(IOUtils.toString(response.getBody()), equalTo("OK"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(request);
     //    List<Container> containers = dockerClient.listContainersCmd().exec();

     //   dockerClient.pingCmd().exec();
        RestartContainerCmd restartContainerCmd = dockerClient.restartContainerCmd("1920b11a8f40");
        restartContainerCmd.exec();

    } */

// lakthareya bch nestaamel hedhiii
    @GetMapping("/restartcontainer")
    public void restartContainer(@RequestParam(value="containername") String containerName) throws IOException {
       String containerId = this.getContainerIdByName(containerName);
    System.out.println(containerId);

    String commandEntete=" curl --unix-socket /var/run/docker.sock -X POST ";
       // String url="http://172.18.2.202:2375/containers/"+containerId+"/restart";
         String url="http://localhost:2375/containers/"+containerId+"/restart";

        String command=commandEntete+url;
        try {
            Process process = Runtime.getRuntime().exec(command);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public String getContainerIdByName(String containerName) throws IOException {


        URL url = new URL("http://localhost:2375/containers/json?all=true"); //tansech bch tbadel l @ ip selon l serveurs mtaa actia
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
            System.out.println("inputLine"+inputLine);
        }
        in.close();

        String containersJson = response.toString();
        JSONArray containers = new JSONArray(containersJson);
        for (int i = 0; i < containers.length(); i++) {
            JSONObject container = containers.getJSONObject(i);
            System.out.println("container"+container);
            //     JSONArray names = container.getJSONArray("Name");
            String img= container.get("Image").toString();
            //   System.out.println("names"+names);
            if(img.contains(containerName)){
                return container.getString("Id");
            }
        }

        return null;
    }



}





