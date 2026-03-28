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

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

class FormsFileValidatorTest {

  private static final String FORMS_JSON_SCHEMA = "classpath:schema/forms-schema.json";
  private final ResourceLoader resourceLoader = new ClassRelativeResourceLoader(getClass());

  @BeforeEach
  public void setUp() {
    this.validator = new FormsFileValidator(FORMS_JSON_SCHEMA, resourceLoader, new JsonMapper());
  }

  private RegulationValidator<File> validator;

  @SneakyThrows
  @Test
  void shouldBeValidationErrorsOnDuplicateName() {
    var processFile = getFileFromClasspath("registry-regulation/broken/ui-form-duplicate-name.json");

    var errors = validator.validate(processFile, ValidationContext.of(RegulationFileType.FORMS));

    assertThat(errors, is(not(empty())));
  }

  @SneakyThrows
  @Test
  void shouldNotHaveErrorsOnForm() {
    var processFile = getFileFromClasspath("registry-regulation/correct/ui-form.json");

    var errors = validator.validate(processFile, ValidationContext.of(RegulationFileType.FORMS));

    assertThat(errors, is(empty()));
  }

  @SneakyThrows
  @Test
  void shouldNotHaveErrorsOnCard() {
    var processFile = getFileFromClasspath("registry-regulation/correct/ui-form-card.json");

    var errors = validator.validate(processFile, ValidationContext.of(RegulationFileType.FORMS));

    assertThat(errors, is(empty()));
  }

  private File getFileFromClasspath(String filePath) {
    var classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(filePath).getFile());
  }
}
