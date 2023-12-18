package com.basktpay.dfsapi.controller;

import com.basktpay.dfsapi.exception.SharedFileException;
import com.basktpay.dfsapi.node.NodeManager;
import com.basktpay.dfsapi.reponse.FileContentResponse;
import com.basktpay.dfsapi.reponse.FileWriteRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SharedFileControllerTest {

    @Mock
    private NodeManager nodeManager;

    @InjectMocks
    private SharedFileController sharedFileController;

    @Test
    void getFile_Success() {
        // Mock the behavior of NodeManager
        when(nodeManager.readSharedFile()).thenReturn("Mocked file content");

        // Call the controller method
        ResponseEntity<FileContentResponse> responseEntity = sharedFileController.getFile();

        // Verify the response
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Mocked file content", responseEntity.getBody().getMessage());

        // Verify that NodeManager.readSharedFile() is called
        verify(nodeManager, times(1)).readSharedFile();
    }

    @Test
    void getFile_Exception() {
        // Mock the behavior of NodeManager to throw an exception
        when(nodeManager.readSharedFile()).thenThrow(new SharedFileException("Mocked exception"));

        // Call the controller method
        ResponseEntity<FileContentResponse> responseEntity = sharedFileController.getFile();

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Unable to Read the file!!!", responseEntity.getBody().getMessage());

        // Verify that NodeManager.readSharedFile() is called
        verify(nodeManager, times(1)).readSharedFile();
    }

    @Test
    void writeFile_Success() {
        // Mock the behavior of NodeManager
        doNothing().when(nodeManager).writeSharedFile(anyString(), anyString());

        FileWriteRequest fileWriteRequest = new FileWriteRequest();
        fileWriteRequest.setMessage("Test Message");
        // Call the controller method
        ResponseEntity<FileContentResponse> responseEntity = sharedFileController.writeFile(
                fileWriteRequest, null, "appKey");

        // Verify the response
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Message written to the shared file.", responseEntity.getBody().getMessage());

        // Verify that NodeManager.writeSharedFile() is called
        verify(nodeManager, times(1)).writeSharedFile(anyString(), anyString());
    }

    @Test
    void writeFile_Exception() {
        // Mock the behavior of NodeManager to throw an exception
        doThrow(new SharedFileException("Mocked exception")).when(nodeManager).writeSharedFile(anyString(), anyString());
        FileWriteRequest fileWriteRequest = new FileWriteRequest();
        fileWriteRequest.setMessage("Test Message");
        // Call the controller method
        ResponseEntity<FileContentResponse> responseEntity = sharedFileController.writeFile(
                fileWriteRequest, null, "appKey");

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Mocked exception", responseEntity.getBody().getMessage());

        // Verify that NodeManager.writeSharedFile() is called
        verify(nodeManager, times(1)).writeSharedFile(anyString(), anyString());
    }

    @Test
    void fallbackWriteFile() {
        FileWriteRequest fileWriteRequest = new FileWriteRequest();
        fileWriteRequest.setMessage("Test Message");
        // Call the fallback method directly
        ResponseEntity<FileContentResponse> responseEntity = sharedFileController.fallbackWriteFile(
                fileWriteRequest, null, "appKey", new Throwable("Mocked throwable"));

        // Verify the response
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, responseEntity.getStatusCode());
        assertEquals("Circuit is open. Fallback response.", responseEntity.getBody().getMessage());
    }
}
