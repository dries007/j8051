///*
// * Copyright (c) 2014, Dries007
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// *  Redistributions of source code must retain the above copyright notice, this
// *   list of conditions and the following disclaimer.
// *
// *  Redistributions in binary form must reproduce the above copyright notice,
// *   this list of conditions and the following disclaimer in the documentation
// *   and/or other materials provided with the distribution.
// *
// *  Neither the name of the project nor the names of its
// *   contributors may be used to endorse or promote products derived from
// *   this software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//
//package net.dries007.j8051.util;
//
//import net.dries007.j8051.Main;
//import net.dries007.j8051.gui.MainGui;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.*;
//
//import static net.dries007.j8051.gui.AsmDocumentListener.DOCUMENT_LISTENER;
//
///**
// * @author Dries007
// */
//public class FileWatcher extends Thread
//{
//    private final Path         fileName;
//    private final WatchService watcher;
//
//    private boolean running = true;
//
//    public FileWatcher(File file) throws IOException
//    {
//        super("FileWatcher-" + file.getName());
//        fileName = Paths.get(file.getName());
//        Path path = Paths.get(file.toURI()).getParent();
//        watcher = path.getFileSystem().newWatchService();
//        path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
//    }
//
//    @Override
//    public void run()
//    {
//        try
//        {
//            long prevLastModified = 0L, lastModified;
//            WatchKey key = watcher.take();
//            while (key != null && running)
//            {
//                for (WatchEvent event : key.pollEvents())
//                {
//                    if (!running) break;
//                    if (event.context().toString().equals(Main.srcFile.getName()))
//                    {
//                        lastModified = Main.srcFile.lastModified();
//                        if (System.currentTimeMillis() - DOCUMENT_LISTENER.lastUpdate > 1100 && lastModified - prevLastModified > 10)
//                        {
//                            prevLastModified = lastModified;
//                            if (MainGui.MAIN_GUI.isAutoUpdating()) MainGui.MAIN_GUI.setAsmContents();
//                            if (MainGui.MAIN_GUI.isAutoCompiling()) MainGui.MAIN_GUI.compile();
//                        }
//                    }
//                }
//                key.reset();
//                key = watcher.take();
//            }
//        }
//        catch (InterruptedException ignored)
//        {
//            running = false;
//        }
//        System.out.println("END OF THREAD: FileWatcher");
//    }
//}
