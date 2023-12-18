package com.basktpay.dfsapi.controller;

import com.basktpay.dfsapi.exception.SharedFileException;
import com.basktpay.dfsapi.node.NodeManager;
import com.basktpay.dfsapi.reponse.FileContentResponse;
import com.basktpay.dfsapi.reponse.FileWriteRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SharedFileController {
    private final Logger logger = LoggerFactory.getLogger(SharedFileController.class);
    private final NodeManager nodeManager;
    @Value("${app.key}")
    private String apikey;
    @Autowired
    public SharedFileController(NodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }


    @GetMapping("/")
    public ResponseEntity<String> showPage() {
        try {
            // Load the HTML file from the classpath
            Resource resource = new ClassPathResource("static/index.html");
            Path htmlPath = resource.getFile().toPath();

            // Read the content of the HTML file
            String htmlContent = Files.readString(htmlPath);

            // Set the Content-Type as text/html
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(htmlContent);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error loading HTML page");
        }
    }
    @GetMapping("/config")
    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("apikey", apikey);
        return config;
    }
    @GetMapping("/file")
    public ResponseEntity<FileContentResponse> getFile() {
        FileContentResponse response = new FileContentResponse();
        try {
            response.setMessage(nodeManager.readSharedFile());
            response.setStatus(HttpStatus.OK.name());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.setMessage("Unable to Read the file!!!");
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
            logger.error("Unable to Read the file!!!");
           return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/file")
    @CircuitBreaker(name = "writeSharedFile", fallbackMethod = "fallbackWriteFile")
    public ResponseEntity<FileContentResponse> writeFile(@RequestBody(required = false) FileWriteRequest request,
                                                         @RequestBody(required = false) String message, @RequestParam(name="app_key") String appKey){
        FileContentResponse response = new FileContentResponse();
        try {
            if (request != null) {
                String jsonMessage = request.getMessage();
                nodeManager.writeSharedFile(jsonMessage,appKey);
                response.setMessage("Message written to the shared file.");
                response.setStatus(HttpStatus.OK.name());
            } else if (message != null) {
                nodeManager.writeSharedFile(message, appKey);
                response.setMessage("Message written to the shared file.");
                response.setStatus(HttpStatus.OK.name());
            } else {
                response.setMessage("Unable to write the file!!!");
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }catch (SharedFileException e){
            logger.error(e.getMessage());
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
           return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }catch (SecurityException e){
            response.setMessage("Invalid or Missing API Key!!!!!");
            response.setStatus(HttpStatus.UNAUTHORIZED.name());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<FileContentResponse> fallbackWriteFile(
            FileWriteRequest request,
            String message,
            String appKey,
            Throwable throwable) {

        // Fallback method implementation when the circuit is open

        FileContentResponse response = new FileContentResponse();
        response.setMessage("Circuit is open. Fallback response.");
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.name());
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
