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

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.BpAuthConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.BpRoleConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
@Slf4j
public class BpAuthToBpmnRoleExistenceValidator implements RegulationValidator<RegulationFiles> {

  private final ObjectMapper yamlObjectMapper;
  private final List<String> defaultRoles;

  @Override
  public Set<ValidationError> validate(RegulationFiles regulation, ValidationContext context) {
    Set<BpAuthConfiguration> processConfigs = new HashSet<>();
    Set<String> existingRoles = new HashSet<>();
    Set<ValidationError> errors = new HashSet<>();

    for (File file : regulation.getBpAuthFiles()) {
      try {
        processConfigs.add(getConfigsFromBpFile(file));
      } catch (IOException e) {
        log.trace("Error while reading bpAuth file: {}", file.getAbsolutePath(), e);
        errors.add(
            ValidationError.of(context.getRegulationFileType(), file,
                String.format("Exception during reading file %s", e.getMessage())));
      }
    }

    for (File file : regulation.getRolesFiles()) {

      try {
        existingRoles.addAll(getExistingRolesInBpFile(file));
      } catch (IOException e) {
        log.trace("Error while reading roles in file: {}", file.getAbsolutePath(), e);
        errors.add(
            ValidationError.of(context.getRegulationFileType(), file,
                String.format("Exception during reading file %s", e.getMessage())));
      }
    }
    if (!CollectionUtils.isEmpty(defaultRoles)) {
      existingRoles.addAll(defaultRoles);
    }
    processConfigs.forEach(config -> config.getAuthorization().getProcessDefinitions().stream()
        .flatMap(definition -> definition.getRoles().stream())
        .distinct()
        .filter(roleName -> !existingRoles.contains(roleName))
        .forEach(roleName -> errors.add(
            ValidationError.of(context.getRegulationFileType(), config.getRegulationFile(),
                String.format("Role with name : %s does not exists", roleName)))));

    return errors;
  }

  private BpAuthConfiguration getConfigsFromBpFile(File bpFile) throws IOException {
    BpAuthConfiguration configuration =
        yamlObjectMapper.readValue(bpFile, BpAuthConfiguration.class);
    configuration.setRegulationFile(bpFile);
    return configuration;
  }

  private Set<String> getExistingRolesInBpFile(File bpFile) throws IOException {
    return yamlObjectMapper.readValue(bpFile, BpRoleConfiguration.class)
        .getRoles().stream().map(BpRoleConfiguration.Role::getName).collect(Collectors.toSet());
  }

}
