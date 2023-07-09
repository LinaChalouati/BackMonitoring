package com.expo.grafana.controller;


import com.expo.grafana.service.FolderManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/grafana/folder")
public class FolderController {
    @Autowired
    private FolderManager FolderManager;

    @GetMapping("/folders")
    public ResponseEntity<String> checkOrCreateFolder() {

        if (FolderManager.checkIfFolderExists() !="") {
            return ResponseEntity.status(HttpStatus.CREATED).body("Folder created successfully");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body("Folder already exists");
        }
    }

}
