package com.disp.celesmaproject.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class FileController {


    //MEMCHIK
    // Endpoint to serve image file
    @GetMapping("/welcomepng")
    public ResponseEntity<Resource> getImage() throws Exception {
        // Path to the image file
        Path path = Paths.get("src/main/java/com/disp/learnspringsecurity/static/image/logo.png");
        // Load the resource
        Resource resource = new UrlResource(path.toUri());
        // Return ResponseEntity with image content type
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

}
