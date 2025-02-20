/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.consolidate;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.cmd.CommandUtil;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

@CommandLine.Command(name = "remove",
        description = "Removes a Ballerina consolidator project for the given services")
public class RemoveSubCommand implements BLauncherCmd {
    private final PrintStream printStream;
    private final PrintStream errStream;

    @CommandLine.Option(names = {"--services"}, split = ",", defaultValue = "", required = true)
    private String[] services;

    @CommandLine.Option(names = {"--help"})
    private boolean help;

    public RemoveSubCommand() {
        this.printStream = System.out;
        this.errStream = System.err;
    }

    @Override
    public void execute() {
        try {
            if (!Util.validateServicesInput(services, errStream)) {
                CommandUtil.exitError(true);
                return;
            }
            removeServicesFromProject(services);
        } catch (IOException e) {
            CommandUtil.printError(this.errStream, e.getMessage(), null, false);
            CommandUtil.exitError(true);
        }
    }

    private void removeServicesFromProject(String[] services) throws IOException {
        printStream.println("Updating the consolidator project to remove");
        Set<String> rmServices = new HashSet<>();
        for (String service : services) {
            printStream.println("\t" + service);
            rmServices.add(service);
        }

        try {
            BuildProject buildProject = BuildProject.load(Paths.get(System.getProperty("user.dir")));
            if (buildProject.currentPackage().ballerinaToml().isEmpty()) {
                CommandUtil.printError(this.errStream, "Invalid project provided",
                        null, false);
                CommandUtil.exitError(true);
            }

            Path balTomlPath = buildProject.sourceRoot().resolve("Ballerina.toml");
            for (PackageManifest.Tool tool : buildProject.currentPackage().manifest().tools()) {
                if ("consolidator".equals(tool.type().value())) {
                    Set<String> allServices = Util.getServices(tool.optionsTable());
                    allServices.removeAll(rmServices);
                    Util.replaceServicesArrayInToml(allServices, balTomlPath);
                   break;
                }
            }




        } catch (ProjectException e) {
            CommandUtil.printError(this.errStream, "Current directory is not a valid Ballerina package",
                    null, false);
            CommandUtil.exitError(true);
        }
        printStream.println("\nSuccessfully removed the services from the project.\n");
        printStream.println("What's next? \n\t Execute 'bal build' to generate the executable.");
    }
    @Override
    public String getName() {
        return "";
    }

    @Override
    public void printLongDesc(StringBuilder stringBuilder) {

    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {

    }

    @Override
    public void setParentCmdParser(CommandLine commandLine) {

    }
}
