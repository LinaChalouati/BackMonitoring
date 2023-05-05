package com.expo.project.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/api")

public class HealthCheck {

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        String prometheusUrl = "http://localhost:9090";
        String grafanaUrl = "http://localhost:3000";


        // @Value("${grafana.url}")
        //public String grafanaUrl;

        // @Value("${prometheus.server.url}")


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

    private boolean isServiceUp(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            httpClient.execute(request);
            return true;
        } catch (Exception e) {
            return false;
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

    @GetMapping("/servicesexist")
    public String checkServicesExist() {
        String command = "docker ps ";

        try {
            Process process = Runtime.getRuntime().exec(command);
            // System.out.println(process);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            System.out.println(process);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("prometheus") || line.equals("grafana")) {
                    return "Services exist";
                }
            }

            return "Services do not exist";
        } catch (IOException e) {
            return "Error checking services: " + e.getMessage();
        }
    }

    @GetMapping("/imagesexist")
    public String checkImagesExist() {
        String[] images = {"prom/prometheus", "grafana/grafana"};
        for (String image : images) {
            String command = "docker image inspect " + image;

            try {
                Process process = Runtime.getRuntime().exec(command);

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                StringBuilder output = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                if (output.toString().equals("[]\n")) {
                    return "Image " + image + " does not exist";
                }
            } catch (IOException e) {
                return "Error checking image " + image + ": " + e.getMessage();
            }
        }

        return "Images exist";
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
                //  return "Grafana image does not exist";
                return false;
            } else {
                //    return "Grafana image exists";
                return true;
            }
        } catch (IOException e) {
            //   return "Error checking Grafana image: " + e.getMessage();
            return false;
        }
    }

    @GetMapping("/prometheusup")
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
                return "Prometheus image UP";
            } else {
                if (checkPrometheusExists()) {
                    return "Prometheus image DOWN ";
                } else {
                    return "Prometheus Image Does Not exist ";
                }

            }
        } catch (IOException e) {
            return "Error checking Grafana image: " + e.getMessage();
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
                return "Grafana image UP";
            } else {
                if (checkGrafanaExists()) {
                    return "Grafana image DOWN ";
                } else {
                    return "Grafana Image Does Not exist ";
                }

            }
        } catch (IOException e) {
            return "Error checking Grafana image: " + e.getMessage();
        }

    }

    @GetMapping("/restartgrafana")
    public String RestartGrafana() {
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


            if (output.toString().contains("grafana")) {
                return "Grafana image UP";
            } else {
                if (checkGrafanaExists()) {
                    return "Grafana image DOWN ";
                } else {
                    return "Grafana Image Does Not exist ";
                }

            }
        } catch (IOException e) {
            return "Error checking Grafana image: " + e.getMessage();
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

    @GetMapping("/getcontainerId")
    public void getContainerId() {
        String containerId = "1920b11a8f40";

        String command = "docker restart " + containerId;
        System.out.println(command);
        try {
            Process process = Runtime.getRuntime().exec(command);

            // BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));


            //  System.out.println(output.toString().contains("prometheus"));
            // System.out.println(output.toString().contains("grafana"));


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @GetMapping("/restartcontainer")
    public void restartcontainer() {
        String containerId = "1920b11a8f40";

       // String command = "docker restart " + containerId;
        //System.out.println(command);
        String commandEntete=" curl --unix-socket /var/run/docker.sock -X POST ";
        String url="http://172.18.2.202:2375/containers/"+containerId+"/restart";
        String command=commandEntete+url;
        try {
            Process process = Runtime.getRuntime().exec(command);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private static final String DOCKER_API_URL = "http://localhost:2375";
    @GetMapping("/containeridbyname")
    public String getContainerIdByName() throws IOException {
        URL url = new URL("http://localhost:2375/containers/json?all=true");
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
       //     JSONArray names = container.getJSONArray("Image");
            String img= container.get("Image").toString();
         //   System.out.println("names"+names);
            if(img.contains("prom/prometheus")){
                return container.getString("Id");
            }
           /* for (int j = 0; j < names.length(); j++) {
                String containerName = names.getString(j);
                if (containerName.contains("prom")) {
                    System.out.println(container.getString("Id"));
                    return container.getString("Id");
                }
            }*/
        }

        return null;
    }


    }





