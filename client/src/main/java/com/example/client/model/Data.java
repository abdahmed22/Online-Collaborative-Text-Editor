package com.example.client.model;

import java.util.ArrayList;

public class Data {
    private String text;
    private int cursorPosition;
    private String positionId;
    private ArrayList<Node> nodes;

    public Data(String text, int cursorPosition, String positionId, ArrayList<Node> nodes) {
        this.text = text;
        this.cursorPosition = cursorPosition;
        this.positionId = positionId;
        this.nodes = nodes;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setCursorPosition(int cursorPosition) {
        this.cursorPosition = cursorPosition;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }

    public ArrayList<Node> getNodes() {
        return this.nodes;
    }
}
