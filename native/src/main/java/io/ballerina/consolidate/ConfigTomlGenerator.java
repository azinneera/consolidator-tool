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

import io.ballerina.projects.ConfigReader;
import io.ballerina.projects.buildtools.ToolContext;
import io.ballerina.projects.configurations.ConfigModuleDetails;
import io.ballerina.projects.configurations.ConfigVariable;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigTomlGenerator {
    private static final String DOT = ".";

    public static String generateConfigToml(ToolContext toolContext) {
        toolContext.currentPackage().getCompilation();

        Map<ConfigModuleDetails, List<ConfigVariable>> configurableVars =
                ConfigReader.getConfigVariables(toolContext.currentPackage());
        StringBuilder configTomlBuilder = new StringBuilder();

        for (ConfigModuleDetails configModuleDetails : configurableVars.keySet()) {
            List<String> optionalVars = new ArrayList<>();
            String fQModuleName;
            if (Objects.equals(configModuleDetails.packageName(), configModuleDetails.moduleName())) {
                fQModuleName = configModuleDetails.orgName() + DOT + configModuleDetails.packageName();
            } else {
                fQModuleName = configModuleDetails.orgName()+ DOT + configModuleDetails.packageName() + DOT
                        + configModuleDetails.moduleName();
            }
            configTomlBuilder.append("[").append(fQModuleName).append("]\n");

            for (ConfigVariable configVariable : configurableVars.get(configModuleDetails)) {
                String example = "";
                if (TypeTags.isSimpleBasicType(configVariable.type().tag)) {
                    example = generateTomlEntryForBasicType(configVariable.name(), configVariable.type().tag);
                } else if (configVariable.type().tag == TypeTags.INTERSECTION) {
                    BIntersectionType bIntersectionType = (BIntersectionType) configVariable.type();
                    BType effectiveType = bIntersectionType.effectiveType;
                    if (effectiveType.getKind().equals(TypeKind.TABLE)) {
                        BIntersectionType constraintType = (BIntersectionType) ((BTableType) effectiveType)
                                .getConstraint();
                        BType effectiveType1 = constraintType.effectiveType;
                        example = "[[" + fQModuleName + DOT + configVariable.name() + "]]" +
                                generateTomlEntryForIntersectionType(configVariable.name(), effectiveType1);
                    } else if (effectiveType.getKind().equals(TypeKind.ARRAY)) {
                        BType eType = ((BArrayType) effectiveType).eType;
                        if (TypeTags.INTERSECTION == eType.tag) {
                            BIntersectionType bIntersectionType1 = (BIntersectionType) eType;
                            BType effectiveType1 = bIntersectionType1.effectiveType;
                            example = "[[" + fQModuleName + DOT + configVariable.name() + "]]" +
                                    generateTomlEntryForIntersectionType(configVariable.name(), effectiveType1);
                        }
                    } else {
                        example = "[" + fQModuleName + DOT + configVariable.name() + "]" +
                                generateTomlEntryForIntersectionType(configVariable.name(), effectiveType);
                    }
                }

                if (!configVariable.isRequired()) {
                    optionalVars.add("# " + example + " (optional)");
                    continue;
                }
                configTomlBuilder.append(example);
            }
            for (String optionalVar : optionalVars) {
                configTomlBuilder.append(optionalVar);
            }
            configTomlBuilder.append("\n");
        }
        return configTomlBuilder.toString();
    }

    private static String generateTomlEntryForIntersectionType(String key, BType effectiveType) {
        if (effectiveType.getKind().equals(TypeKind.ARRAY)) {
            return generateTomlEntryForArrayType(key, effectiveType);
        } else if (effectiveType.getKind().equals(TypeKind.MAP)) {
            return generateTomlEntryForMapType(effectiveType);
        } else if (effectiveType.getKind().equals(TypeKind.RECORD)) {
            return generateTomlEntryForRecordType(effectiveType);
        } else {
            return switch (effectiveType.tag) {
                case TypeTags.XML -> key + " = \"<book>The Lost World</book>\" # type: xml\n";
                case TypeTags.JSON -> key + " = {name = \"Jane\"} # type: json\n";
                default -> throw new RuntimeException("unknown type found for configurable: " + key);
            };
        }

    }

    private static String generateTomlEntryForRecordType(BType effectiveType) {
        LinkedHashMap<String, BField> fields = ((BRecordType) effectiveType).fields;
        StringBuilder stringBuilder = new StringBuilder("\n");
        for (Map.Entry<String, BField> stringBFieldEntry : fields.entrySet()) {
            BType type = stringBFieldEntry.getValue().getType();
            if (TypeTags.isSimpleBasicType(type.tag)) {
                stringBuilder.append(generateTomlEntryForBasicType(stringBFieldEntry.getKey(), type.tag));
            }
        }
        return stringBuilder.toString();
    }

    private static String generateTomlEntryForMapType(BType effectiveType) {
        int tag = ((BMapType) effectiveType).getConstraint().tag;
        if (TypeTags.isSimpleBasicType(tag)) {
            return switch (tag) {
                case TypeTags.BOOLEAN -> " # type: boolean\nkey1 = true\nkey2 = true";
                case TypeTags.BYTE -> " # type: byte\nkey1 = 1\nkey2 = 80";
                case TypeTags.INT -> " # type: int\nkey1 = 1\nkey2 = 9090";
                case TypeTags.FLOAT -> " # type: float\n key1 = 100.00\nkey2 = 1900.00";
                case TypeTags.DECIMAL -> " # type: decimal\nkey1 = 25000.00\nkey2 = 30000.00";
                case TypeTags.STRING -> " # type: string\nkey1 = \"value1\"\nkey2 = \"value2\"";
                default -> throw new RuntimeException("unknown type");
            };
        } else {

        }
        return "";
    }

    private static String generateTomlEntryForArrayType(String key, BType effectiveType) {
        return switch (effectiveType.tag) {
            case TypeTags.BOOLEAN -> key + " = [false, false, true] # type : boolean[]\n";
            case TypeTags.BYTE -> key + " = [1, 2] # type: byte[]\n";
            case TypeTags.INT -> key + " = [12, 34] # type: int[]\n";
            case TypeTags.FLOAT -> key + " = [12.3, 45.6, 78.9] # type: float[]\n";
            case TypeTags.DECIMAL -> key + " = [12300.4, 56700.8, 89100.2] # type: decimal[]\n";
            case TypeTags.STRING -> key + " = [\"str1\", \"str2\", \"str3\"]";
            default -> throw new RuntimeException("unknown type");
        };
    }

    private static String generateTomlEntryForBasicType(String key, int tag) {
        return switch (tag) {
            case TypeTags.BOOLEAN -> key + " = true # type: boolean\n";
            case TypeTags.BYTE -> key + " = 10 # type: byte\n";
            case TypeTags.INT -> key + " = 12 # type: int\n";
            case TypeTags.FLOAT -> key + " = 1.23 # type: float\n";
            case TypeTags.DECIMAL -> key + " = 12345.67 # type: decimal\n";
            case TypeTags.STRING -> key + " = \"str1\" # type: string\n";
            default -> throw new RuntimeException("unknown type");
        };
    }
}
