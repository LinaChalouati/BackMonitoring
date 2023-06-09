package com.expo.grafana.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.expo.grafana.service.FolderManager;

@RestController
@CrossOrigin("*")
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
