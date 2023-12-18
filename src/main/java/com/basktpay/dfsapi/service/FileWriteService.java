package com.basktpay.dfsapi.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FileWriteService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public FileWriteService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void writeToFile(String message) {
        // Implement logic to write to the shared file
        // Notify nodes about the new content using Kafka
        kafkaTemplate.send("shared-file-topic", message);
    }
}
