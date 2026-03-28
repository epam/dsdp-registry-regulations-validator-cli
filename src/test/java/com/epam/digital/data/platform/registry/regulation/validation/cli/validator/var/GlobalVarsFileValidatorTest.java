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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.var;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

class GlobalVarsFileValidatorTest {
  private static final String GLOBAL_VARS_JSON_SCHEMA = "classpath:schema/global-vars-schema.json";
  private final ResourceLoader resourceLoader = new ClassRelativeResourceLoader(getClass());

  @TempDir
  private Path tempDir;

  private RegulationValidator<File> validator;

  @BeforeEach
  public void setUp() {
    this.validator = new GlobalVarsFileValidator(GLOBAL_VARS_JSON_SCHEMA, resourceLoader, new YAMLMapper());
  }

  @Test
  @SneakyThrows
  void shouldPassGlobalVarsValidation() {
    var processFile = tempDir.resolve("valid-global-vars.yml");
    Files.write(processFile, List.of("supportEmail: support_test_ddm@epam.com"));

    var errors = validator.validate(processFile.toFile(), ValidationContext.of(RegulationFileType.GLOBAL_VARS));

    assertThat(errors, is(empty()));
  }

  @Test
  @SneakyThrows
  void shouldPassGlobalVarsValidationWithMissedNotRequiredEmail() {
    var processFile = tempDir.resolve("missed-email-global-vars.yml");
    List<String> globalVars = Collections.singletonList("theme: OSDefaultLight");
    Files.write(processFile, globalVars);

    var errors = validator.validate(processFile.toFile(), ValidationContext.of(RegulationFileType.GLOBAL_VARS));

    assertThat(errors, is(empty()));
  }

  @SneakyThrows
  @ParameterizedTest
  @ValueSource(strings = {"support_test_ddm@mail.ru", "support_test_ddm@yandex.ru",
          ".support_test_ddm@epam.com", "support_test_ddm@epam.com.", "wrongEmail", StringUtils.EMPTY})
  void shouldFailGlobalVarsValidationDueToForbiddenEmail(String supportEmail) {
    var processFile = tempDir.resolve("invalid-email-global-vars.yml");
    Files.write(processFile, List.of("supportEmail: " + supportEmail));

    var errors = validator.validate(processFile.toFile(), ValidationContext.of(RegulationFileType.GLOBAL_VARS));

    assertThat(errors, is(not(empty())));
  }
}