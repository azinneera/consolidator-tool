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
import picocli.CommandLine;

import java.io.PrintStream;

@CommandLine.Command(name = Util.TOOL_NAME,
        subcommands = {NewSubCommand.class, AddSubCommand.class, RemoveSubCommand.class},
        description = "Generates a Ballerina consolidator project for the given package"
)
public class ConsolidatePackagesCommand implements BLauncherCmd {
    private final PrintStream printStream;

    @CommandLine.Option(names = {"--help"})
    private boolean help;

    public ConsolidatePackagesCommand() {
        this.printStream = System.out;
    }

    public ConsolidatePackagesCommand(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public void execute() {
        printStream.println(Util.getHelpText(getName()));
    }

    @Override
    public String getName() {
        return Util.TOOL_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder stringBuilder) {
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
    }

    @Override
    public void setParentCmdParser(picocli.CommandLine commandLine) {
    }
}
