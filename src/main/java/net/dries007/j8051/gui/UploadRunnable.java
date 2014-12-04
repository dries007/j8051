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
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.io.File;
import java.util.StringTokenizer;

import static net.dries007.j8051.util.Constants.PROPERTIES;
import static net.dries007.j8051.util.Constants.UPLOADCMD;

/**
 * @author Dries007
 */
public class UploadRunnable implements Runnable
{
    public boolean running;

    public UploadRunnable()
    {

    }

    @Override
    public void run()
    {
        running = true;
        try
        {
            if (!PROPERTIES.containsKey(UPLOADCMD)) return;
            File file = new File(Main.srcFile.getParentFile(), FilenameUtils.getBaseName(Main.srcFile.getName()) + ".hex");
            StringTokenizer st = new StringTokenizer(PROPERTIES.getProperty(UPLOADCMD).replace("$filename", "\"" + file.getAbsolutePath() + "\""));
            String[] cmdarray = new String[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++) cmdarray[i] = st.nextToken();
            Process process = new ProcessBuilder(cmdarray).inheritIO().start();
            if (process.waitFor() != 0) JOptionPane.showMessageDialog(MainGui.MAIN_GUI.frame, "Upload process didn't exit with 0, but with " + process.exitValue(), "Upload error.", JOptionPane.ERROR_MESSAGE);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        running = false;
    }
}
