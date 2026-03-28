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

import com.epam.digital.data.platform.registry.regulation.validation.cli.exception.FileProcessingException;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.ExtIntegrationInboundConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn.util.BpmnUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class InboundIntegrationToBpmnProcessExistenceValidator extends AbstractBpToBpmnProcessExistenceValidator {
  private final ObjectMapper yamlObjectMapper;

  public InboundIntegrationToBpmnProcessExistenceValidator(ObjectMapper yamlObjectMapper) {
    this.yamlObjectMapper = yamlObjectMapper;
  }

  @Override
  public Set<ValidationError> validate(RegulationFiles regulationFiles, ValidationContext context) {
    Set<String> bpmnFilesProcessDefinitionsIds;
    try {
      bpmnFilesProcessDefinitionsIds = BpmnUtil.getBpmnFilesProcessDefinitionsId(regulationFiles);
    } catch (FileProcessingException e) {
      log.trace("Failed to parse BPMN files for process definitions", e);
      return Set.of(ValidationError.of(context.getRegulationFileType(), e.getFile(), String.format("Exception during parsing file %s", e.getFile().getName())));
    }

    return validateBpFiles(regulationFiles.getExtIntegrationsInboundFiles(), bpmnFilesProcessDefinitionsIds, context);
  }

  @Override
  protected Set<String> getDefinitionsIdsFromBpFile(File bpFile) throws IOException {
    return  yamlObjectMapper.readValue(bpFile, ExtIntegrationInboundConfiguration.class)
            .getInbound()
            .getProcessDefinitions()
            .stream()
            .map(ExtIntegrationInboundConfiguration.ProcessDefinition::getProcessDefinitionId)
            .collect(Collectors.toSet());
  }
}