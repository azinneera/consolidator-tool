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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static io.ballerina.consolidate.TestUtil.getOutput;
import static io.ballerina.consolidate.TestUtil.printStream;
import static io.ballerina.consolidate.TestUtil.readOutput;
import static io.ballerina.consolidate.TestUtil.testResources;

public class ConsolidateCommandTest {

    @Test
    public void testHelp() throws IOException {
        ConsolidateCommand consolidateCommand = new ConsolidateCommand(printStream);
        consolidateCommand.execute();
        String buildLog = readOutput();
        String expected = getOutput(testResources.resolve("command-outputs"), "help-main.txt");
        Assert.assertTrue(buildLog.contains(expected));
    }
}
