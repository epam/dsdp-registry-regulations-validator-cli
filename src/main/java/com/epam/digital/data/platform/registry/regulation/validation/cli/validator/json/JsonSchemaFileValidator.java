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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.json;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.SneakyThrows;
import org.springframework.core.io.ResourceLoader;

public class JsonSchemaFileValidator implements RegulationValidator<File> {

  private static final VersionFlag JSON_SCHEMA_VERSION = VersionFlag.V4;

  private final JsonSchema schema;
  private final ObjectMapper fileObjectMapper;

  public JsonSchemaFileValidator(String jsonSchemaLocation, ResourceLoader resourceLoader, ObjectMapper fileObjectMapper) {
    this.schema = loadSchema(jsonSchemaLocation, resourceLoader);
    this.fileObjectMapper = fileObjectMapper;
  }

  @Override
  public Set<ValidationError> validate(File regulationFile, ValidationContext validationContext) {
    try {
      var jsonNode = fileObjectMapper.readTree(regulationFile);
      return validateSchema(regulationFile, jsonNode, validationContext.getRegulationFileType());
    } catch (IOException ex) {
      return Collections.singleton(
          ValidationError.of(validationContext.getRegulationFileType(), regulationFile, "File processing failure", ex)
      );
    }
  }

  protected Set<ValidationError> validateSchema(File regulationFile, JsonNode jsonNode, RegulationFileType regulationFileType) {
    var validationMessages = schema.validate(jsonNode);
    var errors = new LinkedHashSet<ValidationError>();
    validationMessages.forEach(
        validationMessage -> errors.add(ValidationError.of(regulationFileType, regulationFile, validationMessage.getMessage()))
    );
    return errors;
  }

  @SneakyThrows
  private JsonSchema loadSchema(String jsonSchemaLocation, ResourceLoader resourceLoader) {
    var resource = resourceLoader.getResource(jsonSchemaLocation);
    var factory = JsonSchemaFactory
        .builder(JsonSchemaFactory.getInstance(JSON_SCHEMA_VERSION))
        .objectMapper(new JsonMapper())
        .build();
    return factory.getSchema(resource.getInputStream());
  }
}
