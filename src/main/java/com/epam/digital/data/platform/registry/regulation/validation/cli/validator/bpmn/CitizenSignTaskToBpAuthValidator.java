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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.BpAuthConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.BpAuthConfiguration.ProcessDefinition;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.xml.ModelParseException;

@Slf4j
public class CitizenSignTaskToBpAuthValidator implements RegulationValidator<RegulationFiles> {

  private final ObjectMapper yamlObjectMapper;
  private final List<String> defaultRoles;
  private final Set<String> citizenRoles = Set.of("individual", "legal", "entrepreneur", "citizen");

  public CitizenSignTaskToBpAuthValidator(ObjectMapper yamlObjectMapper,
      List<String> defaultRoles) {
    this.yamlObjectMapper = yamlObjectMapper;
    this.defaultRoles = List.copyOf(Objects.requireNonNullElse(defaultRoles, List.of()));
  }

  @Override
  public Set<ValidationError> validate(RegulationFiles regulation, ValidationContext context) {
    Map<String, Set<String>> processDefinitionRoles = new HashMap<>();
    Set<ValidationError> errors = Sets.newHashSet();
    for (File file : regulation.getBpAuthFiles()) {
      try {
        processDefinitionRoles.putAll(getBpAuthFilesConfiguration(file));
      } catch (IOException e) {
        log.trace("Failed to read BP auth file {}: {}", file.getName(), e.getMessage(), e);
        errors.add(
            ValidationError.of(context.getRegulationFileType(), file,
                String.format("Exception during reading file %s", e.getMessage())));
      }
    }

    for (File bpmn : regulation.getBpmnFiles()) {
      try {
        errors.addAll(
            validateCitizenSignTaskRoles(loadProcessModel(bpmn), bpmn, processDefinitionRoles,
                context)
        );
      } catch (ModelParseException e) {
        log.trace("Failed to parse BPMN file {}: {}", bpmn.getName(), e.getMessage(), e);
        errors.add(
            ValidationError.of(context.getRegulationFileType(), bpmn,
                String.format("Exception during parsing file %s", bpmn.getName())));
      }
    }
    return errors;
  }

  private Map<String, Set<String>> getBpAuthFilesConfiguration(File bpFile) throws IOException {
    var authorization = yamlObjectMapper.readValue(bpFile, BpAuthConfiguration.class)
        .getAuthorization();
    if ("citizen".equals(authorization.getRealm())) {
      return authorization.getProcessDefinitions().stream()
          .collect(Collectors.toMap(ProcessDefinition::getProcessDefinitionId,
              processDefinition -> new HashSet<>(processDefinition.getRoles())));
    }
    return Map.of();
  }

  private Set<ValidationError> validateCitizenSignTaskRoles(BpmnModelInstance bpmnModel,
      File regulationFile, Map<String, Set<String>> processDefinitionRoles,
      ValidationContext context) {
    Set<ValidationError> errors = new HashSet<>();
    var processes = bpmnModel.getModelElementsByType(Process.class);

    processes.stream().filter(process -> processDefinitionRoles.containsKey(process.getId()))
        .forEach(process -> {
          var processRoles = processDefinitionRoles.get(process.getId());
          var isCustomRolePresent = processRoles.retainAll(defaultRoles);
          if (!isCustomRolePresent) {
            processRoles.retainAll(citizenRoles);
            if (processRoles.isEmpty() || processRoles.contains("citizen")) {
              processRoles = Set.of("individual", "legal", "entrepreneur");
            }
            for (var element : getUserTasks(process)) {
              var taskProperties = getSignTaskEnabledRoles(element);
              if (!processRoles.equals(taskProperties)) {
                errors.add(ValidationError.of(context.getRegulationFileType(), regulationFile,
                    String.format(
                        "In task %s of process %s, sign task should be available for %s",
                        element.getId(), regulationFile.getName(), processRoles)));
              }
            }
          }
        });

    return errors;
  }

  private BpmnModelInstance loadProcessModel(File regulationFile) {
    return Bpmn.readModelFromFile(regulationFile);
  }

  private List<UserTask> getUserTasks(Process process) {
    return process.getChildElementsByType(UserTask.class).stream()
        .filter(element -> Objects.equals(
            element.getAttributeValueNs(CAMUNDA_NS, "modelerTemplate"),
            "citizenSignTaskTemplate"))
        .collect(Collectors.toList());
  }

  private Set<String> getSignTaskEnabledRoles(UserTask userTask) {
    return userTask.getExtensionElements().getElementsQuery().filterByType(
            CamundaProperties.class).list().stream()
        .flatMap(camundaProperties -> camundaProperties.getCamundaProperties().stream())
        .filter(
            property -> "true".equals(property.getCamundaValue()) && !"eSign".equals(
                property.getCamundaName()))
        .map(property -> property.getCamundaName().toLowerCase())
        .collect(Collectors.toSet());
  }
}
