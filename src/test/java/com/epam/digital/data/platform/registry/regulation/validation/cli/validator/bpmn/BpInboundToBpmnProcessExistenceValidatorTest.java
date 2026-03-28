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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn;


import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

class BpInboundToBpmnProcessExistenceValidatorTest {

  private InboundIntegrationToBpmnProcessExistenceValidator validator;

  @BeforeEach
  public void setUp() {
    this.validator = new InboundIntegrationToBpmnProcessExistenceValidator(new YAMLMapper());
  }

  @Test
  void shouldPassInboundIntegrationToBpmnProcessExistenceValidation() {
    var regulationFiles = RegulationFiles.builder()
            .extIntegrationsInboundFiles(Collections.singleton(getFileFromClasspath("registry-regulation/correct/inbound-integration.yml")))
            .bpmnFiles(Collections.singleton(getFileFromClasspath("registry-regulation/correct/inbound-process.bpmn")))
            .build();

    var errors = validator.validate(regulationFiles, ValidationContext.of(RegulationFileType.INBOUND_INTEGRATION_TO_BPMN));

    assertThat(errors, is(empty()));
  }

  @Test
  void shouldFailInboundIntegrationToBpmnValidationDueToNonExistentId() {
    var regulationFiles = RegulationFiles.builder()
            .extIntegrationsInboundFiles(Collections.singleton(getFileFromClasspath("registry-regulation/broken/inbound-integration-process-id-broken.yml")))
            .bpmnFiles(Collections.singleton(getFileFromClasspath("registry-regulation/correct/inbound-process.bpmn")))
            .build();

    var errors = validator.validate(regulationFiles, ValidationContext.of(RegulationFileType.INBOUND_INTEGRATION_TO_BPMN));

    assertThat(errors, is(not(empty())));
  }

  private File getFileFromClasspath(String filePath) {
    var classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(filePath).getFile());
  }
}