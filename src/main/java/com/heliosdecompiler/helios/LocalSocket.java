/*
 * Copyright 2017 Sam Sun <github-contact@samczsun.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.heliosdecompiler.helios;

import com.heliosdecompiler.helios.handler.ExceptionHandler;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LocalSocket extends ServerSocket implements Runnable {
    public LocalSocket() throws IOException {
        super(21354);
        new Thread(this, "Inter-Process Communications Socket").start();
    }

    public void run() {
        while (true) {
            try {
                Socket socket = this.accept();
                String args = IOUtils.toString(socket.getInputStream(), "UTF-8");
//                Helios.getGui().getShell().getDisplay().asyncExec(() -> {
//                    Helios.getGui().getShell().setFocus();
//                    Helios.getGui().getShell().forceActive();
//                    Helios.getGui().getShell().forceFocus();
////                    Helios.handleCommandLine(args.split(" "));
//                });
            } catch (Throwable e) {
                ExceptionHandler.handle(e);
            }
        }
    }
}
