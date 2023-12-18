package com.basktpay.dfsapi.node;

import com.basktpay.dfsapi.node.SharedFile;
import com.basktpay.dfsapi.exception.SharedFileException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SharedFileMultithreadedTest {

    private static final int THREAD_COUNT = 5;

    private SharedFile sharedFile;

    @BeforeEach
    void setUp() {
        sharedFile = new SharedFile("testfile.txt", "src/test/resources");
    }

    @AfterEach
    void tearDown() throws IOException {
        clearFile("src/test/resources/testfile.txt");
        sharedFile.shutdown();
    }

    @Test
    void testMultithreadedWriteAndRead() throws IOException {
        clearFile("src/test/resources/testfile.txt");
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(THREAD_COUNT);

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        // Start THREAD_COUNT threads to write to the shared file
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    startSignal.await(); // Wait for the signal to start
                    sharedFile.write("Thread content");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    fail("Thread interrupted", e);
                } finally {
                    doneSignal.countDown(); // Signal that the thread has finished
                }
            });
        }

        startSignal.countDown(); // Release the threads to start writing

        try {
            doneSignal.await(); // Wait for all threads to finish writing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Thread interrupted", e);
        }

        // Read the file content after all threads have finished writing
        String fileContent = sharedFile.read();

        // Expect the content to be written THREAD_COUNT times
        assertEquals("Thread content".repeat(THREAD_COUNT), fileContent);
    }

    public static void clearFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, new byte[0]);
    }
}
