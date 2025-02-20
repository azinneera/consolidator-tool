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

import io.ballerina.projects.buildtools.CodeGeneratorTool;
import io.ballerina.projects.buildtools.ToolConfig;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

@ToolConfig(name = Util.TOOL_NAME)
public class CodeGenerator implements CodeGeneratorTool {


    @Override
    public void execute(ToolContext toolContext) {
        toolContext.println("Running  build tool: " + toolContext.toolId());
        ArrayList<String> services = (ArrayList<String>) toolContext.options().get("services").value();
        StringBuilder stringBuilder = new StringBuilder();
        for (String service : services) {
            stringBuilder.append("import ").append(service).append(" as _;\n");
        }
        try {
            Files.createDirectories(toolContext.outputPath());
            Files.writeString(toolContext.outputPath().resolve("consolidate.bal"), stringBuilder);

            String consolidated_mainBal = """
                import ballerina/log;
                
                public function main() {
                    log:printInfo("Started all the services");
                }
                """;
            Files.writeString(toolContext.outputPath().resolve("consolidate_main.bal"), consolidated_mainBal);
        } catch (IOException e) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                "BTCE001", "Error occurred while generating code", DiagnosticSeverity.ERROR);
            toolContext.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, new NullLocation()));
        }
    }

    private static class NullLocation implements Location {
        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from("openAPI sample build tool", from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }
}