package com.basktpay.dfsapi.node;

import com.basktpay.dfsapi.config.FileConfiguration;
import com.basktpay.dfsapi.exception.SharedFileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NodeManagerWithPartitionIndex {
    private final Logger logger = LoggerFactory.getLogger(NodeManagerWithPartitionIndex.class);
    private final SharedFile2 sharedFile2;
    private final FileConfiguration fileConfiguration;

    @Autowired
    public NodeManagerWithPartitionIndex(SharedFile2 sharedFile2, FileConfiguration fileConfiguration) {
        this.sharedFile2 = sharedFile2;
        this.fileConfiguration = fileConfiguration;
     }

    public String readSharedFile() {
        logger.info("Reading the  Partitioned file Started!!!!!!!!!");
        return sharedFile2.read();
    }

    public void writeSharedFile(String message, String appKey) {
        logger.info("Writing the Partitioned file Started!!!!!!!!!");
       if(appKey.equals(fileConfiguration.getAppKey())) {
           boolean write = sharedFile2.write(message);
           if (!write) {
               throw new SharedFileException("Unable to write the message");
           }
       } else{
           throw new SecurityException("Invalid or missing API Key ");
       }
    }
}
