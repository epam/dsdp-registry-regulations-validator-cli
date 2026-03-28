/*
 * Copyright 2022 EPAM Systems.
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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator;

import com.epam.digital.data.platform.registry.regulation.validation.cli.DuplicateDto;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.json.JsonSchemaFileValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FormsFileValidator implements RegulationValidator<File> {

  private JsonSchemaFileValidator jsonSchemaFileValidator;
  private final ObjectMapper fileObjectMapper;

  public FormsFileValidator(String jsonSchemaLocation, ResourceLoader resourceLoader, ObjectMapper fileObjectMapper) {
    jsonSchemaFileValidator = new JsonSchemaFileValidator(jsonSchemaLocation, resourceLoader, fileObjectMapper);
    this.fileObjectMapper = fileObjectMapper;
  }

  @Override
  public Set<ValidationError> validate(File regulation, ValidationContext context) {
    Set<ValidationError> validationResult = new HashSet<>();
    validationResult.addAll(jsonSchemaFileValidator.validate(regulation, context));
    validationResult.addAll(validateDuplications(regulation, context));
    return validationResult;
  }

  private Set<ValidationError> validateDuplications(File regulationFile, ValidationContext validationContext) {
    try {
      DuplicateDto duplicateDto = fileObjectMapper.readValue(regulationFile, DuplicateDto.class);
      return duplicateDto.getMessages()
              .stream().map(one -> ValidationError.of(validationContext.getRegulationFileType(), regulationFile, one))
              .collect(Collectors.toSet());
    } catch (IOException ex) {
      return Collections.singleton(
              ValidationError.of(validationContext.getRegulationFileType(), regulationFile, "File processing failure", ex)
      );
    }
  }

}
