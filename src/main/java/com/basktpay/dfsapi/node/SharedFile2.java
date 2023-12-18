package com.basktpay.dfsapi.node;

import com.basktpay.dfsapi.exception.SharedFileException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@Configuration
@Slf4j
public class SharedFile2 {
    private final Logger logger = LoggerFactory.getLogger(com.basktpay.dfsapi.node.SharedFile.class);
    @Getter
    private String fileName;

    @Getter
    private String filePath;

    private final List<CopyOnWriteArrayList<String>> contentForReaders;  // List of content lists, one for each partition
    private final List<BlockingQueue<FileContent>> fileContentsQueues;
    private final AtomicBoolean shutdownFlag = new AtomicBoolean(false);
    private final CompletableFuture<Void> writerFuture = new CompletableFuture<>();
    private final List<Thread> writerThreads;
    private final int numberOfPartitions;

    @Autowired
    public SharedFile2(@Value("${fileName}") String fileName, @Value("${filePath}") String filePath, @Value("${numberOfPartitions}") int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
        this.contentForReaders = new ArrayList<>(numberOfPartitions);
        this.fileContentsQueues = new ArrayList<>(numberOfPartitions);
        this.writerThreads = new ArrayList<>(numberOfPartitions);
        this.filePath = filePath;
        this.fileName = fileName;
        for (int i = 0; i < numberOfPartitions; i++) {
            contentForReaders.add(new CopyOnWriteArrayList<>());
            fileContentsQueues.add(new LinkedBlockingQueue<>());
            writerThreads.add(createAsyncWriterThread(i));
        }

        logger.info("Started SharedFile:{} with {} partitions", fileName, numberOfPartitions);

        for (Thread writerThread : writerThreads) {
            writerThread.start();
        }

    }

    public String read() {
        StringBuilder fileData = new StringBuilder();

        for (int i = 0; i < numberOfPartitions; i++) {

            if (!Files.exists(findFilePath())) {
                logger.error("File does not exist for partition {}: {}", i, fileName);
                // Handle the case where the file does not exist
                throw new SharedFileException("File does not exist in " + fileName);
            }

            // checkThreadIsAlive();

            if (contentForReaders.get(i).isEmpty()) {
                loadFileContent(fileName, i);
            }

            String partitionData = contentForReaders.get(i).stream().collect(Collectors.joining(","));
            fileData.append(partitionData);
        }

        logger.debug("File successfully read:{}", fileData);
        return fileData.toString();
    }

    public boolean write(String message) {
        // Choose the partition based on some criteria (e.g., hash of the message, round-robin, etc.)
        int selectedPartition = choosePartition(message);

        FileContent fileContent = createFileContent(message);
        boolean added = fileContentsQueues.get(selectedPartition).offer(fileContent);

        if (!added) {
            logger.error("Failed to add file content to the queue for partition {}: {}", selectedPartition, fileContent);
            throw new SharedFileException("Failed to add file content to the queue: " + fileContent);
        } else {
            // checkThreadIsAlive();
            logger.info("Added to the queue for partition {}: {}", selectedPartition, fileContent);
        }

        return added;
    }

    private int choosePartition(String message) {
        int hashCode = message.hashCode();
        return Math.abs(hashCode % numberOfPartitions);
    }

    private CompletableFuture<Void> createAsyncWriter(int partitionIndex) {
        return CompletableFuture.runAsync(() -> {
                    logger.info("Polling and writing asynchronously for partition {}: {}",
                            partitionIndex, Thread.currentThread().getName());

                    while (!shutdownFlag.get()) {
                        try {
                            if (!fileContentsQueues.get(partitionIndex).isEmpty()) {
                                FileContent fileContent = fileContentsQueues.get(partitionIndex).poll();
                                Path path = findFilePath();
                                Files.writeString(path, fileContent.getDataContent(), StandardOpenOption.APPEND);
                                logger.debug("Written Data:{} to file : {} ", fileContent, fileName);
                                contentForReaders.get(partitionIndex).add(fileContent.getDataContent());
                                logger.info("Data was written to file :{} and read into contentMap", fileName);
                            } else {
                                // Add a short delay when the queue is empty to avoid busy waiting
                                Thread.sleep(10);
                            }
                        } catch (InterruptedException | IOException e) {
                            logger.error("Error during asynchronous writing for partition {}", partitionIndex, e);
                        }
                    }
                }, Executors.newSingleThreadExecutor())
                .exceptionally(throwable -> {
                    logger.error("Error occurred during asynchronous writing for partition {}", partitionIndex, throwable);
                    return null;
                })
                .thenRun(() -> logger.info("Asynchronous writer for partition {} has been shut down.", partitionIndex));
    }

    private Thread createAsyncWriterThread(int partitionIndex) {
        return new Thread(() -> {
            CompletableFuture<Void> asyncWriter = createAsyncWriter(partitionIndex);
            try {
                asyncWriter.join(); // Wait for the async writer to finish
            } catch (Exception e) {
                logger.error("Error waiting for the asynchronous writer to finish for partition {}", partitionIndex, e);
            }
        });
    }

    private FileContent createFileContent(String message) {
        FileContent fileContent = new FileContent();
        fileContent.setDataContent(message);
        return fileContent;
    }

    private void loadFileContent(String fileName, int partitionIndex) {
        try (InputStreamReader reader = new InputStreamReader(
                Files.newInputStream(findFilePath()),
                StandardCharsets.UTF_8)) {
            // Read the content of the file into a string
            String fileContent = FileCopyUtils.copyToString(reader);

            // Add the content to the respective partition's content list
            contentForReaders.get(partitionIndex).add(fileContent);

            logger.info("Initial content loaded successfully for partition {}.", partitionIndex);
        } catch (IOException e) {
            logger.error("Error loading shared file content for partition {}.", partitionIndex, e);

            // Throw a SharedFileException in case of an error
            throw new SharedFileException("Error loading shared file content for partition " + partitionIndex);
        }
    }
    private Path findFilePath() {
        return Paths.get(filePath, fileName);
    }

   /* private void checkThreadIsAlive() {
        if (!writerThread.isAlive()) {
            writerThread = createAsyncWriterThread();
            logger.debug("Thread is restarted!!!");
        }
    }*/

   /* @PreDestroy
    public void shutdown() {
        logger.info("Shutting down SharedFile...");
        shutdownFlag.set(true);
        try {
            // Wait for the writer thread to finish
            writerThread.join();
            logger.info("SharedFile has been gracefully shut down.");
        } catch (InterruptedException e) {
            logger.error("Error waiting for the writer thread to finish.", e);
            Thread.currentThread().interrupt();
        }
    }*/


}
