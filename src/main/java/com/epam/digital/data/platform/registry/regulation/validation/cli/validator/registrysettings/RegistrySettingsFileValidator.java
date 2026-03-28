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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.registrysettings;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.json.JsonSchemaFileValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.springframework.core.io.ResourceLoader;

public class RegistrySettingsFileValidator extends JsonSchemaFileValidator {

  public static final String TITLE = "title";
  public static final String TITLE_FULL = "titleFull";

  public RegistrySettingsFileValidator(String jsonSchemaLocation,
      ResourceLoader resourceLoader,
      ObjectMapper fileObjectMapper) {
    super(jsonSchemaLocation, resourceLoader, fileObjectMapper);
  }

  @Override
  protected Set<ValidationError> validateSchema(File regulationFile, JsonNode jsonNode,
      RegulationFileType regulationFileType) {
    Set<ValidationError> errors = super.validateSchema(regulationFile, jsonNode,
        regulationFileType);
    errors.addAll(validateTitles(regulationFile, jsonNode, regulationFileType));
    return errors;
  }

  private Set<ValidationError> validateTitles(File regulationFile, JsonNode jsonNode,
      RegulationFileType regulationFileType) {
    var title = jsonNode.findValue(TITLE);
    var titleFull = jsonNode.findValue(TITLE_FULL);

    if (Objects.isNull(titleFull) || Objects.isNull(title)) {
      return Collections.emptySet();
    }

    if (title.isNull() && titleFull.isNull()) {
      return Collections.emptySet();
    } else if (title.isNull() || titleFull.isNull()) {
      return Collections.singleton(ValidationError.of(regulationFileType, regulationFile,
          "One of fields \"" + TITLE + "\" or \"" + TITLE_FULL + "\" cannot be empty"));
    }

    return Collections.emptySet();
  }
}
