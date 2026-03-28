/*
 * Copyright 2023 EPAM Systems.
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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.form;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FormToSearchConditionExistenceValidatorTest {

  private FormToSearchConditionExistenceValidator validator;

  @BeforeEach
  public void setUp() {
    this.validator = new FormToSearchConditionExistenceValidator(new JsonMapper());
    System.setProperty("liquibase.secureParsing", "false");
    System.setProperty("javax.xml.accessExternalSchema", "all");
  }

  @Test
  void shouldPassFormToSearchConditionExistenceValidation() {

    var regulationFiles = RegulationFiles.builder()
        .formFiles(Collections.singleton(
            getFileFromClasspath("registry-regulation/correct/form/test-form.json")))
        .liquibaseFiles(Collections.singleton(
            getFileFromClasspath("registry-regulation/correct/form/main-liquibase.xml")))
        .build();

    var errors = validator.validate(regulationFiles,
        ValidationContext.of(RegulationFileType.FORM_TO_SC));

    assertThat(errors, is(empty()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testArgumentProvider")
  void shouldFailFormToSearchConditionExistenceValidation(String name, String path) {
    var regulationFiles = RegulationFiles.builder()
        .formFiles(Collections.singleton(
            getFileFromClasspath("registry-regulation/correct/form/test-form.json")))
        .liquibaseFiles(Collections.singleton(
            getFileFromClasspath(path)))
        .build();

    var errors = validator.validate(regulationFiles,
        ValidationContext.of(RegulationFileType.FORM_TO_SC));

    assertThat(errors.size(), is(1));
    assertThat(errors.iterator().next().getErrorMessage(),
        is("Next search condition doesn't exist under data-model files: find_all"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testArgumentProvider")
  void shouldFailFormToSearchConditionExistenceValidationOnInvalidUrl(String name, String path) {
    var regulationFiles = RegulationFiles.builder()
        .formFiles(Collections.singleton(
            getFileFromClasspath("registry-regulation/broken/form/invalid-endpoint-url-form.json")))
        .liquibaseFiles(Collections.singleton(
            getFileFromClasspath(path)))
        .build();

    var errors = validator.validate(regulationFiles,
        ValidationContext.of(RegulationFileType.FORM_TO_SC));

    assertThat(errors.size(), is(1));
    assertThat(errors.iterator().next().getErrorMessage(),
        is("Invalid data factory url: /api/hello-world/find-all"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testArgumentProvider")
  void shouldFailFormToSearchConditionValidationOnInvalidThirdPartyUrl(String name, String path) {
    var regulationFiles = RegulationFiles.builder()
        .formFiles(Collections.singleton(
            getFileFromClasspath("registry-regulation/broken/form/invalid-third-party-url-form.json")))
        .liquibaseFiles(Collections.singleton(
            getFileFromClasspath(path)))
        .build();

    var errors = validator.validate(regulationFiles,
        ValidationContext.of(RegulationFileType.FORM_TO_SC));

    assertThat(errors.size(), is(1));
    assertThat(errors.iterator().next().getErrorMessage(),
        is("Invalid data factory url: https://example.com/ &someParam=paramParam"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testArgumentProvider")
  void shouldPassFormToSearchConditionValidationOnValidThirdPartyUrl(String name, String path) {
    var regulationFiles = RegulationFiles.builder()
        .formFiles(Collections.singleton(
            getFileFromClasspath("registry-regulation/correct/form/valid-third-party-url-form.json")))
        .liquibaseFiles(Collections.singleton(
            getFileFromClasspath(path)))
        .build();

    var errors = validator.validate(regulationFiles,
        ValidationContext.of(RegulationFileType.FORM_TO_SC));

    assertThat(errors.size(), is(0));
  }

  private File getFileFromClasspath(String filePath) {
    var classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(filePath).getFile());
  }

  static Stream<Arguments> testArgumentProvider() {
    return Stream.of(
        arguments("No search condition", "registry-regulation/broken/form/no-search-condition.xml"),
        arguments("Drop search condition", "registry-regulation/broken/form/drop-search-condition.xml"),
        arguments("Include drop search condition", "registry-regulation/broken/form/include-create-search-condition.xml")
    );
  }
}
