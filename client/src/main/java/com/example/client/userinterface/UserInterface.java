package com.example.client.userinterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;

import org.springframework.stereotype.Component;

import com.example.client.handler.HttpRequestHandler;
import com.example.client.handler.WebSocketHandler;
import com.example.client.model.CRDT;
import com.example.client.model.Data;
import com.example.client.model.Node;
import com.example.client.model.Position;
import com.example.client.model.SubDocument;
import com.example.client.model.TransferData;
import com.example.client.model.User;
import com.example.client.model.UserRole;

@Component
public class UserInterface extends JFrame {
    private JTextArea textArea;
    private JTextField viewerCodeField, editorCodeField;
    private JLayeredPane layeredPane;
    private JPanel userPresencePanel;
    private HashMap<String, JLabel> userCursors;

    private User currentUser;
    private SubDocument document;    
    private CRDT tree;    
    Data treeData;

    public UserInterface() {
        System.out.println("User Interface Initialized");

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (document != null)
                    HttpRequestHandler.leaveDocument(currentUser.getUserId(), document.getDocumentId());
                System.exit(0);
            }
        });

        userCursors = new HashMap<>();
        viewerCodeField = new JTextField();
        editorCodeField = new JTextField();
        tree = new CRDT();
        showUserInfoPage();
    }

    private void showUserInfoPage() {
        JFrame userFrame = new JFrame("User Information");
        userFrame.setSize(400, 250);
        userFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        userFrame.setLocationRelativeTo(null);
        userFrame.setLayout(new GridBagLayout());
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
    
        JLabel nameLabel = new JLabel("Enter your name:");
        JTextField nameField = new JTextField(20);
    
        JLabel colorLabel = new JLabel("Choose a color:");
        String[] colorOptions = {"Red", "Green", "Blue", "Yellow", "Purple"};
        JComboBox<String> colorComboBox = new JComboBox<>(colorOptions);
    
        JButton nextButton = new JButton("Next");
    
        gbc.gridx = 0;
        gbc.gridy = 0;
        userFrame.add(nameLabel, gbc);
    
        gbc.gridy = 1;
        userFrame.add(nameField, gbc);
    
        gbc.gridy = 2;
        userFrame.add(colorLabel, gbc);
    
        gbc.gridy = 3;
        userFrame.add(colorComboBox, gbc);
    
        gbc.gridy = 4;
        userFrame.add(nextButton, gbc);
    
        nextButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (!name.isEmpty()) {
                String userName = name;

                String selectedColor = (String) colorComboBox.getSelectedItem();

                currentUser = HttpRequestHandler.createUser(userName, selectedColor);
                if (currentUser == null) {
                    JOptionPane.showMessageDialog(userFrame, "Failed to create user.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                userFrame.dispose();
                showSessionChoicePage();
            } else {
                JOptionPane.showMessageDialog(userFrame, "Please enter your name.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        userFrame.setVisible(true);
    }

    private void showSessionChoicePage() {
        JFrame sessionFrame = new JFrame("Join or Create Session");
        sessionFrame.setSize(400, 200);
        sessionFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        sessionFrame.setLocationRelativeTo(null);
        sessionFrame.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel prompt = new JLabel("Would you like to create or join a document?");
        JButton createButton = new JButton("Create New Document");
        JButton joinButton = new JButton("Join Existing Document");

        gbc.gridx = 0;
        gbc.gridy = 0;
        sessionFrame.add(prompt, gbc);

        gbc.gridy = 1;
        sessionFrame.add(createButton, gbc);

        gbc.gridy = 2;
        sessionFrame.add(joinButton, gbc);

        createButton.addActionListener(e -> {
            currentUser.setUserRole(UserRole.valueOf("OWNER"));

            document = HttpRequestHandler.createDocument(currentUser.getUserId());
            if (document == null) {
                JOptionPane.showMessageDialog(sessionFrame, "Failed to create document.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            sessionFrame.dispose();
            launchEditor();

            viewerCodeField.setText(document.getViewerCode());
            editorCodeField.setText(document.getEditorCode());
        });


        joinButton.addActionListener(e -> {
            String code = JOptionPane.showInputDialog(sessionFrame, "Enter the session code:");
            if (code != null && !code.trim().isEmpty()) {
                String sessionCode = code.trim();

                if (sessionCodeType(sessionCode)) {
                    currentUser.setUserRole(UserRole.valueOf("VIEWER"));
                } else {
                    currentUser.setUserRole(UserRole.valueOf("EDITOR"));
                }
        
                document = HttpRequestHandler.joinDocument(currentUser.getUserId(), sessionCode);
                if (document == null) {
                    JOptionPane.showMessageDialog(sessionFrame, "Failed to join document.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                treeData = tree.setDocumentFromString(document.getDocumentText());

                sessionFrame.dispose();
                launchEditor();
    
                viewerCodeField.setText(document.getViewerCode());
                editorCodeField.setText(document.getEditorCode());
            } else {
                JOptionPane.showMessageDialog(sessionFrame, "Please enter a session code.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        sessionFrame.setVisible(true);
        }


    private void launchEditor() {
        WebSocketHandler.comingInsert(document.getDocumentId(), currentUser.getUserId());
        WebSocketHandler.comingDelete(document.getDocumentId(), currentUser.getUserId());
        WebSocketHandler.comingUndoRedo(document.getDocumentId(), currentUser.getUserId());
        setTitle("Collaborative Text Editor");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Menu Bar UI

        JMenuBar menuBar = new JMenuBar();
        JButton importButton = new JButton("Import");
        JButton exportButton = new JButton("Export");
        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");

        // Left Panel UI

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(250, 0));

        JLabel viewerCodeLabel = new JLabel("Viewer Code:");
        JLabel editorCodeLabel = new JLabel("Editor Code:");

        JTextField viewerCodeField = new JTextField();
        JTextField editorCodeField = new JTextField();

        JButton copyViewerCodeButton = new JButton("Copy Viewer Code");
        JButton copyEditorCodeButton = new JButton("Copy Editor Code");

        viewerCodeLabel.setVisible(false);
        viewerCodeField.setVisible(false);
        copyViewerCodeButton.setVisible(false);

        editorCodeLabel.setVisible(false);
        editorCodeField.setVisible(false);
        copyEditorCodeButton.setVisible(false);

        leftPanel.add(viewerCodeLabel);
        leftPanel.add(viewerCodeField);
        leftPanel.add(copyViewerCodeButton);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        leftPanel.add(editorCodeLabel);
        leftPanel.add(editorCodeField);
        leftPanel.add(copyEditorCodeButton);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        if (currentUser.getUserRole().equals(UserRole.valueOf("OWNER"))) {
            viewerCodeField.setText(document.getViewerCode());
            viewerCodeLabel.setVisible(true);
            viewerCodeField.setVisible(true);
            copyViewerCodeButton.setVisible(true);
            leftPanel.revalidate();
            leftPanel.repaint();

            editorCodeField.setText(document.getEditorCode());
            editorCodeLabel.setVisible(true);
            editorCodeField.setVisible(true);
            copyEditorCodeButton.setVisible(true);
            leftPanel.revalidate();
            leftPanel.repaint();
        } else if (currentUser.getUserRole().equals(UserRole.valueOf("EDITOR"))) {
            JButton fetchViewerCodeButton = new JButton("Get Viewer Code");
            JButton fetchEditorCodeButton = new JButton("Get Editor Code");

            leftPanel.add(fetchViewerCodeButton);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            leftPanel.add(fetchEditorCodeButton);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));

            fetchViewerCodeButton.addActionListener(e -> {
                String viewerCode = HttpRequestHandler.getViewerCode(document.getDocumentId());
                if (viewerCode != null) {
                    viewerCodeField.setText(viewerCode);
                    viewerCodeLabel.setVisible(true);
                    viewerCodeField.setVisible(true);
                    copyViewerCodeButton.setVisible(true);
                    fetchViewerCodeButton.setVisible(false);
                    leftPanel.revalidate();
                    leftPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to retrieve viewer code.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            fetchEditorCodeButton.addActionListener(e -> {
                String editorCode = HttpRequestHandler.getEditorCode(document.getDocumentId());
                if (editorCode != null) {
                    editorCodeField.setText(editorCode);
                    editorCodeLabel.setVisible(true);
                    editorCodeField.setVisible(true);
                    copyEditorCodeButton.setVisible(true);
                    fetchEditorCodeButton.setVisible(false);
                    leftPanel.revalidate();
                    leftPanel.repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to retrieve editor code.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        copyViewerCodeButton.addActionListener(e -> copyToClipboard(viewerCodeField.getText()));
        copyEditorCodeButton.addActionListener(e -> copyToClipboard(editorCodeField.getText()));

        leftPanel.add(new JLabel("Active Users:"));

        DefaultListModel<String>  activeUsersModel = new DefaultListModel<>();

        for (User user : document.getUsers().values()) {
            activeUsersModel.addElement(user.getUserName());
        }

        JList<String> activeUsersList = new JList<>(activeUsersModel);
        JScrollPane usersScrollPane = new JScrollPane(activeUsersList);
        leftPanel.add(usersScrollPane);

        // Text Area UI

        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        if (currentUser.getUserRole().equals(UserRole.valueOf("VIEWER"))) {
            textArea.setEditable(false);
        }

        if (treeData != null) {
            textArea.setText(treeData.getText());            
            textArea.setCaretPosition(treeData.getCursorPosition());
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        scrollPane.setBounds(0, 0, 700, 700);
        layeredPane.add(scrollPane, Integer.valueOf(0));

        userPresencePanel = new JPanel();
        userPresencePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        userPresencePanel.setPreferredSize(new Dimension(0, 40));
        scrollPane.setColumnHeaderView(userPresencePanel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(layeredPane, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);

        for (User user : document.getUsers().values()) {
            addUserPresence(user.getUserName(), user.getUserColor());
        }

        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (currentUser.getUserRole().equals(UserRole.valueOf("VIEWER"))) return; 

                char keyChar = e.getKeyChar();

                if (!Character.isISOControl(keyChar)) {
                    e.consume(); 
                    ArrayList<Node> nodes = new ArrayList<>();
                    nodes.add(new Node(1, keyChar, false));
                    String id = tree.getCursor().getCurrent().getId();
                    Data data = tree.insertAndTraverse(nodes);
                    data.setPositionId(id);
                    textArea.setText(data.getText());
                    textArea.setCaretPosition(data.getCursorPosition());
                    WebSocketHandler.insert(document.getDocumentId(), currentUser.getUserId(), data.getPositionId(), nodes);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (currentUser.getUserRole().equals(UserRole.valueOf("VIEWER"))) return; 

                int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                    String id = tree.getCursor().getCurrent().getId();
                    Data data = tree.deleteAndTraverse(1);
                    data.setPositionId(id);
                    textArea.setText(data.getText());
                    textArea.setCaretPosition(data.getCursorPosition());
                    WebSocketHandler.delete(document.getDocumentId(), currentUser.getUserId(), data.getPositionId());
                } else if (keyCode == KeyEvent.VK_LEFT) {
                    e.consume();
                    Data data = tree.moveLeftAndTraverse();
                    textArea.setText(data.getText());
                    textArea.setCaretPosition(data.getCursorPosition());
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    e.consume();
                    Data data = tree.moveRightAndTraverse();
                    textArea.setText(data.getText());
                    textArea.setCaretPosition(data.getCursorPosition());
                } else if (keyCode == KeyEvent.VK_ENTER) {
                    e.consume();
                    ArrayList<Node> nodes = new ArrayList<>();
                    nodes.add(new Node(1, '\n', false));
                    String id = tree.getCursor().getCurrent().getId();
                    Data data = tree.insertAndTraverse(nodes);
                    data.setPositionId(id);
                    textArea.setText(data.getText());
                    textArea.setCaretPosition(data.getCursorPosition());
                    WebSocketHandler.insert(document.getDocumentId(), currentUser.getUserId(), data.getPositionId(), nodes);
                }
            }
        });

        textArea.addCaretListener(new CaretListener() {
            private int lastCaretPosition = textArea.getCaretPosition();
        
            @Override
            public void caretUpdate(CaretEvent e) {
                int currentCaretPosition = textArea.getCaretPosition();
                if (currentCaretPosition != lastCaretPosition) {
                    if (currentCaretPosition > lastCaretPosition) {
                        moveCustomCursorRight(currentCaretPosition - lastCaretPosition);
                    } else {
                        moveCustomCursorLeft(lastCaretPosition - currentCaretPosition);
                    }
                    lastCaretPosition = currentCaretPosition;
                    try {
                        Rectangle rectangle = textArea.modelToView2D(currentCaretPosition).getBounds();

                        if (rectangle != null) {
                            Position currentPosition = new Position(
                                layeredPane,
                                rectangle.x,
                                rectangle.y + rectangle.height + 20
                            );

                            drawCursor(currentPosition, currentUser);
                        }
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        importButton.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(this, "Enter file name to import:");
            if (fileName != null && !fileName.trim().isEmpty()) {
                ArrayList<Node> nodes = new ArrayList<>();
                String id = tree.getCursor().getCurrent().getId();
                Data data = tree.importFile(fileName.trim(), nodes);
                data.setPositionId(id);
                textArea.setText(data.getText());
                textArea.setCaretPosition(data.getCursorPosition());
                WebSocketHandler.insert(document.getDocumentId(), currentUser.getUserId(), data.getPositionId(), nodes);
            }
        });

        exportButton.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(this, "Enter file name to export:");
            if (fileName != null && !fileName.trim().isEmpty()) {
                tree.exportFile(fileName.trim());
            }
        });
        
        undoButton.addActionListener(e -> {
            Data data = tree.undoAndTraverse();
            textArea.setText(data.getText());
            textArea.setCaretPosition(data.getCursorPosition());
            WebSocketHandler.undoRedo(document.getDocumentId(), currentUser.getUserId(), data.getNodes());
        });

        redoButton.addActionListener(e -> {
            Data data = tree.redoAndTraverse();
            textArea.setText(data.getText());
            textArea.setCaretPosition(data.getCursorPosition());
            WebSocketHandler.undoRedo(document.getDocumentId(), currentUser.getUserId(), data.getNodes());
        });

        menuBar.add(importButton);
        menuBar.add(exportButton);
        menuBar.add(undoButton);
        menuBar.add(redoButton);
        setJMenuBar(menuBar);

        setVisible(true);
    }

    public void insertExternal(TransferData insertData) {
        Data data = tree.insertExternalAndTraverse(insertData.getPositionId(), insertData.getNodes());
        textArea.setText(data.getText());
        textArea.setCaretPosition(data.getCursorPosition());
    }

    public void deleteExternal(TransferData deleteData) {
        Data data = tree.deleteExternalAndTraverse(deleteData.getPositionId(), 1);
        textArea.setText(data.getText());
        textArea.setCaretPosition(data.getCursorPosition());
    }

    public void undoRedoExternal(TransferData insertData) {
        Data data = tree.undoRedoExternalAndTraverse(insertData.getNodes());
        textArea.setText(data.getText());
        textArea.setCaretPosition(data.getCursorPosition());
    }

    private void moveCustomCursorRight(int distance) {
        for (int i = 0; i < distance; i++) {
            tree.moveRightAndTraverse();
        }
    }
    
    private void moveCustomCursorLeft(int distance) {
        for (int i = 0; i < distance; i++) {
            tree.moveLeftAndTraverse();
        }
    }

    private Color parseColor(String colorName) {
        switch (colorName.toLowerCase()) {
            case "red": return Color.RED;
            case "green": return Color.GREEN;
            case "blue": return Color.BLUE;
            case "yellow": return Color.YELLOW;
            case "purple": return new Color(128, 0, 128);
            default: return Color.GRAY;
        }
    }

    private void addUserPresence(String username, String stringColor) {
        Color color = parseColor(stringColor); 
        JLabel cursorLabel = new JLabel(username);
        cursorLabel.setOpaque(true);
        cursorLabel.setBackground(color);
        cursorLabel.setForeground(Color.WHITE);
        cursorLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        userPresencePanel.add(cursorLabel);
    }

    private void drawCursor(Position pos, User user) {
        String userId = user.getUserId();
    
        if (userCursors.containsKey(userId)) {
            JLabel cursorLabel = userCursors.get(userId);
            cursorLabel.setBounds(pos.getX(), pos.getY(), 4, 20);
        } else {
            JLabel cursorLabel = new JLabel();
            cursorLabel.setOpaque(true);
            cursorLabel.setBackground(parseColor(user.getUserColor()));
            cursorLabel.setBounds(pos.getX(), pos.getY(), 4, 20);
            cursorLabel.setText("");
    
            layeredPane.add(cursorLabel, Integer.valueOf(1));
            userCursors.put(userId, cursorLabel);
        }
    }
    

    private boolean sessionCodeType(String sessionCode) {
        if (sessionCode == null) 
            return true;

        sessionCode = sessionCode.toLowerCase();

        if (sessionCode.startsWith("viewer")) {
            return true;
        } else if (sessionCode.startsWith("editor")) {
            return false;
        }

        return true;
    }

    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(text), null);
        JOptionPane.showMessageDialog(this, "Copied to clipboard!");
    }
}