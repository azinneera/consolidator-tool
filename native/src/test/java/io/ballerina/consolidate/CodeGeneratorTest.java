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

import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.projects.directory.BuildProject;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CodeGeneratorTest {

    @Test
    public void testValidToolUsage() throws IOException {
        BuildProject project = BuildProject.load(Paths.get("build/test-consolidate"));
        PackageManifest.Tool tool = project.currentPackage().manifest().tools().getFirst();
        ToolContext toolContext = ToolContext.from(tool, project.currentPackage(), System.out);
        CodeGenerator codeGenerator = new CodeGenerator();
        Assert.assertFalse(Files.exists(project.sourceRoot().resolve("generated")));
        codeGenerator.execute(toolContext);

        // Verify the content in generated/consolidator_main.bal file
        Path generatedMainBalPath = project.sourceRoot().resolve("generated/consolidator_main.bal");
        Assert.assertTrue(Files.exists(generatedMainBalPath));
        String consolidatorMainBal = """
                import ballerina/log;
                
                public function main() {
                    log:printInfo("Started all services");
                }
                """;
        Assert.assertEquals(Files.readString(generatedMainBalPath), consolidatorMainBal);

        // Verify the content in generated/consolidator.bal file
        Path generatedImportsBal = project.sourceRoot().resolve("generated/consolidator.bal");
        Assert.assertTrue(Files.exists(generatedImportsBal));
        String consolidatorBal = """
                import myOrg/svc1 as _;
                import myOrg/svc2 as _;
                import myOrg/svc3 as _;
                """;
        Assert.assertEquals(Files.readString(generatedImportsBal), consolidatorBal);
    }
}
