package com.example.texteditor.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransferData {

    @JsonIdentityReference(alwaysAsId = false)
    private String positionId;

    @JsonIdentityReference(alwaysAsId = false)
    private ArrayList<Node> nodes;

    public TransferData() {
    }

    @JsonCreator
    public TransferData(@JsonProperty("positionId") String positionId,
                      @JsonProperty("nodes") ArrayList<Node> nodes) {
        this.positionId = positionId;
        this.nodes = nodes;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }
}
