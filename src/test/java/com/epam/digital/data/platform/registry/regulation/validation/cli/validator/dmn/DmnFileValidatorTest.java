/*
 * Copyright 2021 EPAM Systems.
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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.dmn;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import java.io.File;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class DmnFileValidatorTest {

  private DmnFileValidator validator;

  @BeforeEach
  public void setUp() {
    this.validator = new DmnFileValidator();
  }

  @Test
  public void shouldPassRuleValidation() {
    var ruleFile = getFileFromClasspath("registry-regulation/correct/rule.dmn");

    var errors = this.validator.validate(ruleFile, ValidationContext.of(RegulationFileType.DMN));

    assertThat(errors, is(empty()));
  }

  @Test
  public void shouldFailDueToRuleParsingIssue() {
    var ruleFile = getFileFromClasspath("registry-regulation/broken/rule-broken.dmn");

    var errors = this.validator.validate(ruleFile, ValidationContext.of(RegulationFileType.DMN));

    assertThat(errors, is(not(empty())));
  }

  @Test
  public void shouldFailDueToRuleModelValidationIssue() {
    var ruleFile = getFileFromClasspath("registry-regulation/correct/rule.dmn");
    var dmnModel = Dmn.readModelFromFile(ruleFile);

    try (MockedStatic<Dmn> bpmn = Mockito.mockStatic(Dmn.class)) {
      bpmn.when(() -> Dmn.readModelFromFile(ruleFile))
          .thenReturn(dmnModel);
      bpmn.when(() -> Dmn.validateModel(dmnModel))
          .thenThrow(new ModelValidationException());

      var errors = this.validator.validate(ruleFile, ValidationContext.of(RegulationFileType.DMN));

      assertThat(errors, is(not(empty())));
    }
  }

  private File getFileFromClasspath(String filePath) {
    var classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(filePath).getFile());
  }
}