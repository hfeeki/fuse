/*
 * Copyright (C) FuseSource, Inc.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.process.manager.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.fusesource.process.manager.Installation;
import org.fusesource.process.manager.commands.support.ProcessCommandSupport;

/**
 * Installs a new process
 */
@Command(name = "install", scope = "process", description = "Installs a managed process into this container.")
public class Install extends ProcessCommandSupport {
    @Argument(required = true, name = "installation URL", description = "The URL of the installation distribution to install. Typically this is a tarball or zip file")
    protected String url;

    @Override
    protected Object doExecute() throws Exception {
        checkRequirements();
        Installation install = getProcessManager().install(url);

        System.out.println("Installed process " + install.getId() + " to " + install.getInstallDir());
        return null;
    }
}