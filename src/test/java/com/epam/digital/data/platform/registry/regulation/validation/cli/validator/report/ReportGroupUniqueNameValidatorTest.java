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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.report;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.epam.digital.data.platform.registry.regulation.validation.cli.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.fasterxml.jackson.databind.json.JsonMapper;

class ReportGroupUniqueNameValidatorTest {

  private ReportGroupUniqueNameValidator validator;

  @BeforeEach
  public void setUp() {
    this.validator = new ReportGroupUniqueNameValidator(new JsonMapper());
  }

  @Test
  void shouldPassReportsGroupUniqueNameValidation() {
    var groupFiles =
        getFilesFromClasspath(
            "registry-regulation/correct/reports/citizen/uk/laboratory.json,"
                + "registry-regulation/correct/reports/citizen/uk/queries/queries.json,"
                + "registry-regulation/correct/reports/citizen/uk/registration.json,"
                + "registry-regulation/correct/reports/officer/uk/laboratory.json,"
                + "registry-regulation/correct/reports/officer/uk/queries/queries.json,"
                + "registry-regulation/correct/reports/officer/uk/registration.json");

    var errors = validator.validate(groupFiles, ValidationContext.of(RegulationFileType.REPORTS));

    assertThat(errors, is(empty()));
  }

  @Test
  void shouldFailReportsGroupUniqueNameValidation() {
    var groupFiles =
        getFilesFromClasspath(
            "registry-regulation/broken/reports/officer/uk/laboratory.json,"
                + "registry-regulation/broken/reports/officer/uk/registration.json");

    var errors = validator.validate(groupFiles, ValidationContext.of(RegulationFileType.REPORTS));

    assertThat(errors, is(not(empty())));
    assertThat(
        errors.iterator().next().getErrorMessage(),
        is(
            "Duplicated dashboard names found for role officer and language uk: Атестовані лабораторії"));
  }

  @Test
  void shouldFailReportsQueriesGroupUniqueNameValidation() {
    var groupFiles =
        getFilesFromClasspath(
            "registry-regulation/broken/reports/officer/uk/queries/queries.json,"
                + "registry-regulation/broken/reports/officer/uk/queries/queries_2.json");

    var errors = validator.validate(groupFiles, ValidationContext.of(RegulationFileType.REPORTS));

    assertThat(errors.size(), is(2));
    assertThat(
        errors.iterator().next().getErrorMessage(),
        startsWith("Duplicated query names found for role officer and language uk: "));
  }

  private Collection<File> getFilesFromClasspath(String paths) {
    return Stream.of(paths.split(","))
        .map(TestUtils::getFile)
        .collect(Collectors.toList());
  }
}
