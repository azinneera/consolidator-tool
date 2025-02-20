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

import io.ballerina.cli.cmd.CommandUtil;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.toml.semantic.TomlType;
import io.ballerina.toml.semantic.ast.TomlArrayValueNode;
import io.ballerina.toml.semantic.ast.TomlKeyValueNode;
import io.ballerina.toml.semantic.ast.TomlStringValueNode;
import io.ballerina.toml.semantic.ast.TomlTableNode;
import io.ballerina.toml.semantic.ast.TomlValueNode;
import io.ballerina.toml.semantic.ast.TopLevelNode;
import io.ballerina.toml.validator.schema.ArraySchema;
import io.ballerina.toml.validator.schema.Schema;
import io.ballerina.toml.validator.schema.StringSchema;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Util {
    static final String TOOL_NAME = "consolidate";

    static boolean validateServicesInput(String[] services, PrintStream errStream) throws IOException {
        if (services == null || services.length == 0) {
            CommandUtil.printError(errStream, "no services provided to generate the consolidator project",
                    "bal consolidate --services myOrg/svc1,myOrg/svc2", false);
            return false;
        }

        Schema schema = Schema.from(FileUtils.readSchema(TOOL_NAME, Util.class.getClassLoader()));
        ArraySchema properties = (ArraySchema) schema.properties().get("services");
        Optional<String> optionalPattern = ((StringSchema) properties.items()).pattern();
        if (optionalPattern.isEmpty()) {
            throw new IllegalStateException("unable to find the pattern for services in the tool schema");
        }
        boolean isValid = true;
        for (String service : services) {
            if (!service.matches(optionalPattern.get())) {
                String msg = properties.items().message().get("pattern");
                CommandUtil.printError(errStream, "'" + service + "': " + msg, null, false);
                isValid = false;
            }
        }
        return isValid;
    }

     static Set<String> getServices(TomlTableNode tomlTableNode) {
        Set<String> elements = new HashSet<>();
        TopLevelNode servicesNode = tomlTableNode.entries().get("services");
        if (servicesNode.kind() != null && servicesNode.kind() == TomlType.KEY_VALUE) {
            TomlKeyValueNode keyValueNode = (TomlKeyValueNode) servicesNode;
            TomlValueNode valueNode = keyValueNode.value();
            if (valueNode.kind() == TomlType.ARRAY) {
                TomlArrayValueNode arrayValueNode = (TomlArrayValueNode) valueNode;
                for (TomlValueNode value : arrayValueNode.elements()) {
                    if (value.kind() == TomlType.STRING) {
                        elements.add(((TomlStringValueNode) value).getValue());
                    }
                }
            }
        }
        return elements;
    }

     static void replaceServicesArrayInToml(Set<String> allServices, Path balTomlPath)
            throws IOException {
        String content = Files.readString(balTomlPath);
        Pattern pattern = Pattern.compile("options\\.services\\s*=\\s*\\[(?:\\s*\"[^\"]+\"\\s*,?\\s*)+]",
                Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (!matcher.find()) {
            return;
        }
        String existingStr = matcher.group();
        String replacementStr = "options.services = [\"" + String.join("\", \"", allServices) + "\"]";
        String modifiedContent = content.replace(existingStr, replacementStr);
        Files.writeString(balTomlPath, modifiedContent, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static String getHelpText() {
        try (InputStream inputStream = Util.class.getClassLoader().getResourceAsStream("ballerina-consolidate.help");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            return "help text not found";
        }
    }

    public static void validatePackageName(String packageName, PrintStream outStream) {
        if (!ProjectUtils.validatePackageName(packageName)) {
            packageName = ProjectUtils.guessPkgName(packageName, "default");
            outStream.println("Package name is derived as '" + packageName
                    + "'. Edit the Ballerina.toml to change it.");
            outStream.println();
        }
    }
}
