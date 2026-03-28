/*
 * Copyright 2024 EPAM Systems.
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

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

class FormCardRoleExistenceValidatorTest {

  private FormCardRoleExistenceValidator formCardRoleExistenceValidator;

  @BeforeEach
  public void setUp() {
    List<String> defaultRoles = List.of("officer", "citizen");
    this.formCardRoleExistenceValidator =
        new FormCardRoleExistenceValidator(new YAMLMapper(), new ObjectMapper(), defaultRoles);
  }

  @Test
  void shouldPassValidationIfFormIsNotCard() {
    var regulationFiles =
        RegulationFiles.builder()
            .formFiles(
                Collections.singleton(
                    getFileFromClasspath("registry-regulation/correct/ui-form.json")))
            .rolesFiles(
                Set.of(
                    getFileFromClasspath("registry-regulation/correct/citizen.yml"),
                    getFileFromClasspath("registry-regulation/correct/officer.yml")))
            .build();

    var errors =
        formCardRoleExistenceValidator.validate(
            regulationFiles, ValidationContext.of(RegulationFileType.FORM_CARD_ROLE_EXISTENCE));

    assertThat(errors, is(empty()));
  }

  @Test
  void shouldPassValidationIfNoTypeOnForm() {
    var regulationFiles =
        RegulationFiles.builder()
            .formFiles(
                Collections.singleton(
                    getFileFromClasspath("registry-regulation/correct/ui-form-without-type.json")))
            .rolesFiles(
                Set.of(
                    getFileFromClasspath("registry-regulation/correct/citizen.yml"),
                    getFileFromClasspath("registry-regulation/correct/officer.yml")))
            .build();

    var errors =
        formCardRoleExistenceValidator.validate(
            regulationFiles, ValidationContext.of(RegulationFileType.FORM_CARD_ROLE_EXISTENCE));

    assertThat(errors, is(empty()));
  }

  @Test
  void shouldPassValidationIfCardContainsValidRole() {
    var regulationFiles =
        RegulationFiles.builder()
            .formFiles(
                Collections.singleton(
                    getFileFromClasspath("registry-regulation/correct/ui-form-card.json")))
            .rolesFiles(
                Set.of(
                    getFileFromClasspath("registry-regulation/correct/citizen.yml"),
                    getFileFromClasspath("registry-regulation/correct/officer.yml")))
            .build();

    var errors =
        formCardRoleExistenceValidator.validate(
            regulationFiles, ValidationContext.of(RegulationFileType.FORM_CARD_ROLE_EXISTENCE));

    assertThat(errors, is(empty()));
  }

  @Test
  void shouldFailValidationIfCardContainsUnknownRoles() {
    var regulationFiles =
        RegulationFiles.builder()
            .formFiles(
                Collections.singleton(
                    getFileFromClasspath(
                        "registry-regulation/broken/ui-form-card-with-unknown-role.json")))
            .rolesFiles(
                Set.of(
                    getFileFromClasspath("registry-regulation/correct/citizen.yml"),
                    getFileFromClasspath("registry-regulation/correct/officer.yml")))
            .build();

    var errors =
        formCardRoleExistenceValidator.validate(
            regulationFiles, ValidationContext.of(RegulationFileType.BP_ROLE_EXISTENCE));

    assertThat(errors, hasSize(2));
  }

  private File getFileFromClasspath(String filePath) {
    var classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(filePath).getFile());
  }
}
