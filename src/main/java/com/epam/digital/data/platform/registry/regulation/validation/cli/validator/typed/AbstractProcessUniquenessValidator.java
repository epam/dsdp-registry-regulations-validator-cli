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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.typed;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.Identifiable;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractProcessUniquenessValidator<T extends RegulationConfiguration> implements RegulationValidator<T> {

  private static final String DUPLICATES_ERROR_MSG_FORMAT = "Duplicated process definitions found: %s";

  @Override
  public Set<ValidationError> validate(T regulation, ValidationContext context) {
    var processDefinitions = getProcessDefinitions(regulation);
    var duplicateItems = findDuplicates(processDefinitions);

    if (duplicateItems.isEmpty()) {
      return Collections.emptySet();
    }

    var joinedDuplicatedIds = duplicateItems.stream()
        .map(item -> String.format("'%s'", item.getId()))
        .collect(Collectors.joining(","));

    var regulationFileType = context.getRegulationFileType();
    var regulationFile = regulation.getRegulationFile();
    var validationError = ValidationError.builder()
        .regulationFileType(regulationFileType)
        .regulationFile(regulationFile)
        .errorMessage(String.format(DUPLICATES_ERROR_MSG_FORMAT, joinedDuplicatedIds))
        .build();

    return Collections.singleton(validationError);
  }

  protected abstract List<? extends Identifiable> getProcessDefinitions(T regulation);

  private <T extends Identifiable> Set<Identifiable> findDuplicates(List<T> identifiableItems) {
    var duplicates = Sets.newHashSet();
    return identifiableItems.stream()
        .filter(n -> !duplicates.add(n.getId()))
        .collect(Collectors.toSet());
  }
}
