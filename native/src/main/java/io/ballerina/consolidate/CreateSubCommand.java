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
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.StringJoiner;

@CommandLine.Command(name = "create",
        description = "Creates a Ballerina consolidator project for the given services")
public class CreateSubCommand implements BLauncherCmd {
    private final PrintStream outStream;
    private final PrintStream errStream;

    @CommandLine.Option(names = {"--services"}, split = ",", defaultValue = "", required = true)
    private String[] services;

    @CommandLine.Option(names = {"--name"}, defaultValue = "consolidator")
    private String packageName;

    @CommandLine.Option(names = {"--help"})
    private boolean help;

    public CreateSubCommand() {
        this.outStream = System.out;
        this.errStream = System.err;
    }

    @Override
    public void execute() {
        Util.validatePackageName(packageName, outStream);
        try {
            if (!Util.validateServicesInput(services, errStream)) {
                CommandUtil.exitError(true);
                return;
            }
            createProject(Paths.get(packageName));
        } catch (IOException | URISyntaxException e) {
            CommandUtil.printError(this.errStream, e.getMessage(), null, false);
            CommandUtil.exitError(true);
        }
    }

    private void createProject(Path packageDir) throws IOException, URISyntaxException {
        outStream.println("Generating the consolidator project for");
        for (String service : services) {
            outStream.println("\t" + service);
        }
        Files.createDirectories(packageDir);
        CommandUtil.initPackageByTemplate(packageDir, packageName, "default", true);

        StringJoiner options = new StringJoiner(",");
        for (String service : services) {
            options.add("\"" + service + "\"");
        }
        String toolEntry = "\n[[tool.consolidator]]\n" + "id = " + "\"consolidate1\"\n" +
                "options.services = [" +
                options + "]";

        Files.writeString(packageDir.resolve("Ballerina.toml"), toolEntry, StandardOpenOption.APPEND);
        outStream.println("\nSuccessfully created the consolidator project at '" + packageName + "'.\n");
        outStream.println("What's next? \n\t Execute 'bal build " + packageName + "' to generate the executable.");
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
