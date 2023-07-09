package com.expo.prometheus.model;

// PrometheusResponse.java

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class PrometheusResponse {
    private Data data;

    public static class Data {
        private List<Result> result;

        public static class Result {
            private Map<String, String> metric;
            private List<Object> value;

            @JsonCreator
            public Result(@JsonProperty("metric") Map<String, String> metric, @JsonProperty("value") List<Object> value) {
                this.metric = metric;
                this.value = value;
            }

            // getters and setters
        }

        // getters and setters
    }

    @JsonCreator
    public PrometheusResponse(@JsonProperty("data") Data data) {
        this.data = data;
    }

    // getters and setters
}
