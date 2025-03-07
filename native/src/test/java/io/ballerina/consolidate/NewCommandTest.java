/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.consolidate;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static io.ballerina.consolidate.TestUtil.getOutput;
import static io.ballerina.consolidate.TestUtil.readOutput;
import static io.ballerina.consolidate.TestUtil.testResources;

public class NewCommandTest {

    private ByteArrayOutputStream console;
    private PrintStream printStream;

    @BeforeClass
    public void setup() {
        this.console = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.console);
    }

    @Test
    public void testHelp() throws IOException {
        NewSubCommand newSubCommand = new NewSubCommand(printStream);
        newSubCommand.execute();
        String buildLog = readOutput(console);
        String expected = getOutput(testResources.resolve("command-outputs"), "help-new.txt");
        Assert.assertTrue(buildLog.contains(expected), "Help text mismatched");
    }

    @Test
    public void testCreate() throws IOException {
        String services = "myorg/service1,myorg/service2";
        String projectPath = "build/consolidator";
        NewSubCommand newSubCommand = new NewSubCommand(printStream, projectPath, services, false);
        newSubCommand.execute();
        String buildLog = readOutput(console);
        String expected = getOutput(testResources.resolve("command-outputs"), "new.txt");
        Assert.assertTrue(buildLog.contains(expected), "Actual output:" + buildLog);
//        balBuildAfter(projectPath);
    }

    @Test
    public void testCreateWithInvalidSvcValues() throws IOException {
        String services = "service1,service2";
        String projectPath = "build/consolidator";
        NewSubCommand newSubCommand = new NewSubCommand(printStream, projectPath, services, false);
        newSubCommand.execute();
        String buildLog = readOutput(console);
        String expected = getOutput(testResources.resolve("command-outputs"), "invalid-svc.txt");
        Assert.assertTrue(buildLog.contains(expected));
    }
}
