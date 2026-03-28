/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.role;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RoleUniqueNameValidator implements RegulationValidator<File> {

    private static final String DUPLICATED_ROLE_NAME_ERROR_MSG_FORMAT = "Duplicated role names found: %s";
    public static final String FILE_PROCESSING_ERROR_MSG = "File processing failure";
    private final ObjectMapper yamlObjectMapper;

    public RoleUniqueNameValidator(ObjectMapper yamlObjectMapper) {
        this.yamlObjectMapper = yamlObjectMapper;
    }

    @Override
    public Set<ValidationError> validate(File file, ValidationContext context) {
        Set<ValidationError> errors = new HashSet<>();
        Set<String> roleNames = new HashSet<>();
        try {
            getRoleNames(file).forEach(roleName -> {
                if (!roleNames.add(roleName)) {
                    errors.add(toValidationError(
                            String.format(DUPLICATED_ROLE_NAME_ERROR_MSG_FORMAT, roleName), file,
                            context));
                }
            });
        } catch (IOException e) {
            errors.add(ValidationError.of(context.getRegulationFileType(),
                    file, FILE_PROCESSING_ERROR_MSG, e));
        }
        return errors;
    }

    private List<String> getRoleNames(File file) throws IOException {
        JsonNode jsonNode = yamlObjectMapper.readTree(file);
        return StreamSupport.stream(jsonNode.get("roles").spliterator(), false)
                .map(jn -> jn.get("name")).filter(jn -> jn != null).map(jn -> jn.asText())
                .collect(Collectors.toList());
    }

    private ValidationError toValidationError(String message, File regulationFile, ValidationContext context) {
        return ValidationError.builder()
                .errorMessage(message)
                .regulationFileType(context.getRegulationFileType())
                .regulationFile(regulationFile)
                .build();
    }
}
