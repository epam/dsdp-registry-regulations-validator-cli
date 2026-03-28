/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.json.regsettings;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.json.JsonSchemaFileValidator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrySettingsFileValidatorTest {

  private static final String REGISTRY_SETTINGS_JSON_SCHEMA = "classpath:schema/registry-settings-schema.json";
  private final ResourceLoader resourceLoader = new ClassRelativeResourceLoader(getClass());

  private RegulationValidator<File> validator;

  @BeforeEach
  public void setUp() {
    this.validator =
        new JsonSchemaFileValidator(REGISTRY_SETTINGS_JSON_SCHEMA, resourceLoader, new YAMLMapper());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "registry-regulation/correct/registry-settings.yaml",
        "registry-regulation/correct/registry-settings-no-full-title.yaml"
      })
  void shouldPassValidation(String registrySettingsFile) {
    var actualErrors =
        validator.validate(
            getFileFromClasspath(registrySettingsFile),
            ValidationContext.of(RegulationFileType.REGISTRY_SETTINGS));

    assertThat(actualErrors).isEmpty();
  }
  @ParameterizedTest
  @ValueSource(
      strings = {
        "registry-regulation/broken/registry-settings-started-with-special-characters.yaml",
        "registry-regulation/broken/registry-settings-special-characters-in-first-three-symbols.yaml"
      })
  void shouldFailValidation1(String registrySettingsFile) {
    var actualErrors =
        validator.validate(
            getFileFromClasspath(registrySettingsFile),
            ValidationContext.of(RegulationFileType.REGISTRY_SETTINGS));

    assertThat(actualErrors).isNotEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "registry-regulation/broken/registry-settings-long-title.yaml",
          "registry-regulation/broken/registry-settings-forbidden-chars.yaml"
  })
  void shouldFailValidation(String registrySettingsFile) {
    var actualErrors =
            validator.validate(
                    getFileFromClasspath(registrySettingsFile),
                    ValidationContext.of(RegulationFileType.REGISTRY_SETTINGS));

    assertThat(actualErrors).isNotEmpty();
  }

  private File getFileFromClasspath(String filePath) {
    var classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(filePath).getFile());
  }
}
