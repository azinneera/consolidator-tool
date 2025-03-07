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
import org.testng.annotations.BeforeSuite;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import static io.ballerina.cli.utils.OsUtils.isWindows;

public class TestUtil {

    static Path testResources;
    static final String BALLERINA_HOME = "ballerina.home";
    static final String USER_DIR = "user.dir";
    private static ByteArrayOutputStream console;
    private static PrintStream printStream;

    @BeforeSuite
    public void beforeSuite() throws IOException {
        System.setProperty("java.command", "java");
        testResources = Paths.get("src/test/resources/");
        copyTestResources(Paths.get("build/test-consolidate"));
    }

    private void copyTestResources(Path target) throws IOException {
        Files.createDirectories(target);
        Path source = testResources.resolve("packages/test-consolidate");
        try (Stream<Path> paths = Files.walk(source)) {
            paths.forEach(src -> {
                try {
                    Path destination = target.resolve(source.relativize(src));
                    Files.copy(src, destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException("Error copying file: " + src, e);
                }
            });
        }
    }

    protected static String getOutput(Path outputPath, String fileName) throws IOException {
        if (isWindows()) {
            return Files.readString(outputPath.resolve("windows").resolve(fileName))
                    .replace("\r", "");
        } else {
            return Files.readString(outputPath.resolve("unix").resolve(fileName));
        }
    }

    static String readOutput(ByteArrayOutputStream console) throws IOException {
        String output = console.toString();
        console.close();
        PrintStream out = System.out;
        out.println(output);
        return output;
    }

     static void balBuildAfter(String projectPath, PrintStream printStream) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                System.getProperty(BALLERINA_HOME) + "/bin/bal", "build", projectPath);
         processBuilder.redirectErrorStream(true);
         Process process = processBuilder.start();
         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
         String line;
         while ((line = reader.readLine()) != null) {
             printStream.println(line);
         }
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            Assert.fail("bal build failed with exit code: " + exitCode);
        }

    }
}
