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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Dries007
 */
public class AsmDocumentListener implements DocumentListener
{
    public static final  AsmDocumentListener DOCUMENT_LISTENER = new AsmDocumentListener();
    private static final Timer               TIMER             = new Timer();
    public               boolean             active            = true;
    public               int                 count             = 0;

    private AsmDocumentListener()
    {
        super();
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
        if (!active) return;
        MainGui.MAIN_GUI.status.setText("Waiting for editing pause...");
        count++;
        TIMER.schedule(new Task(), 2500);
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
        if (!active) return;
        MainGui.MAIN_GUI.status.setText("Waiting for editing pause...");
        count++;
        TIMER.schedule(new Task(), 2500);
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
        if (!active) return;
        MainGui.MAIN_GUI.status.setText("Waiting for editing pause...");
        count++;
        TIMER.schedule(new Task(), 2500);
    }

    public static class Task extends TimerTask
    {
        @Override
        public void run()
        {
            DOCUMENT_LISTENER.count--;
            if (DOCUMENT_LISTENER.count != 0) return;

            if (MainGui.MAIN_GUI.isAutoSaving()) MainGui.MAIN_GUI.saveChanges();
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (MainGui.MAIN_GUI.isAutoCompiling()) MainGui.MAIN_GUI.compile();
                }
            }).start();
        }
    }
}
