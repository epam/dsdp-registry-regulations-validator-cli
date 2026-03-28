/*
 * Copyright 2024 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.form;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.BpRoleConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class FormCardRoleExistenceValidator implements
    RegulationValidator<RegulationFiles> {

  private static final String CARD_TYPE = "card";

  private final ObjectMapper yamlObjectMapper;
  private final ObjectMapper jsonObjectMapper;
  private final List<String> defaultRoles;

  @Override
  public Set<ValidationError> validate(RegulationFiles regulation, ValidationContext context) {
    Set<String> regulationExistingRoles = new HashSet<>();
    Set<ValidationError> errors = new HashSet<>();

    for (File file: regulation.getRolesFiles()) {
      try {
        regulationExistingRoles.addAll(getExistingRolesInBpFile(file));
      } catch (IOException e) {
        log.trace("Failed to read roles in file {}: {}", file.getName(), e.getMessage(), e);
        errors.add(
                ValidationError.of(context.getRegulationFileType(), file,
                        String.format("Exception during reading file %s", e.getMessage())));
      }
    }
    if (!CollectionUtils.isEmpty(defaultRoles)) {
      regulationExistingRoles.addAll(defaultRoles);
    }

    for (File file : regulation.getFormFiles()) {
      try {
        var form = jsonObjectMapper.readTree(file);
        var formType = getFormType(form);
        if (!CARD_TYPE.equals(formType)) {
          continue;
        }
        Set<String> cardRoles = getRolesFromFormCard(form);
        cardRoles.forEach(role -> {
          if (!regulationExistingRoles.contains(role)) {
            errors.add(ValidationError.of(context.getRegulationFileType(), file,
                    String.format("Role with name: %s does not exists", role)));
          }
        });
      } catch (IOException e) {
        log.trace("Failed to read form file {}: {}", file.getName(), e.getMessage(), e);
        errors.add(
                ValidationError.of(context.getRegulationFileType(), file,
                        String.format("Exception during reading file %s", e.getMessage())));
      }
    }
    return errors;
  }

  private Set<String> getExistingRolesInBpFile(File bpFile) throws IOException {
    return yamlObjectMapper.readValue(bpFile, BpRoleConfiguration.class)
            .getRoles().stream().map(BpRoleConfiguration.Role::getName).collect(Collectors.toSet());
  }

  private String getFormType(JsonNode form) {
    return Optional.ofNullable(form.get("type")).map(JsonNode::asText).orElse(null);
  }

  private Set<String> getRolesFromFormCard(JsonNode form) throws IOException {
    var rolesNode = form.get("roles");
    if (rolesNode == null || rolesNode.isNull()) {
      return Collections.emptySet();
    }
    var reader = jsonObjectMapper.readerFor(new TypeReference<Set<String>>() {});
    return reader.readValue(rolesNode);
  }
}
