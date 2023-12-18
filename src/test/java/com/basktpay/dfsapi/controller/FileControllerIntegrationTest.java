package com.basktpay.dfsapi.controller;

import com.basktpay.dfsapi.reponse.FileContentResponse;
import com.basktpay.dfsapi.reponse.FileWriteRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FileControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testGetFileEndpoint() {
        FileContentResponse result = this.restTemplate.getForObject("http://localhost:" + port + "/file", FileContentResponse.class);
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("OK");
    }

    @Test
    public void testWriteFileEndpoint() {
        FileWriteRequest request = new FileWriteRequest();
        request.setMessage("Test message");

        FileContentResponse result = this.restTemplate.postForObject("http://localhost:" + port + "/file?app_key=12345", request, FileContentResponse.class);
        assertThat(result.getMessage()).isEqualTo("Message written to the shared file.");
        assertThat(result.getStatus()).isEqualTo("OK");
    }

    @Test
    public void testWriteFileEndpointWithoutApikey() {
        FileWriteRequest request = new FileWriteRequest();
        request.setMessage("Test message");
        FileContentResponse result = this.restTemplate.postForObject("http://localhost:" + port + "/file", request, FileContentResponse.class);

        assertThat(result.getStatus()).isEqualTo("400");

    }

}
