package com.expo.grafana.controller;

import com.expo.grafana.service.TemplateExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/grafana/template")
public class TemplateController {


    private final TemplateExporter templateExporter;
    private final ResourceLoader resourceLoader;

    @Autowired
    public TemplateController(TemplateExporter templateExporter, ResourceLoader resourceLoader) {
        this.templateExporter = templateExporter;
        this.resourceLoader = resourceLoader;
    }

        @GetMapping("/template")
    public ResponseEntity<String> exportTemplate() throws IOException {
            if (templateExporter.checkTemplateExistence()){
                return new ResponseEntity<>("Template already exists", HttpStatus.OK);

            }
            else{
                templateExporter.exportDashboard();
                return new ResponseEntity<>("Template exported successfully", HttpStatus.CREATED);
            }

    }


}
