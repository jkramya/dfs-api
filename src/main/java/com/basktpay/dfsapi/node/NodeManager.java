package com.basktpay.dfsapi.node;

import com.basktpay.dfsapi.config.FileConfiguration;
import com.basktpay.dfsapi.exception.SharedFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class NodeManager {
    private final Logger logger = LoggerFactory.getLogger(NodeManager.class);
    private final SharedFile sharedFile;
    private final FileConfiguration fileConfiguration;

    @Autowired
    public NodeManager(SharedFile sharedFile, FileConfiguration fileConfiguration) {
        this.sharedFile = sharedFile;
        this.fileConfiguration = fileConfiguration;
     }

    public String readSharedFile() {
        logger.info("Reading the file Started!!!!!!!!!");
        return sharedFile.read();
    }

    public void writeSharedFile(String message, String appKey) {
       if(appKey.equals(fileConfiguration.getAppKey())) {
           logger.info("Writing the file Started!!!!!!!!!");
           boolean write = sharedFile.write(message);
           if (!write) {
               throw new SharedFileException("Unable to write the message");
           }
       } else{
           throw new SecurityException("Invalid or missing API Key ");
       }
    }
}
