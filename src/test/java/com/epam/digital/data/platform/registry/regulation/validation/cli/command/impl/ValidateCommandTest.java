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

package com.epam.digital.data.platform.registry.regulation.validation.cli.command.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandParameters;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.SystemExit;
import com.epam.digital.data.platform.registry.regulation.validation.cli.util.TestUtils;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationFilesValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidatorFactory;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidateCommandTest {

  @Mock
  private RegulationValidatorFactory validatorFactory;

  @Mock
  private SystemExit systemExit;
  @Mock
  private RegulationFilesValidator validator;
  @InjectMocks
  private ValidateCommand validateCommand;

  @Test
  void shouldPassedValidation() {
    var testFile = TestUtils.getFile("registry-regulation/correct/bp-grouping/bp-grouping.yml");
    var regulationFiles = RegulationFiles.builder()
        .bpGroupingFiles(Collections.singleton(testFile))
        .build();
    var parameters = new CommandParameters();
    parameters.setRegulationFiles(regulationFiles);
    var validationErr = new HashSet<ValidationError>();
    when(validatorFactory.newRegulationFilesValidator()).thenReturn(validator);
    when(validator.validate(eq(regulationFiles), any())).thenReturn(validationErr);

    validateCommand.execute(parameters);

    verify(systemExit).complete();
  }

  @Test
  void shouldFailedValidation() {
    var testFile = TestUtils.getFile("registry-regulation/broken/bp-grouping/bp-grouping-process-definition-id-duplicates.yml");
    var regulationFiles = RegulationFiles.builder()
        .bpGroupingFiles(Collections.singleton(testFile))
        .build();
    var parameters = new CommandParameters();
    parameters.setRegulationFiles(regulationFiles);
    var validationErr = new HashSet<ValidationError>();
    validationErr.add(ValidationError.builder()
        .errorMessage("Duplicated process definition ids found: first-process-group")
        .regulationFile(testFile)
        .regulationFileType(RegulationFileType.BP_GROUPING)
        .build());
    when(validatorFactory.newRegulationFilesValidator()).thenReturn(validator);
    when(validator.validate(eq(regulationFiles), any())).thenReturn(validationErr);

    validateCommand.execute(parameters);

    verify(systemExit).validationFailure();
  }

  @Test
  void shouldSuccessfullyCompleteWhenNoFilesToValidate() {
    var parameters = new CommandParameters();
    parameters.setRegulationFiles(RegulationFiles.builder().build());

    validateCommand.execute(parameters);

    verify(systemExit).complete();
  }
}
