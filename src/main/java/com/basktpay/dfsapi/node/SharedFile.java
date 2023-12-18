package com.basktpay.dfsapi.node;

import com.basktpay.dfsapi.config.FileConfiguration;
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

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@Configuration
@Slf4j
public class SharedFile {
    private final Logger logger = LoggerFactory.getLogger(SharedFile.class);
    @Getter
    private String fileName;

    @Getter
    private String filePath;

    private final CopyOnWriteArrayList<String> contentForReader = new CopyOnWriteArrayList<>();

    private final BlockingQueue<FileContent> fileContents = new LinkedBlockingQueue<FileContent>();
    private final AtomicBoolean shutdownFlag = new AtomicBoolean(false);
    private final CompletableFuture<Void> writerFuture = new CompletableFuture<>();
    Thread writerThread;

    @Autowired
    public SharedFile(@Value("${fileName}") String fileName, @Value("${filePath}") String filePath) {
        this.fileName = fileName;
        this.filePath = filePath;
        writerThread = createAsyncWriterThread();
        logger.info("Started SharedFile:{} with writer thread:{}", fileName, writerThread);
        writerThread.start();
        loadFileContent();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String read() {
        String fileData = "";
        if (!Files.exists(findFilePath())) {
            logger.error("File does not exist: {}", fileName);
            // Handle the case where the file does not exist
            throw new SharedFileException("File does not exist in " + fileName);
        }
        checkThreadIsAlive();
        if (contentForReader.isEmpty()) {
            loadFileContent();
        }
        fileData = contentForReader.stream().collect(Collectors.joining(" "));
        logger.debug("File successfully read:{}", fileData);
        return fileData;
    }

    public boolean write(String message) {
        FileContent fileContent = createFileContent(message);
        boolean added = fileContents.offer(fileContent);
        if (!added) {
            logger.error("Failed to add file content to the queue: {}", fileContent);
            throw new SharedFileException("Failed to add file content to the queue: " + fileContent);
        } else {
            checkThreadIsAlive();
            logger.info("Added to the queue: {}", fileContent);
        }
        return added;
    }


    private CompletableFuture<Void> createAsyncWriter() {
        return CompletableFuture.runAsync(() -> {
                    logger.info("Polling and writing asynchronously: {}", Thread.currentThread().getName());
                    while (!shutdownFlag.get()) {
                        if (!fileContents.isEmpty()) {
                            FileContent fileContent = fileContents.poll();
                            Path path = findFilePath();
                            try {
                                Files.writeString(path, fileContent.getDataContent(), StandardOpenOption.APPEND);
                                logger.debug("Written Data:{} to file : {} ", fileContent, fileName);
                                contentForReader.add(fileContent.getDataContent());
                                logger.info("Data was written to file :{} and read into contentMap", fileName);
                            } catch (IOException e) {
                                throw new SharedFileException("Unable to write the content in " + fileName);
                            }
                        }
                    }
                }, Executors.newSingleThreadExecutor())
                .exceptionally(throwable -> {
                    logger.error("Error occurred during asynchronous writing", throwable);
                    return null;
                })
                .thenRun(() -> logger.info("Asynchronous writer has been shut down."));
    }

    private Thread createAsyncWriterThread() {
        return new Thread(() -> {
            CompletableFuture<Void> asyncWriter = createAsyncWriter();
            try {
                asyncWriter.join(); // Wait for the async writer to finish
            } catch (Exception e) {
                logger.error("Error waiting for the asynchronous writer to finish.", e);
            }
        });
    }

    private FileContent createFileContent(String message) {
        FileContent fileContent = new FileContent();
        fileContent.setDataContent(message);
        return fileContent;
    }

    private void loadFileContent() {
        try (InputStreamReader reader = new InputStreamReader(
                Files.newInputStream(findFilePath()),
                StandardCharsets.UTF_8)) {
            String fileContent = FileCopyUtils.copyToString(reader);
            contentForReader.add(fileContent);
            logger.info("Initial content loaded successfully.");
        } catch (IOException e) {
            logger.error("Error loading shared file content", e);
            throw new SharedFileException("Error loading shared file content");
        }
    }

    private void checkThreadIsAlive() {
        if (!writerThread.isAlive()) {
            writerThread = createAsyncWriterThread();
            logger.debug("Thread is restarted!!!");
        }
    }

    private Path findFilePath() {
        return Paths.get(filePath, fileName);
    }

    @PreDestroy
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
    }

}
