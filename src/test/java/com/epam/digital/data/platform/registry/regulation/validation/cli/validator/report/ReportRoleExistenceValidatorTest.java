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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.report;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReportRoleExistenceValidatorTest {

  private ReportRoleExistenceValidator reportRoleExistenceValidator;

  @BeforeEach
  public void setUp() {
    List<String> defaultRoles = List.of("officer", "citizen");
    String officerPermissionsFileName = "officer.yml";
    this.reportRoleExistenceValidator =
        new ReportRoleExistenceValidator(new YAMLMapper(), officerPermissionsFileName,
            defaultRoles);
  }

  @Test
  void shouldPassReportRoleExistenceValidator() {
    var regulationFiles = RegulationFiles.builder()
        .reportsFolders(
            Collections.singleton(getFileFromClasspath("registry-regulation/correct/reports")))
        .rolesFiles(Set.of(getFileFromClasspath("registry-regulation/correct/officer.yml")))
        .build();
    var errors = reportRoleExistenceValidator.validate(regulationFiles,
        ValidationContext.of(RegulationFileType.REPORT_FOLDER));
    assertThat(errors, is(empty()));
  }

  @Test
  void shouldFailReportRoleExistenceValidationDueToNonExistentRole() {
    var regulationFiles = RegulationFiles.builder()
        .reportsFolders(
            Collections.singleton(getFileFromClasspath("registry-regulation/broken/reports")))
        .rolesFiles(Set.of(getFileFromClasspath("registry-regulation/correct/officer.yml")))
        .build();

    var errors = reportRoleExistenceValidator.validate(regulationFiles,
        ValidationContext.of(RegulationFileType.REPORT_FOLDER));

    assertThat(errors, is(not(empty())));
  }

  private File getFileFromClasspath(String filePath) {
    var classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(filePath).getFile());
  }

}