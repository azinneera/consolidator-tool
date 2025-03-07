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
import java.util.List;

@ToolConfig(name = Util.TOOL_NAME)
public class CodeGenerator implements CodeGeneratorTool {

    @Override
    public void execute(ToolContext toolContext) {
        toolContext.println("Running  build tool: " + toolContext.toolId());

        if (!toolContext.options().containsKey("services")) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                "BTCE001", "No services provided in 'options' to generate the consolidator package",
                    DiagnosticSeverity.ERROR);
            toolContext.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, new NullLocation()));
            return;
        }

        List<?> services = (ArrayList<?>) toolContext.options().get("services").value();
        StringBuilder stringBuilder = new StringBuilder();
        for (Object serviceObj : services) {
            String service = (String) serviceObj;
            stringBuilder.append("import ").append(service).append(" as _;\n");
        }
        try {
            Files.createDirectories(toolContext.outputPath());
            Files.writeString(toolContext.outputPath().resolve("consolidator.bal"), stringBuilder);

            String consolidatorMainBal = """
                import ballerina/log;
                
                public function main() {
                    log:printInfo("Started all services");
                }
                """;
            Files.writeString(toolContext.outputPath().resolve("consolidator_main.bal"), consolidatorMainBal);
        } catch (IOException e) {
            DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                "BTCE002", "Error occurred while generating code", DiagnosticSeverity.ERROR);
            toolContext.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, new NullLocation()));
        }
    }

    // TODO: Remove the null location once the location is supported in toolContext#options
    private static class NullLocation implements Location {

        @Override
        public LineRange lineRange() {
            LinePosition from = LinePosition.from(0, 0);
            return LineRange.from(Util.BALLERINA_TOML, from, from);
        }

        @Override
        public TextRange textRange() {
            return TextRange.from(0, 0);
        }
    }
}
