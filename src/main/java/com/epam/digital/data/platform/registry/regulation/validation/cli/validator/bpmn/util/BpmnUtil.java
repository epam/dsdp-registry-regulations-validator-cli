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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn.util;

import com.epam.digital.data.platform.registry.regulation.validation.cli.exception.FileProcessingException;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.BpRoleConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.ExtIntegrationOutboundConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.Process;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import org.camunda.bpm.model.xml.ModelParseException;

import static com.epam.digital.data.platform.registry.regulation.validation.cli.model.ExtIntegrationOutboundConfiguration.ExternalSystem;

public final class BpmnUtil {

  private static final ObjectMapper yamlMapper = new YAMLMapper();

  private BpmnUtil() {
  }

  public static Set<String> getBpmnFilesProcessDefinitionsId(RegulationFiles regulationFiles) {
    Set<String> bpmnProcessDefinitionsId = new HashSet<>();
    for (File file : regulationFiles.getBpmnFiles()) {
      try {
        if (file.exists()) {
          Collection<Process> modelElementsByType = Bpmn.readModelFromFile(file).getModelElementsByType(Process.class);
          bpmnProcessDefinitionsId.addAll(modelElementsByType.stream().map(BaseElement::getId).collect(Collectors.toSet()));
        }
      } catch (ModelParseException e) {
        throw new FileProcessingException(String.format("Invalid process id at %s", file.getName()), file, e);
      }
    }
    return bpmnProcessDefinitionsId;
  }

  public static Set<String> getRoles(RegulationFiles regulationFiles) {
    return regulationFiles.getRolesFiles().stream()
        .filter(File::exists)
        .map(file -> readConfiguration(file, BpRoleConfiguration.class))
        .map(BpRoleConfiguration::getRoles)
        .flatMap(List::stream)
        .map(BpRoleConfiguration.Role::getName)
        .collect(Collectors.toSet());
  }

  public static Map<String, ExternalSystem> getOutboundIntegrationExternalSystems(RegulationFiles regulationFiles) {
    return regulationFiles.getExtIntegrationsOutboundFiles().stream()
        .filter(File::exists)
        .map(file -> readConfiguration(file, ExtIntegrationOutboundConfiguration.class))
        .map(ExtIntegrationOutboundConfiguration::getExternalSystems)
        .flatMap(m -> m.entrySet().stream())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static <T> T readConfiguration(File file, Class<T> clazz) {
    try {
      return yamlMapper.readValue(file, clazz);
    } catch (IOException e) {
      throw new FileProcessingException(String.format("A failure occurred while processing file %s", file.getName()), file, e);
    }
  }
}
