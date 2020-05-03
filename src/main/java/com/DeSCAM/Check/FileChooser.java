package com.DeSCAM.Check;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class FileChooser extends JPanel {
    public static final int MODE_OPEN = 1;
    public static final int MODE_SAVE = 2;
    private JTextField textField;
    private JFileChooser fileChooser;
    private int mode;

    // Constructs a text filed + button UI elements and sets the JPanel layout to FlowLayout
    public FileChooser(String buttonLabel) {
        fileChooser = new JFileChooser();
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        textField = new JTextField(30);
        JButton button = new JButton(buttonLabel);
        button.setPreferredSize(new Dimension(30, 24));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                buttonActionPerformed(evt);
            }
        });
        add(textField);
        add(button);
        mode = MODE_OPEN;
    }

    // Button behavior
    private void buttonActionPerformed(ActionEvent evt) {
        if (mode == MODE_OPEN) {
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        } else if (mode == MODE_SAVE) {
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        }
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getSelectedFilePath() {
        return textField.getText();
    }

    public JFileChooser getFileChooser() {
        return this.fileChooser;
    }

    public JTextField getTextField() {
        return textField;
    }
}
