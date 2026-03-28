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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpgrouping;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.BpGroupingConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn.util.BpmnUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.Process;

public class BpGroupingProcessDefinitionIdValidator implements
    RegulationValidator<RegulationFiles> {

  private static final String DUPLICATES_ERROR_MSG_FORMAT = "Duplicated process definition ids found: %s";
  private static final String NOT_EXIST_ERROR_MSG_FORMAT = "Processes with definition ids not exist: %s";

  private final ObjectMapper yamlObjectMapper;

  public BpGroupingProcessDefinitionIdValidator(ObjectMapper yamlObjectMapper) {
    this.yamlObjectMapper = yamlObjectMapper;
  }

  @Override
  public Set<ValidationError> validate(RegulationFiles regulationFiles, ValidationContext context) {
    Set<ValidationError> errors = Sets.newHashSet();
    for (File file : regulationFiles.getBpGroupingFiles()) {
      try {
        var bpGroup = yamlObjectMapper.readValue(file, BpGroupingConfiguration.class);
        var groupsProcessDefinitionIds = bpGroup.getGroups()
            .stream()
            .map(group -> Optional.ofNullable(group.getProcessDefinitions()).orElse(Lists.newArrayList()))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        var ungroupedProcessDefinitionIds = Optional.ofNullable(bpGroup.getUngrouped()).orElse(Lists.newArrayList());
        ungroupedProcessDefinitionIds.addAll(groupsProcessDefinitionIds);
        errors.addAll(
            validateProcessDefinitionExistence(ungroupedProcessDefinitionIds, file, regulationFiles, context));
        errors.addAll(
            validateProcessDefinitionUnique(ungroupedProcessDefinitionIds, file, context));
      } catch (IOException e) {
        errors.add(
            ValidationError.of(context.getRegulationFileType(), file, "File processing failure", e));
      }
    }
    return errors;
  }


  private Set<ValidationError> validateProcessDefinitionExistence(List<String> groupDefinitionIds,
      File bpGroupFile, RegulationFiles regulationFiles, ValidationContext context) {

    var processDefinitionIds = BpmnUtil.getBpmnFilesProcessDefinitionsId(regulationFiles);
    var nonExistentIds = groupDefinitionIds.stream()
        .filter(id -> !processDefinitionIds.contains(id))
        .collect(Collectors.toSet());

    if (nonExistentIds.isEmpty()) {
      return Collections.emptySet();
    } else {
      return Collections.singleton(toValidationError(NOT_EXIST_ERROR_MSG_FORMAT, bpGroupFile, context, nonExistentIds));
    }
  }

  private Set<ValidationError> validateProcessDefinitionUnique(List<String> groupDefinitionIds,
      File regulationFile, ValidationContext context) {

    var uniques = new HashSet<String>();
    var duplicates = groupDefinitionIds.stream()
        .filter(id -> !uniques.add(id))
        .collect(Collectors.toSet());

    if (duplicates.isEmpty()) {
      return Collections.emptySet();
    } else {
      return Collections.singleton( toValidationError(DUPLICATES_ERROR_MSG_FORMAT, regulationFile, context, duplicates));
    }
  }

  private ValidationError toValidationError(String msgFormat, File regulationFile,
      ValidationContext context, Set<String> duplicates) {
    return ValidationError.builder()
        .errorMessage(String.format(msgFormat, duplicates))
        .regulationFileType(context.getRegulationFileType())
        .regulationFile(regulationFile)
        .build();
  }
}