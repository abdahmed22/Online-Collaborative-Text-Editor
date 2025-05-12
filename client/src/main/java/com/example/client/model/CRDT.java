package com.example.client.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CRDT {
    private final Node virtualNode;
    private int count;
    private final Cursor cursor;
    private boolean found;
    private final Map<String, Node> nodesMap;
    private final Stack<String> undoStack;
    private final Stack<String> redoStack;
    private final Stack<Integer> undoStackCounter;
    private final Stack<Integer> redoStackCounter;
    private MyList list;
    private int uniqueExportId;

    public CRDT() {
        this.nodesMap = new HashMap<>();
        this.virtualNode = new Node(-1,'v', true);
        this.nodesMap.put(virtualNode.getId(), virtualNode);
        this.count = 0;
        this.cursor = new Cursor(this.virtualNode);
        this.found = false;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.undoStackCounter = new Stack<>();
        this.redoStackCounter = new Stack<>();
        this.list = new MyList();
        this.uniqueExportId = -2;
    }

    public void setUniqueExportId(int id) {
        this.uniqueExportId = id;
    }

    public int getUniqueExportId() {
        return this.uniqueExportId;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void insert(ArrayList<Node> nodes) {
        for (Node n : nodes) {
            cursor.getCurrent().addChild(n);
            cursor.setCurrent(n);
            nodesMap.put(n.getId(), n);
            count++;
            undoStack.push(n.getId());
        }
        undoStackCounter.push(nodes.size());
    }

    public void insertExternal(String positionId, ArrayList<Node> nodes) {
        Node target = nodesMap.get(positionId);
        for (Node n : nodes) {
            target.addChild(n);
            target = n;
            nodesMap.put(n.getId(), n);
            count++;
        }
    }

    public void delete(int n) {
        for (int i = 0; i < n; i++) {
            if (cursor.getCurrent() != virtualNode) {
                cursor.getCurrent().setDeleted(true);
                count--;
                undoStack.push(cursor.getCurrent().getId());
                cursor.setCurrent(cursor.getCurrent().getPrev());
            }
        }
        undoStackCounter.push(n);
    }

    public void deleteExternal(String positionId, int n) {
        Node target = nodesMap.get(positionId);
        for (int i = 0; i < n; i++) {
            if (target != virtualNode) {
                target.setDeleted(true);
                count--;
                target = target.getPrev();
            }
        }
    }

    public Data traverse() {
        this.list.setHead(null);
        this.list.setTail(null);
        String positionId = cursor.getCurrent().getId();
        StringBuilder text = new StringBuilder();
        int[] cursorPosition = new int[]{0};
        traverseInternal(virtualNode, cursorPosition, text);
        this.found = false;
        return new Data(text.toString(), cursorPosition[0], "NA", null);
    }

    public void traverseInternal(Node root, int[] cursorPosition, StringBuilder text) {
        if (root == null) return;
        if (!root.isDeleted()) {
            list.add(root);
            if (root != virtualNode) {
                if (!found) cursorPosition[0]++;
                text.append(root.getData()); 
                if (root == cursor.getCurrent()) {
                    found = true;
                }
            }
        }
        for (Node child : root.getChildren()) {
            traverseInternal(child, cursorPosition, text);
        }
    }

    public Data importFile(String fileName, ArrayList<Node> nods) {
        try {
            String txt = Files.readString(Path.of(fileName));
            ArrayList<Node> nodes = new ArrayList<>();
            for (int i = 0; i < txt.length(); i++) {
                nodes.add(new Node(getUniqueExportId(), txt.charAt(i), false));
            }
            nods.addAll(nodes);
            setUniqueExportId(getUniqueExportId()-1);
            insert(nodes);
            return traverse();
        } catch (IOException e) {
            System.out.print("There is a problem");
            return null;
        }
    }

    public void exportFile(String fileName) {
        try {
            System.out.println("Exporting to " + fileName);
            StringBuilder text = new StringBuilder();
            traverseForExport(virtualNode, text);
            System.out.println(text.toString());
            Files.writeString(Path.of(fileName), text.toString());
        } catch (IOException e) {
            System.out.println("There is a problem");
        }
    }

    public void traverseForExport(Node root, StringBuilder text) {
        if (root == null) return;
        if (!root.isDeleted()) {
            if (root != virtualNode) {
                text.append(root.getData());
            }
        }
        for (Node child : root.getChildren()) {
            traverseForExport(child, text);
        }
    }

    public ArrayList<Node> undo() {
        ArrayList<Node> nodes = new ArrayList<>();
        if (!undoStackCounter.isEmpty()) {
            int n = undoStackCounter.pop();
            for (int i = 0; i < n; i++) {
                String tempId = undoStack.pop();
                Node temp = nodesMap.get(tempId);
                nodes.add(temp);
                if (temp.isDeleted()) {
                    temp.setDeleted(false);
                    cursor.setCurrent(temp);
                    count++;
                }
                else {
                    temp.setDeleted(true);
                    cursor.setCurrent(temp.getPrev());
                    count--;
                }
                redoStack.push(temp.getId());
            }
            redoStackCounter.push(n);
        }
        return nodes;
    }

    public ArrayList<Node> redo() {
        ArrayList<Node> nodes = new ArrayList<>();
        if (!redoStackCounter.isEmpty()) {
            int n = redoStackCounter.pop();
            for (int i = 0; i < n; i++) {
                String tempId = redoStack.pop();
                Node temp = nodesMap.get(tempId);
                nodes.add(temp);
                if (temp.isDeleted()) {
                    temp.setDeleted(false);
                    cursor.setCurrent(temp);
                    count++;
                }
                else {
                    temp.setDeleted(true);
                    cursor.setCurrent(temp.getPrev());
                    count--;
                }
                undoStack.push(temp.getId());
            }
            undoStackCounter.push(n);
        }
        return nodes;
    }

    public void undoRedoExternal(ArrayList<Node> nodes) {
        ArrayList<Node> temp = new ArrayList<>();
        for (Node n : nodes) {
            temp.add(nodesMap.get(n.getId()));
        }
        for (Node n : temp) {
            if (n.isDeleted()) {
                n.setDeleted(false);
                count++;
            } else {
                n.setDeleted(true);
                count--;
            }
        }
    }

    public Data insertAndTraverse(ArrayList<Node> nodes) {
        insert(nodes);
        return traverse();
    }

    public Data insertExternalAndTraverse(String positionId, ArrayList<Node> nodes) {
        insertExternal(positionId, nodes);
        return traverse();
    }

    public Data deleteAndTraverse(int n) {
        delete(n);
        return traverse();
    }

    public Data deleteExternalAndTraverse(String positionId, int n) {
        deleteExternal(positionId, n);
        return traverse();
    }

    public Data moveRightAndTraverse() {
        cursor.moveRight();
        return traverse();
    }

    public Data moveLeftAndTraverse() {
        cursor.moveLeft();
        return traverse();
    }

    public Data undoAndTraverse() {
        ArrayList<Node> nodes = undo();
        Data data = traverse();
        data.setNodes(nodes);
        return data;
    }

    public Data redoAndTraverse() {
        ArrayList<Node> nodes = redo();
        Data data = traverse();
        data.setNodes(nodes);
        return data;
    }

    public Data undoRedoExternalAndTraverse(ArrayList<Node> nodes) {
        undoRedoExternal(nodes);
        return traverse();
    }

    public int getCount() {
        return count;
    }

    public void printTree() {
        System.out.println("Tree content: ");
        traverseTest(virtualNode);
        System.out.println();
    }

    public void traverseTest(Node root) {
        if (root == null) return;
        if (!root.isDeleted()) {
            if (root != virtualNode) {
                System.out.print(root.getData());
            }
        }
        for (Node child : root.getChildren()) {
            traverseTest(child);
        }
    }

    public Data setDocumentFromString(String text) {
        ArrayList<Node> nodes = new ArrayList<>();
    
        for (int i = 0; i < text.length(); i++) {
            nodes.add(new Node(getUniqueExportId(), text.charAt(i), false));
        }
    
        setUniqueExportId(getUniqueExportId() - 1);
        insert(nodes);
    
        return traverse();
    }    
}