/*
 * Copyright 2024 EPAM Systems.
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

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.core.Is.is;

class ReportFolderStructureValidatorTest {

  private ReportFolderStructureValidator validator;

  @BeforeEach
  public void setUp() {
    this.validator = new ReportFolderStructureValidator();
  }

  @Test
  void shouldPassValidationForCorrectFolders() {
    var regulationFiles = RegulationFiles.builder()
            .reportsFolders(
                    List.of(getFileFromClasspath("registry-regulation/correct/reports")))
            .build();
    var context = ValidationContext.of(RegulationFileType.REPORT_FOLDER);

    var actual = validator.validate(regulationFiles, context);

    assertThat(actual, is(empty()));
  }

  @Test
  void shouldFailValidationIfRedundantFilesInRoleFolder() {
    var regulationFiles =
        RegulationFiles.builder()
            .reportsFolders(List.of(getFileFromClasspath("registry-regulation/broken/reports-with-redundant-files")))
            .build();
    var context = ValidationContext.of(RegulationFileType.REPORT_FOLDER);

    var actual = validator.validate(regulationFiles, context);

    assertThat(actual, hasSize(1));
    assertThat(actual.iterator().next().getErrorMessage(), matchesPattern("Invalid structure, folder .* should not contain anything except subfolders"));
  }

  @Test
  void shouldFailValidationIfReportLangIsNonIso() {
    var regulationFiles =
            RegulationFiles.builder()
                    .reportsFolders(List.of(getFileFromClasspath("registry-regulation/broken/reports-with-invalid-lang")))
                    .build();
    var context = ValidationContext.of(RegulationFileType.REPORT_FOLDER);

    var actual = validator.validate(regulationFiles, context);

    assertThat(actual, hasSize(1));
    assertThat(actual.iterator().next().getErrorMessage(), matchesPattern("Language ad is a non ISO-639, rename folder .*"));
  }

  private File getFileFromClasspath(String filePath) {
    var classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(filePath).getFile());
  }
}
