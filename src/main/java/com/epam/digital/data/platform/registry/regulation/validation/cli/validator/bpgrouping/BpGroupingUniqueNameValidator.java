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
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.BpGroupingConfiguration.Groups;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BpGroupingUniqueNameValidator implements RegulationValidator<File> {

  private static final String DUPLICATES_ERROR_MSG_FORMAT = "Duplicated group names found: %s";

  private final ObjectMapper yamlObjectMapper;

  public BpGroupingUniqueNameValidator(ObjectMapper yamlObjectMapper) {
    this.yamlObjectMapper = yamlObjectMapper;
  }

  @Override
  public Set<ValidationError> validate(File regulationFile, ValidationContext context) {
    Set<ValidationError> errors = Sets.newHashSet();
    try {
      var names = getGroupNames(regulationFile);
      var duplicateGroupNames = findDuplicates(names);

      if (duplicateGroupNames.isEmpty()) {
        return Collections.emptySet();
      }

      errors.add(ValidationError.builder()
          .regulationFileType(context.getRegulationFileType())
          .regulationFile(regulationFile)
          .errorMessage(String.format(DUPLICATES_ERROR_MSG_FORMAT, duplicateGroupNames))
          .build());
    } catch (IOException e) {
      errors.add(ValidationError.of(context.getRegulationFileType(), regulationFile,
          "File processing failure", e));
    }
    return errors;
  }

  protected List<String> getGroupNames(File bpGroupingFile) throws IOException {
    return yamlObjectMapper.readValue(bpGroupingFile, BpGroupingConfiguration.class)
        .getGroups()
        .stream()
        .map(Groups::getName)
        .collect(Collectors.toList());
  }

  private Set<String> findDuplicates(List<String> names) {
    var uniques = Sets.newHashSet();
    return names.stream()
        .filter(name -> !uniques.add(name))
        .collect(Collectors.toSet());
  }
}