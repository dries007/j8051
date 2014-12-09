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

import net.dries007.j8051.compiler.Parser;
import net.dries007.j8051.util.Helper;
import net.dries007.j8051.util.exceptions.CompileException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static net.dries007.j8051.gui.MainGui.MAIN_GUI;
import static net.dries007.j8051.util.Constants.*;

/**
 * @author Dries007
 */
class CompileRunnable implements Runnable
{
    public boolean running;

    @Override
    public void run()
    {
        AsmParser.ASM_PARSER.result.clearNotices();
        running = true;
        try
        {
            MAIN_GUI.status.setText("Compiling...");
            System.gc();
            final Parser parser = new Parser(MAIN_GUI.asmContents.getText());
            while (parser.hasWork())
            {
                parser.doWork();
                MAIN_GUI.status.setText(Helper.capitalize(parser.getStage().name()));
                switch (parser.getStage())
                {
                    case PREPROCESSOR:
                        MAIN_GUI.preText.setText(parser.postPre);
                        MAIN_GUI.includeFiles.removeAll();
                        for (String file : parser.includeFiles.keySet())
                        {
                            GridBagConstraints gbc = new GridBagConstraints();
                            gbc.gridx = 0;
                            gbc.gridy = 0;
                            gbc.weightx = 1.0;
                            gbc.weighty = 1.0;
                            gbc.fill = GridBagConstraints.BOTH;
                            RTextScrollPane rTextScrollPane1 = new RTextScrollPane();
                            rTextScrollPane1.setName(file);
                            rTextScrollPane1.setBorder(BorderFactory.createTitledBorder("Source"));
                            RSyntaxTextArea text = new RSyntaxTextArea(parser.includeFiles.get(file));
                            TextLineNumber tln = new TextLineNumber(text);
                            rTextScrollPane1.setRowHeaderView(tln);
                            text.setFadeCurrentLineHighlight(false);
                            text.setTabsEmulated(false);
                            text.setEditable(false);
                            text.setTabSize(Integer.parseInt(PROPERTIES.getProperty(TABSIZE, "4")));
                            text.setFont(MAIN_GUI.fontChooser.getSelectedFont());
                            text.setSyntaxEditingStyle(SYNTAX_NAME);
                            rTextScrollPane1.setViewportView(text);
                            MAIN_GUI.includeFiles.add(rTextScrollPane1, gbc);
                        }
                        break;
                    case MAKE_HEX:
                        MAIN_GUI.hexTable.setModel(new DefaultTableModel(parser.getHexTable(), new String[]{"Address    ", "0x.0", "0x.1", "0x.2", "0x.3", "0x.4", "0x.5", "0x.6", "0x.7", "0x.8", "0x.9", "0x.A", "0x.B", "0x.C", "0x.D", "0x.E", "0x.F"})
                        {
                            @Override
                            public boolean isCellEditable(int row, int column)
                            {
                                return false;
                            }
                        });
                        MAIN_GUI.resizeColumnWidth(MAIN_GUI.hexTable);
                        MAIN_GUI.hexTable.updateUI();
                        break;
                    default:
                        MAIN_GUI.symbolHashMap = parser.symbols;
                        MAIN_GUI.symbolsTable.setModel(new DefaultTableModel(parser.getSymbols(), new String[]{"Name", "Type", "Value (Hex)", "Value (dec)", "Value (String)"})
                        {
                            @Override
                            public boolean isCellEditable(int row, int column)
                            {
                                return false;
                            }
                        });
                        MAIN_GUI.resizeColumnWidth(MAIN_GUI.symbolsTable);
                        MAIN_GUI.symbolsTable.updateUI();

                        MAIN_GUI.componentsTable.setModel(new DefaultTableModel(parser.getComponents(), new String[]{"Line", "Type", "SubType", "Contents", "Address", "Bytes"})
                        {
                            @Override
                            public boolean isCellEditable(int row, int column)
                            {
                                return false;
                            }
                        });
                        MAIN_GUI.resizeColumnWidth(MAIN_GUI.componentsTable);
                        MAIN_GUI.componentsTable.updateUI();
                        break;
                }
            }
        }
        catch (Exception e)
        {
            if (e instanceof CompileException)
            {
                DefaultParserNotice notice = new DefaultParserNotice(AsmParser.ASM_PARSER, e.getMessage(), ((CompileException) e).component.getSrcLine());
                notice.setLevel(ParserNotice.Level.ERROR);
                notice.setShowInEditor(true);
                notice.setToolTipText(e.getMessage());
                AsmParser.ASM_PARSER.result.addNotice(notice);
                MAIN_GUI.status.setText(e.getClass().getSimpleName() + ": " + e.getMessage() + " on line " + ((CompileException) e).component.getSrcLine());
            }
            else
            {
                MAIN_GUI.status.setText(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
            e.printStackTrace();
        }
        MAIN_GUI.asmContents.forceReparsing(AsmParser.ASM_PARSER);
        running = false;
    }
}
