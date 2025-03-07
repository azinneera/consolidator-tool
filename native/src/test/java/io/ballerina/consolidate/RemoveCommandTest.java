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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;

import static io.ballerina.consolidate.TestUtil.USER_DIR;
import static io.ballerina.consolidate.TestUtil.getOutput;
import static io.ballerina.consolidate.TestUtil.readOutput;
import static io.ballerina.consolidate.TestUtil.testResources;

public class RemoveCommandTest {

    String userDir = System.getProperty(USER_DIR);
    private ByteArrayOutputStream console;
    private PrintStream printStream;

    @BeforeClass
    public void setup() {
        String services = "myorg/service1,myorg/service2,myorg/service3,myorg/service4";
        String projectPath = "build/consolidatorRemoveTest";
        NewSubCommand newSubCommand = new NewSubCommand(System.out, projectPath, services, false);
        newSubCommand.execute();
        System.setProperty(USER_DIR, Paths.get(projectPath).toAbsolutePath().toString());
    }

    @BeforeMethod
    public void clearConsoleLog() {
        this.console = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.console);
    }

    @Test
    public void testHelp() throws IOException {
        RemoveSubCommand removeSubCommand = new RemoveSubCommand(printStream);
        removeSubCommand.execute();
        String buildLog = readOutput(console);
        String expected = getOutput(testResources.resolve("command-outputs"), "help-remove.txt");
        Assert.assertTrue(buildLog.contains(expected), "Help text mismatched");
    }

    @Test
    public void testRemoveOneService() throws IOException {
        String services = "myorg/service2";
        RemoveSubCommand removeSubCommand = new RemoveSubCommand(printStream, services, false);
        removeSubCommand.execute();
        String buildLog = readOutput(console);
        String expected = getOutput(testResources.resolve("command-outputs"), "remove-one-service.txt");
        Assert.assertTrue(buildLog.contains(expected), "Actual output:" + buildLog);
        // TODO: enable this after fixing the offline resolution of tools
//        balBuildAfter(System.getProperty(USER_DIR));
    }

    @Test
    public void testRemoveTwoServices() throws IOException {
        String services = "myorg/service3,myorg/service4";
        RemoveSubCommand removeSubCommand = new RemoveSubCommand(printStream, services, false);
        removeSubCommand.execute();
        String buildLog = readOutput(console);
        String expected = getOutput(testResources.resolve("command-outputs"), "remove-two-services.txt");
        Assert.assertTrue(buildLog.contains(expected), "Actual output:" + buildLog);
        // TODO: enable this after fixing the offline resolution of tools
//        balBuildAfter(System.getProperty(USER_DIR));
    }

    @Test
    public void testCreateWithInvalidSvcValues() throws IOException {
        String services = "service1,service2";
        RemoveSubCommand removeSubCommand = new RemoveSubCommand(printStream, services, false);
        removeSubCommand.execute();
        String buildLog = readOutput(console);
        String expected = getOutput(testResources.resolve("command-outputs"), "invalid-svc.txt");
        Assert.assertTrue(buildLog.contains(expected));
    }

    @AfterClass
    public void tearDown() {
        System.setProperty(USER_DIR, userDir);
    }
}
