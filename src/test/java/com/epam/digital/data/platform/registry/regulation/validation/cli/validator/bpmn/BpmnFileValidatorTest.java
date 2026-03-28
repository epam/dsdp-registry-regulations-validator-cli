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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import java.io.File;
import java.util.Set;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class BpmnFileValidatorTest {

  private BpmnFileValidator validator;

  @BeforeEach
  public void setUp() {
    this.validator = new BpmnFileValidator();
  }

  @Test
  public void shouldPassProcessValidation() {
    var processFile = getFileFromClasspath("registry-regulation/correct/process.bpmn");

    var errors = this.validator.validate(processFile, ValidationContext.of(RegulationFileType.BPMN));

    assertThat(errors, is(empty()));
  }

  @Test
  public void shouldFailProcessParsingDueToMissingProcessId() {
    var brokenProcessFile = getFileFromClasspath("registry-regulation/broken/process-broken.bpmn");

    var errors = this.validator.validate(brokenProcessFile, ValidationContext.of(RegulationFileType.BPMN));

    assertThat(errors, is(not(empty())));
  }

  @Test
  public void shouldFailProcessParsingDueToDuplicatedProcessId() {
    var brokenProcessFile = getFileFromClasspath(
        "registry-regulation/broken/processes-duplicated-id.bpmn");

    var errors = this.validator.validate(brokenProcessFile, ValidationContext.of(RegulationFileType.BPMN));

    assertThat(errors, is(not(empty())));
    assertThat(errors.iterator().next().getErrorMessage()
        .contains("There are multiple occurrences of ID value 'Process_1"), is(true));
  }

  @Test
  public void shouldFailDueToMissingProcessName() {
    var brokenProcessFile = getFileFromClasspath("registry-regulation/broken/process-empty-name.bpmn");

    var errors = this.validator.validate(brokenProcessFile, ValidationContext.of(RegulationFileType.BPMN));

    assertThat(errors, is(not(empty())));
  }

  @Test
  public void shouldFailDueToProcessModelValidationIssue() {
    var processFile = getFileFromClasspath("registry-regulation/correct/process.bpmn");
    var bpmnModel = Bpmn.readModelFromFile(processFile);

    try (MockedStatic<Bpmn> bpmn = Mockito.mockStatic(Bpmn.class)) {
      bpmn.when(() -> Bpmn.readModelFromFile(processFile))
          .thenReturn(bpmnModel);
      bpmn.when(() -> Bpmn.validateModel(bpmnModel))
          .thenThrow(new ModelValidationException());

      var errors = this.validator.validate(processFile, ValidationContext.of(RegulationFileType.BPMN));

      assertThat(errors, is(not(empty())));
    }
  }

  @Test
  public void shouldFailDueToUnsupportedFileExtension() {
    File missingFile = new File("missing-file.bpmn");

    var errors = this.validator.validate(missingFile, ValidationContext.of(RegulationFileType.BPMN));

    assertThat(errors, is(not(empty())));
  }

  @Test
  public void shouldFailDueToMissingFile() {
    File unsupportedFile = new File("unsupported-file.xml");

    var errors = this.validator.validate(unsupportedFile, ValidationContext.of(RegulationFileType.BPMN));

    assertThat(errors, is(not(empty())));
  }

  @Test
  void shouldFailDueToMissingCompleterTaskId() {
    var brokenProcessFile = getFileFromClasspath("registry-regulation/broken/juel-functions/completer.bpmn");

    var errors = this.validator.validate(brokenProcessFile, ValidationContext.of(RegulationFileType.BPMN));

    assertThat(errors, is(not(empty())));
    assertThat(errors.iterator().next().getErrorMessage(), is("Element id 'addLabFormActivity1' not found for completer() juel function"));
  }

  @Test
  void shouldFailDueToMissingSignSubmissionTaskId() {
    var brokenProcessFile = getFileFromClasspath("registry-regulation/broken/juel-functions/sign_submission.bpmn");

    var errors = this.validator.validate(brokenProcessFile, ValidationContext.of(RegulationFileType.BPMN));

    assertThat(errors, is(not(empty())));
    assertThat(errors.iterator().next().getErrorMessage(), is("Element id 'signLabFormActivity1' not found for sign_submission() juel function"));
  }

  @Test
  void shouldFailDueToMissingSubmissionTaskId() {
    var brokenProcessFile = getFileFromClasspath("registry-regulation/broken/juel-functions/submission.bpmn");

    var errors = this.validator.validate(brokenProcessFile, ValidationContext.of(RegulationFileType.BPMN));

    assertThat(errors, is(not(empty())));
    assertThat(errors.iterator().next().getErrorMessage(), is("Element id 'addLabFormActivity1' not found for submission() juel function"));
  }

  @Test
  void shouldPassValidationWhenDynamicValueInJuelFunction() {
    var brokenProcessFile = getFileFromClasspath("registry-regulation/broken/juel-functions/skip-validation.bpmn");

    var errors = this.validator.validate(brokenProcessFile, ValidationContext.of(RegulationFileType.BPMN));

    assertThat(errors, is(empty()));
  }

  @Test
  public void shouldFailDueToWrongProcessName() {
    var processFile = getFileFromClasspath("registry-regulation/broken/process-wrong-name.bpmn");
    Set<ValidationError> errors =
        this.validator.validate(processFile, ValidationContext.of(RegulationFileType.BPMN));
    assertThat(errors, is(not(empty())));
  }

  @Test
  public void shouldSPassProcessName() {
    var processFile = getFileFromClasspath("registry-regulation/correct/process-correct-name.bpmn");
    Set<ValidationError> errors =
        this.validator.validate(processFile, ValidationContext.of(RegulationFileType.BPMN));
    assertThat(errors, is(empty()));
  }

  @Test
  public void shouldFailDueToShortProcessId() {
    var processFile = getFileFromClasspath("registry-regulation/broken/process-short-id.bpmn");
    Set<ValidationError> errors =
        this.validator.validate(processFile, ValidationContext.of(RegulationFileType.BPMN));
    assertThat(errors, is(not(empty())));
  }
  @Test
  public void shouldFailDueToInvalidProcessId() {
    var processFile = getFileFromClasspath("registry-regulation/broken/process-invalid-id.bpmn");
    Set<ValidationError> errors =
        this.validator.validate(processFile, ValidationContext.of(RegulationFileType.BPMN));
    assertThat(errors, is(not(empty())));
  }

  @Test
  public void shouldSPassProcessId() {
    var processFile = getFileFromClasspath("registry-regulation/correct/process-correct-id.bpmn");
    Set<ValidationError> errors =
        this.validator.validate(processFile, ValidationContext.of(RegulationFileType.BPMN));
    assertThat(errors, is(empty()));
  }

  @Test
  void shouldFailOnNotValidConditionExpression() {
    var brokenProcessFile = getFileFromClasspath("registry-regulation/broken/condition-expression-not-correct.bpmn");

    var errors = this.validator.validate(brokenProcessFile, ValidationContext.of(RegulationFileType.BPMN));

    assertThat(errors, is(not(empty())));
    assertThat(errors.iterator().next().getErrorMessage(), is("Condition expression '${response !=== null}' is not valid"));
  }

  private File getFileFromClasspath(String filePath) {
    var classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(filePath).getFile());
  }
}