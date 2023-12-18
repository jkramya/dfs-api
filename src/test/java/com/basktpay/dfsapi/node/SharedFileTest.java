package com.basktpay.dfsapi.node;

import com.basktpay.dfsapi.exception.SharedFileException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SharedFileTest {

    @AfterEach
    void afterEach() throws IOException {
        // Clean up resources after each test
        clearFile("src/test/resources/testfile.txt");
    }

    @Test
    void testExceptionForSharedFileNotFound() {
        assertThrows(SharedFileException.class, () -> {
            // Pass parameters that will cause an exception in the constructor
            SharedFile sharedFile = new SharedFile("xx.txt","src/test/resources");
            sharedFile.read();

        });
    }

    @Test
    void testEmptySharedFile() throws IOException {
        SharedFile sharedFile = new SharedFile("testfile.txt","src/test/resources");
        sharedFile.setFilePath("src/test/resources");
        String fileContent = sharedFile.read();
        assertEquals("",fileContent);
    }

    @Test
    void testConcurrentWrite() throws InterruptedException {
        SharedFile sharedFile = new SharedFile("testfile.txt","src/test/resources");
        int numThreads = 10;
        CountDownLatch latch = new CountDownLatch(numThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            String message = "Message"+i+"\n";
            executorService.execute(() -> {
                sharedFile.write(message);
                latch.countDown();
            });
        }

        latch.await(); // Wait for all threads to complete
        Thread.sleep(100);
        String result = sharedFile.read();
        int actualMessageCount = result.split("\n").length;
        assertEquals(numThreads, actualMessageCount, "Concurrent write test failed");
    }


    @Test
    void testConcurrentRead() throws InterruptedException, IOException {
        SharedFile sharedFile = new SharedFile("testfile.txt","src/test/resources"); // Adjust the file name

        // Populate the file with some initial content
        sharedFile.write("Initial content for testing");

        int numThreads = 10; // Adjust as needed
        CountDownLatch latch = new CountDownLatch(numThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            executorService.execute(() -> {
                try {
                    String content = sharedFile.read();
                    System.out.println("Content read: " + content);
                    assertEquals(",Initial content for testing",content);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
    }

    public static void clearFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, new byte[0]);
    }
}
