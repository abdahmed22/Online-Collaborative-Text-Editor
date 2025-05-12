package com.example.client.model;

import javax.swing.JLayeredPane;

public class Position {
    private int x;
    private int y;
    private JLayeredPane pane;

    public Position(JLayeredPane pane, int x, int y) {
        this.pane = pane;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public JLayeredPane getPane() {
        return pane;
    }
}
