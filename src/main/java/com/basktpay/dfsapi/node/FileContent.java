package com.basktpay.dfsapi.node;

import java.util.Objects;

public class FileContent {
    String nodeId;
    String dataContent;


    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getDataContent() {
        return dataContent;
    }

    public void setDataContent(String dataContent) {
        this.dataContent = dataContent;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileContent that = (FileContent) o;
        return Objects.equals(nodeId, that.nodeId) && Objects.equals(dataContent, that.dataContent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, dataContent);
    }

    @Override
    public String toString() {
        return "FileContent{" +
                "nodeId='" + nodeId + '\'' +
                ", dataContent='" + dataContent + '\'' +
                '}';
    }
}
