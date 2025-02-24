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

import org.testng.annotations.BeforeSuite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import static io.ballerina.cli.utils.OsUtils.isWindows;

public class TestUtil {

    static Path testResources;
    static ByteArrayOutputStream console;
    static PrintStream printStream;
    static String balDist = "../build/target/extracted-distributions/" +
            "jballerina-tools-zip/jballerina-tools-2201.11.0";

    @BeforeSuite
    public void beforeSuite() throws IOException {
        System.setProperty("java.command", "java");
        System.setProperty("ballerina.home", Paths.get(balDist).toAbsolutePath().toString());
        testResources = Paths.get("src/test/resources/");
        copyTestResources(Paths.get("build/test-consolidate"));
        console = new ByteArrayOutputStream();
        printStream = new PrintStream(console);
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

    static String readOutput() throws IOException {
        String output;
        output = console.toString();
        console.close();
        console = new ByteArrayOutputStream();
        printStream = new PrintStream(console);

        PrintStream out = System.out;
        out.println(output);

        return output;
    }
}
