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
 */

package net.dries007.j8051.gui;

import net.dries007.j8051.Main;
import net.dries007.j8051.compiler.Compiler;
import net.dries007.j8051.util.FileWatcher;
import net.dries007.j8051.util.JFontChooser;
import net.dries007.j8051.util.exceptions.CompileException;
import org.apache.commons.io.FileUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import static net.dries007.j8051.util.AsmDocumentListener.DOCUMENT_LISTENER;
import static net.dries007.j8051.util.Constants.*;

/**
 * @author Dries007
 */
public class MainGui
{
    public static final MainGui MAIN_GUI = new MainGui();
    private static FileWatcher fileWatcher;
    public final JFileChooser fileChooser   = new JFileChooser();
    public final JFileChooser folderChooser = new JFileChooser();
    public final JFontChooser fontChooser   = new JFontChooser();
    private final JFrame            frame;
    private       JTabbedPane       tabPane;
    private       JMenuItem         loadFile;
    private       JMenuItem         saveFile;
    private       JMenuItem         changeFont;
    private       JMenuItem         changeTabSize;
    private       JPanel            root;
    private       JMenuBar          menuBar;
    private       RSyntaxTextArea   asmContents;
    private       JTextArea         preText;
    private       JTable            constantsTable;
    private       JTable            componentsTable;
    private JTable            hexTable;
    private JCheckBoxMenuItem autoLoad;
    private JCheckBoxMenuItem autoSave;
    private JCheckBoxMenuItem autoCompile;
    private JMenuItem         includeFolder;
    private int compilerId = 0;

    private MainGui()
    {
        $$$setupUI$$$();

//        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
//        atmf.putMapping("text/8051", AsmTokenMaker.class.getName());
//        asmContents.setSyntaxEditingStyle("text/8051");

        fontChooser.setSelectedFontFamily(PROPERTIES.getProperty(FONT_NAME, "Courier New"));
        fontChooser.setSelectedFontStyle(Integer.parseInt(PROPERTIES.getProperty(FONT_STYLE, Integer.toString(Font.PLAIN))));
        fontChooser.setSelectedFontSize(Integer.parseInt(PROPERTIES.getProperty(FONT_SIZE, "12")));

        asmContents.setTabSize(Integer.parseInt(PROPERTIES.getProperty(TABSIZE, "4")));
        asmContents.setFont(fontChooser.getSelectedFont());

        preText.setTabSize(Integer.parseInt(PROPERTIES.getProperty(TABSIZE, "4")));
        preText.setFont(fontChooser.getSelectedFont());

        // DEBUG todo

        componentsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
        {
            private final Color HIGHLIGHT_COLOR = Color.YELLOW;

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null && value.equals("UnsolvedComponent"))
                {
                    if (isSelected)
                    {
                        comp.setBackground(new Color((HIGHLIGHT_COLOR.getRGB() ^ comp.getBackground().getRGB())));
                        comp.setForeground(new Color(Color.BLACK.getRGB() ^ comp.getForeground().getRGB()));
                    }
                    else
                    {
                        comp.setBackground(HIGHLIGHT_COLOR);
                        comp.setForeground(Color.BLACK);
                    }
                }
                else
                {
                    if (!isSelected)
                    {
                        comp.setBackground(Color.WHITE);
                        comp.setForeground(Color.BLACK);
                    }
                }
                return comp;
            }
        });

        hexTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer()
        {
            private final Color HIGHLIGHT_COLOR = Color.YELLOW;

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
            {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null && value.toString().length() > 2 && column != 0)
                {
                    if (isSelected)
                    {
                        comp.setBackground(new Color((HIGHLIGHT_COLOR.getRGB() ^ comp.getBackground().getRGB())));
                        comp.setForeground(new Color(Color.BLACK.getRGB() ^ comp.getForeground().getRGB()));
                    }
                    else
                    {
                        comp.setBackground(HIGHLIGHT_COLOR);
                        comp.setForeground(Color.BLACK);
                    }
                }
                else
                {
                    if (!isSelected)
                    {
                        comp.setBackground(Color.WHITE);
                        comp.setForeground(Color.BLACK);
                    }
                }
                return comp;
            }
        });

        // Main gui init

        frame = new JFrame("j8051");
        frame.setContentPane(this.$$$getRootComponent$$$());
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setLocation(parseInt(PROPERTIES.getProperty(WINDOW_X, "0")), parseInt(PROPERTIES.getProperty(WINDOW_Y, "0")));
        frame.setSize(parseInt(PROPERTIES.getProperty(WINDOW_W, "740")), parseInt(PROPERTIES.getProperty(WINDOW_H, "760")));
        frame.setVisible(true);
        frame.addComponentListener(new ComponentListener()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                PROPERTIES.setProperty(WINDOW_W, Integer.toString(e.getComponent().getWidth()));
                PROPERTIES.setProperty(WINDOW_H, Integer.toString(e.getComponent().getHeight()));
            }

            @Override
            public void componentMoved(ComponentEvent e)
            {
                PROPERTIES.setProperty(WINDOW_X, Integer.toString(e.getComponent().getX()));
                PROPERTIES.setProperty(WINDOW_Y, Integer.toString(e.getComponent().getY()));
            }

            @Override
            public void componentShown(ComponentEvent e)
            {
            }

            @Override
            public void componentHidden(ComponentEvent e)
            {
            }
        });
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                super.windowClosed(e);
                fileWatcher.interrupt();
            }
        });
        loadFile.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
                {
                    File file = fileChooser.getSelectedFile();
                    Main.setSrcFile(file);
                    changeFile();
                }
            }
        });
        saveFile.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                saveChanges();
            }
        });
        changeFont.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (JFontChooser.OK_OPTION == fontChooser.showDialog(frame))
                {
                    asmContents.setFont(fontChooser.getSelectedFont());
                    PROPERTIES.setProperty(FONT_NAME, fontChooser.getSelectedFontFamily());
                    PROPERTIES.setProperty(FONT_SIZE, Integer.toString(fontChooser.getSelectedFontSize()));
                    PROPERTIES.setProperty(FONT_STYLE, Integer.toString(fontChooser.getSelectedFontStyle()));
                }
            }
        });
        autoLoad.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PROPERTIES.setProperty(AUTO_LOAD, Boolean.toString(autoLoad.getState()));
            }
        });
        autoCompile.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PROPERTIES.setProperty(AUTO_COMPILE, Boolean.toString(autoLoad.getState()));
            }
        });
        autoSave.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PROPERTIES.setProperty(AUTO_SAVE, Boolean.toString(autoSave.getState()));
            }
        });
        changeTabSize.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    int i = Integer.parseInt(JOptionPane.showInputDialog(frame, "Tab size?", "Tab size", JOptionPane.QUESTION_MESSAGE));
                    PROPERTIES.setProperty(TABSIZE, Integer.toString(i));
                    asmContents.setTabSize(i);
                }
                catch (NumberFormatException ex)
                {
                    ex.printStackTrace();
                }
            }
        });
        includeFolder.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (folderChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
                {
                    Main.setIncludeFolder(folderChooser.getSelectedFile());
                }
            }
        });
    }

    public void init()
    {
        changeFile();
        compile();
        DOCUMENT_LISTENER.start();
        asmContents.getDocument().addDocumentListener(DOCUMENT_LISTENER);

        if (PROPERTIES.containsKey(SRC_FILE))
        {
            File file = new File(PROPERTIES.getProperty(SRC_FILE));
            if (file.exists()) fileChooser.setSelectedFile(file);
        }
    }

    public void changeFile()
    {
        if (fileWatcher != null)
        {
            fileWatcher.interrupt();
        }
        if (Main.srcFile != null)
        {
            frame.setTitle(String.format("j8051 - %s", Main.srcFile.getAbsolutePath()));
            try
            {
                fileWatcher = new FileWatcher(Main.srcFile);
                fileWatcher.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        asmContents.setEditable(Main.srcFile != null);
        setAsmContents();
    }

    public void setAsmContents()
    {
        Document document = asmContents.getDocument();
        try
        {
            int selectStart = asmContents.getSelectionStart(), selectEnd = asmContents.getSelectionEnd();
            document.remove(0, document.getLength());
            if (Main.srcFile != null)
            {
                document.insertString(0, FileUtils.readFileToString(Main.srcFile, PROPERTIES.getProperty(ENCODING, "Cp1252")), null);
                asmContents.setSelectionStart(selectStart);
                asmContents.setSelectionEnd(selectEnd);
            }
        }
        catch (BadLocationException | IOException e1)
        {
            e1.printStackTrace();
        }
    }

    public void compile()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    System.gc();

                    final Compiler compiler = new Compiler(asmContents.getText());

                    while (compiler.hasWork())
                    {
                        compiler.doWork();

                        preText.setText(compiler.src);
                        constantsTable.setModel(new DefaultTableModel(compiler.getSymbols(), new String[]{"Name", "Type", "Value (Hex)", "Value (dec)", "Value (String)"}));
                        resizeColumnWidth(constantsTable);
                        constantsTable.updateUI();

                        componentsTable.setModel(new DefaultTableModel(compiler.getComponents(), new String[]{"Start", "End", "Type", "SubType", "Contents", "Address", "Bytes"}));
                        resizeColumnWidth(componentsTable);
                        componentsTable.updateUI();

                        hexTable.setModel(new DefaultTableModel(compiler.getHexTable(), new String[]{"Address    ", "0x.0", "0x.1", "0x.2", "0x.3", "0x.4", "0x.5", "0x.6", "0x.7", "0x.8", "0x.9", "0x.A", "0x.B", "0x.C", "0x.D", "0x.E", "0x.F"}));
                        resizeColumnWidth(hexTable);
                        hexTable.updateUI();
                    }
                }
                catch (Exception e)
                {
                    if (e instanceof CompileException)
                    {
                        preText.select(((CompileException) e).component.getSrcStart(), ((CompileException) e).component.getSrcEnd());
                        System.out.println(((CompileException) e).component);
                    }
                    e.printStackTrace();
                }
            }
        }, "Compiler-" + compilerId++).start();
    }

    public void resizeColumnWidth(JTable table)
    {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++)
        {
            int width = 50; // Min width
            for (int row = 0; row < table.getRowCount(); row++)
            {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width, width);
            }
            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }

    private void createUIComponents()
    {
        // Filechooser specifications
        fileChooser.addChoosableFileFilter(ASM_FILE_FILTER);
        fileChooser.setFileHidingEnabled(true);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileFilter(ASM_FILE_FILTER);

        // Folderchooser specifications
        folderChooser.addChoosableFileFilter(FOLDER_FILTER);
        folderChooser.setFileHidingEnabled(true);
        folderChooser.setMultiSelectionEnabled(false);
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setFileFilter(FOLDER_FILTER);

        //Menubar
        menuBar = new JMenuBar();
        //  Filemenu
        JMenu fileMenu = new JMenu("File");

        loadFile = new JMenuItem("Open...");
        fileMenu.add(loadFile);

        saveFile = new JMenuItem("Save");
        fileMenu.add(saveFile);

        fileMenu.addSeparator();

        autoLoad = new JCheckBoxMenuItem("Auto load changes from disk");
        autoLoad.setState(parseBoolean(PROPERTIES.getProperty(AUTO_LOAD, "true")));
        fileMenu.add(autoLoad);

        autoSave = new JCheckBoxMenuItem("Auto save");
        autoSave.setState(parseBoolean(PROPERTIES.getProperty(AUTO_SAVE, "true")));
        fileMenu.add(autoSave);

        autoCompile = new JCheckBoxMenuItem("Auto compile");
        autoCompile.setState(parseBoolean(PROPERTIES.getProperty(AUTO_COMPILE, "true")));
        fileMenu.add(autoCompile);

        menuBar.add(fileMenu);
        //  Viewmenu
        JMenu viewMenu = new JMenu("View");

        changeFont = new JMenuItem("Font...");
        viewMenu.add(changeFont);

        changeTabSize = new JMenuItem("Tab size...");
        viewMenu.add(changeTabSize);

        menuBar.add(viewMenu);

        //  optionsmenu
        JMenu optionsMenu = new JMenu("Options");

        includeFolder = new JMenuItem("Include folder");
        optionsMenu.add(includeFolder);

        menuBar.add(optionsMenu);
    }

    public boolean isAutoUpdating()
    {
        return autoLoad.getState();
    }

    public boolean isAutoCompiling()
    {
        return autoCompile.getState();
    }

    public boolean isAutoSaving()
    {
        return autoSave.getState();
    }

    public void saveChanges()
    {
        try
        {
            FileUtils.writeStringToFile(Main.srcFile, asmContents.getDocument().getText(0, asmContents.getDocument().getLength()), PROPERTIES.getProperty(ENCODING, "Cp1252"));
        }
        catch (IOException | BadLocationException e1)
        {
            e1.printStackTrace();
        }
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
        createUIComponents();
        root = new JPanel();
        root.setLayout(new GridBagLayout());
        tabPane = new JTabbedPane();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        root.add(tabPane, gbc);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        tabPane.addTab("ASM file", panel1);
        final RTextScrollPane rTextScrollPane1 = new RTextScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(rTextScrollPane1, gbc);
        rTextScrollPane1.setBorder(BorderFactory.createTitledBorder("Source"));
        asmContents = new RSyntaxTextArea();
        asmContents.setFadeCurrentLineHighlight(false);
        asmContents.setTabsEmulated(false);
        asmContents.setText("");
        rTextScrollPane1.setViewportView(asmContents);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridBagLayout());
        tabPane.addTab("Pre-processed", panel2);
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setVerticalScrollBarPolicy(22);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel2.add(scrollPane1, gbc);
        preText = new JTextArea();
        preText.setEditable(false);
        preText.setEnabled(true);
        scrollPane1.setViewportView(preText);
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridBagLayout());
        tabPane.addTab("Components", panel3);
        final JScrollPane scrollPane2 = new JScrollPane();
        scrollPane2.setVerticalScrollBarPolicy(22);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel3.add(scrollPane2, gbc);
        componentsTable = new JTable();
        componentsTable.setAutoCreateRowSorter(false);
        componentsTable.setAutoResizeMode(3);
        scrollPane2.setViewportView(componentsTable);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridBagLayout());
        tabPane.addTab("Symbols", panel4);
        panel4.setBorder(BorderFactory.createTitledBorder("Symbols"));
        final JScrollPane scrollPane3 = new JScrollPane();
        scrollPane3.setVerticalScrollBarPolicy(22);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel4.add(scrollPane3, gbc);
        constantsTable = new JTable();
        constantsTable.setAutoCreateRowSorter(true);
        constantsTable.setAutoResizeMode(4);
        scrollPane3.setViewportView(constantsTable);
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridBagLayout());
        tabPane.addTab("Hex", panel5);
        final JScrollPane scrollPane4 = new JScrollPane();
        scrollPane4.setVerticalScrollBarPolicy(22);
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel5.add(scrollPane4, gbc);
        hexTable = new JTable();
        hexTable.setAutoResizeMode(0);
        scrollPane4.setViewportView(hexTable);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        root.add(menuBar, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return root;
    }
}
