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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.report;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.BpRoleConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ReportRoleExistenceValidator implements RegulationValidator<RegulationFiles> {

  private final ObjectMapper yamlObjectMapper;
  private final String officerPermissionsFile;
  private final List<String> defaultRoles;

  @Override
  public Set<ValidationError> validate(RegulationFiles regulation, ValidationContext context) {
    List<String> reportsRolesNames = new ArrayList<>();
    Set<String> existingRoles = new HashSet<>();
    Set<ValidationError> errors = new HashSet<>();

    regulation.getReportsFolders().forEach(folder ->
        reportsRolesNames.addAll(getReportsRolesNames(folder)));

    for (File file : regulation.getRolesFiles()) {
      try {
        if (file.getName().equals(officerPermissionsFile)) {
          existingRoles.addAll(getExistingRolesInBpFile(file));
        }
      } catch (IOException e) {
        log.trace("Failed to read roles in file {}", file.getName(), e);
        errors.add(
            ValidationError.of(context.getRegulationFileType(), file,
                "Exception during reading file"));
      }
    }

    if (null != defaultRoles) {
      existingRoles.addAll(defaultRoles);
    }

    for (String role : reportsRolesNames) {
      if (!existingRoles.contains(role)) {
        errors.add(
            ValidationError.of(context.getRegulationFileType(), new File(role),
                String.format("Role with name : %s does not exists", role)));
      }
    }
    return errors;
  }

  private List<String> getReportsRolesNames(File folder) {
    List<String> roles = new ArrayList<>();
    if (null != folder.listFiles() && folder.isDirectory()) {
      roles = new ArrayList<>(
          Arrays.asList(
              folder.listFiles(File::isDirectory)
          )
      ).stream().map(File::getName).collect(Collectors.toList());
    }
    return roles;
  }

  private Set<String> getExistingRolesInBpFile(File bpFile) throws IOException {
    return yamlObjectMapper.readValue(bpFile, BpRoleConfiguration.class)
        .getRoles().stream().map(BpRoleConfiguration.Role::getName)
        .collect(Collectors.toSet());
  }

}
