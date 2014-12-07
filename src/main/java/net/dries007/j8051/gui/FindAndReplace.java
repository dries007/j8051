/*
 * Copyright (c) 2014, Dries007
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.dries007.j8051.gui;

import net.dries007.j8051.Main;
import net.dries007.j8051.util.Constants;
import org.apache.commons.io.FilenameUtils;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rtextarea.RTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import static net.dries007.j8051.gui.AsmParser.ASM_PARSER;
import static net.dries007.j8051.gui.MainGui.MAIN_GUI;
import static net.dries007.j8051.util.Constants.*;
import static net.dries007.j8051.util.Constants.saveProperties;

public class FindAndReplace extends JDialog
{
    public static final FindAndReplace         FIND_AND_REPLACE = new FindAndReplace();
    private final       DefaultMutableTreeNode rootNode         = new DefaultMutableTreeNode("Results");
    private JPanel       contentPane;
    public  JButton      findNextButton;
    public  JButton      findAllButton;
    public  JRadioButton normalRadioButton;
    public  JRadioButton regexRadioButton;
    public  JButton      replaceButton;
    public  JButton      closeButton;
    public  JComboBox    findBox;
    public  JComboBox    replaceBox;
    public  JButton      replaceAllButton;
    public  JRadioButton srcIncludesRadioButton;
    public  JRadioButton srcOnlyRadioButton;
    public  JRadioButton selectionOnlyRadioButton;
    public  JCheckBox    alwaysOnTopCheckBox;
    public  JCheckBox    ignoreCaseCheckBox;
    public  JTree        resultsTree;

    private Result lastResult = null;

    private FindAndReplace()
    {
        setContentPane(contentPane);
        setModal(false);
        setTitle("Find and replace - j8051");
        setMinimumSize(new Dimension(660, 375));
        setMaximumSize(new Dimension(660, 375));
        setResizable(false);
        try
        {
            for (String res : new String[]{"1024", "512", "256", "128"})
            {
                ArrayList<Image> imageList = new ArrayList<>();
                URL url = getClass().getResource("/icon/j8051-" + res + ".png");
                if (url == null) continue;
                imageList.add(Toolkit.getDefaultToolkit().getImage(url));
                this.setIconImages(imageList);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        setLocationRelativeTo(MAIN_GUI.frame);
        setLocation(Integer.parseInt(Constants.PROPERTIES.getProperty(FIND_X, "0")), Integer.parseInt(Constants.PROPERTIES.getProperty(FIND_Y, "0")));
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentMoved(ComponentEvent e)
            {
                Constants.PROPERTIES.setProperty(FIND_X, Integer.toString(e.getComponent().getX()));
                Constants.PROPERTIES.setProperty(FIND_Y, Integer.toString(e.getComponent().getY()));
                saveProperties();
            }
        });
        setAlwaysOnTop(Boolean.parseBoolean(Constants.PROPERTIES.getProperty(FIND_ALWAYSONTOP, "true")));
        alwaysOnTopCheckBox.setSelected(isAlwaysOnTop());
        alwaysOnTopCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Constants.PROPERTIES.setProperty(FIND_ALWAYSONTOP, String.valueOf(alwaysOnTopCheckBox.isSelected()));
                setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
                saveProperties();
            }
        });
        ignoreCaseCheckBox.setSelected(Boolean.parseBoolean(Constants.PROPERTIES.getProperty(FIND_IGNORECASE, "true")));
        ignoreCaseCheckBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Constants.PROPERTIES.setProperty(FIND_IGNORECASE, String.valueOf(ignoreCaseCheckBox.isSelected()));
                saveProperties();
            }
        });
        resultsTree.setModel(new DefaultTreeModel(rootNode));
        closeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
                MAIN_GUI.asmContents.removeAllLineHighlights();
                for (int i = 0; i < MAIN_GUI.includeFiles.getTabCount(); i++) ((RTextScrollPane) MAIN_GUI.includeFiles.getComponentAt(i)).getTextArea().removeAllLineHighlights();
            }
        });
        ActionListener cmdButtonListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doAction(e.getActionCommand());
            }
        };
        findBox.addActionListener(cmdButtonListener);
        replaceBox.addActionListener(cmdButtonListener);
        findNextButton.addActionListener(cmdButtonListener);
        findAllButton.addActionListener(cmdButtonListener);
        replaceButton.addActionListener(cmdButtonListener);
        replaceAllButton.addActionListener(cmdButtonListener);
        resultsTree.addTreeSelectionListener(new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent e)
            {
                try
                {
                    if (resultsTree.getSelectionPaths() == null) return;
                    MAIN_GUI.asmContents.removeAllLineHighlights();
                    for (int i = 0; i < MAIN_GUI.includeFiles.getTabCount(); i++) ((RTextScrollPane) MAIN_GUI.includeFiles.getComponentAt(i)).getTextArea().removeAllLineHighlights();
                    for (TreePath path : resultsTree.getSelectionPaths()) select((DefaultMutableTreeNode) path.getLastPathComponent());
                }
                catch (BadLocationException e1)
                {
                    e1.printStackTrace();
                }
            }

            private void select(DefaultMutableTreeNode node) throws BadLocationException
            {
                if (node.getChildCount() == 0)
                {
                    Result result = (Result) node.getUserObject();
                    result.select();
                    getTextAreaFromFileId(result.fileId).addLineHighlight(result.lineNr, Color.YELLOW);
                }
                else
                {
                    for (int i = 0; i < node.getChildCount(); i++) select((DefaultMutableTreeNode) node.getChildAt(i));
                }
            }
        });
    }

    private void doAction(String actionCommand)
    {
        if (actionCommand.equals("comboBoxChanged"))
        {
            lastResult = null;
            return;
        }
        if (actionCommand.equals("comboBoxEdited")) actionCommand = "find";
        try
        {
            MAIN_GUI.asmContents.removeAllLineHighlights();
            for (int i = 0; i < MAIN_GUI.includeFiles.getTabCount(); i++) ((RTextScrollPane) MAIN_GUI.includeFiles.getComponentAt(i)).getTextArea().removeAllLineHighlights();
            boolean replace = actionCommand.startsWith("replace");
            boolean all = actionCommand.endsWith("All");
            ArrayList<Result> results = gather(all);
            for (Result result : results) getTextAreaFromFileId(result.fileId).addLineHighlight(result.lineNr, Color.YELLOW);
            DefaultMutableTreeNode node = getDefaultMutableTreeNode(results, all);
            rootNode.insert(node, 0);
            resultsTree.expandPath(new TreePath(node.getPath()));
            resultsTree.updateUI();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private DefaultMutableTreeNode getDefaultMutableTreeNode(ArrayList<Result> results, boolean all)
    {
        DefaultMutableTreeNode node;
        if (results.isEmpty()) node = new DefaultMutableTreeNode(getTitle(0, null));
        else if (!all && results.size() == 1)
        {
            Result result = results.get(0);
            node = new DefaultMutableTreeNode(result);
            result.select();
        }
        else
        {
            node = new DefaultMutableTreeNode(getTitle(results.size(), null));
            for (Result result : results) node.add(new DefaultMutableTreeNode(result));
        }
        return node;
    }

    private String getTitle(int nodes, Result result)
    {
        StringBuilder out = new StringBuilder();
        if (result == null || nodes <= 1)
        {
            out.append('"').append(findBox.getSelectedItem()).append('"');
            if (replaceBox.getSelectedItem() != null) out.append(" -> ").append('"').append(replaceBox.getSelectedItem()).append('"');
            out.append(' ');
        }
        if (nodes == 0) out.append("No matches.");
        else if (result == null) out.append(nodes).append(" matches.");
        return out.toString();
    }

    private ArrayList<Result> gather(boolean all) throws BadLocationException
    {
        ArrayList<Result> results = new ArrayList<>();
        if (regexRadioButton.isSelected())
        {
            Pattern pattern = Pattern.compile(findBox.getSelectedItem().toString(), ignoreCaseCheckBox.isSelected() ? Pattern.CASE_INSENSITIVE : 0x00);
            //pattern.matcher(searchText);
        }
        else
        {
            Result tmp = (lastResult = findPlain(all ? null : lastResult));
            if (tmp != null) results.add(tmp);
            if (!all || tmp == null) return results;
            while ((tmp = findPlain(tmp)) != null) results.add(tmp);
        }
        return results;
    }

    private static RTextArea getTextAreaFromFileId(int fileid)
    {
        return fileid != -1 ? (((RTextScrollPane) MAIN_GUI.includeFiles.getComponentAt(fileid))).getTextArea() : MAIN_GUI.asmContents;
    }

    private Result findPlain(Result prev) throws BadLocationException
    {
        Result result = new Result();
        result.fileId = prev != null ? prev.fileId : -1;
        int index;
        do
        {
            int startPos = prev != null && prev.fileId == result.fileId ? prev.endPos : 0;
            RTextArea textArea = getTextAreaFromFileId(result.fileId);
            Document document = textArea.getDocument();
            String searchText = document.getText(startPos, document.getLength() - startPos);
            result.findText = String.valueOf(findBox.getSelectedItem());
            result.replaceText = String.valueOf(replaceBox.getSelectedItem());
            if (!ignoreCaseCheckBox.isSelected()) index = searchText.indexOf(result.findText);
            else index = searchText.toLowerCase().indexOf(result.findText.toLowerCase());
            result.startPos = startPos + index;
            result.endPos = result.startPos + result.findText.length();
            result.lineNr = textArea.getLineOfOffset(result.startPos);
        }
        while (index == -1 && MAIN_GUI.includeFiles.getTabCount() > ++result.fileId);
        if (index == -1) return null;
        return result;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        contentPane = new JPanel();
        contentPane.setLayout(new GridBagLayout());
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel1, gbc);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel2, gbc);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel3, gbc);
        panel3.setBorder(BorderFactory.createTitledBorder("Search modes"));
        normalRadioButton = new JRadioButton();
        normalRadioButton.setText("Normal");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(normalRadioButton, gbc);
        regexRadioButton = new JRadioButton();
        regexRadioButton.setText("Regex");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(regexRadioButton, gbc);
        ignoreCaseCheckBox = new JCheckBox();
        ignoreCaseCheckBox.setSelected(true);
        ignoreCaseCheckBox.setText("Ignore case");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel3.add(ignoreCaseCheckBox, gbc);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(panel4, gbc);
        panel4.setBorder(BorderFactory.createTitledBorder("Search domain"));
        srcIncludesRadioButton = new JRadioButton();
        srcIncludesRadioButton.setText("Src + includes");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(srcIncludesRadioButton, gbc);
        selectionOnlyRadioButton = new JRadioButton();
        selectionOnlyRadioButton.setText("Selection only");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(selectionOnlyRadioButton, gbc);
        srcOnlyRadioButton = new JRadioButton();
        srcOnlyRadioButton.setText("Src only");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel4.add(srcOnlyRadioButton, gbc);
        alwaysOnTopCheckBox = new JCheckBox();
        alwaysOnTopCheckBox.setSelected(true);
        alwaysOnTopCheckBox.setText("Always on top");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel2.add(alwaysOnTopCheckBox, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel2.add(spacer1, gbc);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panel5, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Find:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 5, 10);
        panel5.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Replace:");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 10, 10);
        panel5.add(label2, gbc);
        findBox = new JComboBox();
        findBox.setEditable(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 5, 10);
        panel5.add(findBox, gbc);
        replaceBox = new JComboBox();
        replaceBox.setEditable(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 10, 10);
        panel5.add(replaceBox, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer2, gbc);
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(panel6, gbc);
        findNextButton = new JButton();
        findNextButton.setActionCommand("find");
        findNextButton.setHideActionText(false);
        findNextButton.setText("Find next");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        panel6.add(findNextButton, gbc);
        findAllButton = new JButton();
        findAllButton.setActionCommand("findAll");
        findAllButton.setText("Find all");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        panel6.add(findAllButton, gbc);
        replaceButton = new JButton();
        replaceButton.setActionCommand("replace");
        replaceButton.setText("Replace");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        panel6.add(replaceButton, gbc);
        closeButton = new JButton();
        closeButton.setText("Close");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        panel6.add(closeButton, gbc);
        replaceAllButton = new JButton();
        replaceAllButton.setActionCommand("replaceAll");
        replaceAllButton.setText("Replace all");
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 2, 2, 2);
        panel6.add(replaceAllButton, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        contentPane.add(scrollPane1, gbc);
        resultsTree = new JTree();
        resultsTree.setEditable(false);
        scrollPane1.setViewportView(resultsTree);
        ButtonGroup buttonGroup;
        buttonGroup = new ButtonGroup();
        buttonGroup.add(srcIncludesRadioButton);
        buttonGroup.add(srcOnlyRadioButton);
        buttonGroup.add(selectionOnlyRadioButton);
        buttonGroup = new ButtonGroup();
        buttonGroup.add(normalRadioButton);
        buttonGroup.add(regexRadioButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return contentPane;
    }

    public static final class Result
    {
        public String findText;
        public String replaceText;
        public int    startPos;
        public int    endPos;
        public int fileId = -1;
        public int lineNr;

        @Override
        public String toString()
        {
            StringBuilder out = new StringBuilder().append("Line: ").append(lineNr + 1).append(" \tFile: ").append('"');
            if (fileId == -1) out.append(FilenameUtils.getBaseName(Main.srcFile.getName()));
            else out.append(MAIN_GUI.includeFiles.getTitleAt(fileId));
            out.append('"');
            return out.toString();
        }

        public void select()
        {
            if (fileId == -1) MAIN_GUI.tabPane.setSelectedIndex(0);
            else
            {
                MAIN_GUI.tabPane.setSelectedIndex(1);
                MAIN_GUI.includeFiles.setSelectedIndex(fileId);
            }
            RTextArea textArea = getTextAreaFromFileId(fileId);
            textArea.setSelectionStart(startPos);
            textArea.setSelectionEnd(endPos);
        }
    }

}
