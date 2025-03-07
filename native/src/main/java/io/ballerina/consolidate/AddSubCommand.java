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
import java.util.Optional;
import java.util.Set;

import static io.ballerina.consolidate.Util.ADD;
import static io.ballerina.consolidate.Util.HYPHEN;

@CommandLine.Command(name = "add",
        description = "Adds a Ballerina consolidator project for the given services")
public class AddSubCommand implements BLauncherCmd {
    private final PrintStream outStream;
    private final PrintStream errStream;
    boolean exit;

    @CommandLine.Parameters (arity = "0..1")
    private String servicesStr;

    @CommandLine.Option(names = {"--help"})
    private boolean help;

    public AddSubCommand() {
        this.outStream = System.out;
        this.errStream = System.err;
    }

    public AddSubCommand(PrintStream printStream) {
        this.outStream = printStream;
        this.errStream = printStream;
        this.help = true;
    }

    public AddSubCommand(PrintStream printStream, String servicesStr, boolean exit) {
        this.outStream = printStream;
        this.errStream = printStream;
        this.servicesStr = servicesStr;
        this.exit = exit;
        CommandUtil.initJarFs();
    }

    @Override
    public void execute() {
        if (help || servicesStr == null) {
            outStream.println(Util.getHelpText(getName()));
            return;
        }

        Optional<Set<String>> services;
        try {
            services = Util.getServices(servicesStr, ADD, errStream);
            if (services.isEmpty()) {
                CommandUtil.exitError(this.exit);
                return;
            }
        } catch (Exception e) {
            CommandUtil.printError(this.errStream, "Failed to extract the services. ", null, false);
            CommandUtil.exitError(this.exit);
            return;
        }
        try {
            addServicesToProject(services.get());
        } catch (IOException e) {
            CommandUtil.printError(this.errStream, e.getMessage(), null, false);
            CommandUtil.exitError(this.exit);
        }
    }

    private void addServicesToProject(Set<String> services) throws IOException {
        outStream.println("Updating the consolidator package to add");
        for (String service : services) {
            outStream.println("\t" + service);
        }

        try {
            BuildProject buildProject = BuildProject.load(Paths.get(System.getProperty("user.dir")));
            if (buildProject.currentPackage().ballerinaToml().isEmpty()) {
                CommandUtil.printError(this.errStream, "Invalid package provided",
                        null, false);
                CommandUtil.exitError(this.exit);
            }
            Set<String> allServices = new HashSet<>();
            for (PackageManifest.Tool tool : buildProject.currentPackage().manifest().tools()) {
                if (Util.TOOL_NAME.equals(tool.type().value())) {
                    Set<String> existingServices = Util.getServices(tool.optionsTable());
                    allServices.addAll(existingServices);
                    break;
                }
            }
            allServices.addAll(services);
            Path balTomlPath = buildProject.sourceRoot().resolve(Util.BALLERINA_TOML);
            Util.replaceServicesArrayInToml(allServices, balTomlPath);

        } catch (ProjectException e) {
            CommandUtil.printError(this.errStream, "Current directory is not a valid Ballerina package",
                    null, false);
            CommandUtil.exitError(this.exit);
        }
        outStream.println("\nSuccessfully added the services to the package.\n");
        outStream.println("What's next?\n\t Execute 'bal build' to generate the executable.");
    }
    @Override
    public String getName() {
        return Util.TOOL_NAME + HYPHEN + ADD;
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
