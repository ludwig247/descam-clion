package com.DeSCAM.Check;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.components.panels.VerticalLayout;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class Option implements Configurable {
    public static final String OPTION_KEY_PYTHON = "python";
    public static final String OPTION_KEY_CHECKER = "checker";
    private OptionModifiedListener listener = new OptionModifiedListener(this);
    private boolean modified = false;

    // text field and button for python path
    private FileChooser pythonInterpreterChooser;
    // text field and button for linter path
    private FileChooser linterFileChooser;

    //option name in settings menu
    @Nls
    @Override
    public String getDisplayName() {
        return "DeSCAM Check";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        // Creating a panel containing all the options menu elements
        JPanel jPanel = new JPanel();
        VerticalLayout layout = new VerticalLayout(1, 2);
        jPanel.setLayout(layout);
        JLabel pythonLabel = new JLabel("Path to Python executable:");
        pythonInterpreterChooser = new FileChooser("...");
        JLabel linterLabel = new JLabel("Path to descam_check.py:");
        linterFileChooser = new FileChooser("...");
        reset();
        // Adding document listener to text fields to track changes
        pythonInterpreterChooser.getTextField().getDocument().addDocumentListener(listener);
        linterFileChooser.getTextField().getDocument().addDocumentListener(listener);
        // Adding menu elements to panel
        jPanel.add(pythonLabel);
        jPanel.add(pythonInterpreterChooser);
        jPanel.add(linterLabel);
        jPanel.add(linterFileChooser);
        return jPanel;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        Settings.set(OPTION_KEY_PYTHON, pythonInterpreterChooser.getTextField().getText());
        Settings.set(OPTION_KEY_CHECKER, linterFileChooser.getTextField().getText());
        modified = false;
    }

    //reset options to last saved state
    @Override
    public void reset() {
        String pythonPath = Settings.get(OPTION_KEY_PYTHON);
        pythonInterpreterChooser.getTextField().setText(pythonPath);

        String linterPath = Settings.get(OPTION_KEY_CHECKER);
        linterFileChooser.getTextField().setText(linterPath);

        modified = false;
    }

    @Override
    public void disposeUIResources() {
        pythonInterpreterChooser.getTextField().getDocument().removeDocumentListener(listener);
        linterFileChooser.getTextField().getDocument().removeDocumentListener(listener);
        //LinterOptionsText.getDocument().removeDocumentListener(listener);
    }

    private static class OptionModifiedListener implements DocumentListener {
        private final Option option;

        public OptionModifiedListener(Option option) {
            this.option = option;
        }

        @Override
        public void insertUpdate(DocumentEvent documentEvent) {
            option.setModified(true);
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent) {
            option.setModified(true);
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent) {
            option.setModified(true);
        }
    }
}
