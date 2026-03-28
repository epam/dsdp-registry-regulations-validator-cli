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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn;

import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.google.common.collect.Sets;
import java.io.File;
import java.util.Collection;
import java.util.Set;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.instance.Process;

public class BpmnFileGroupUniqueProcessIdValidator implements RegulationValidator<Collection<File>> {

  private static final String DUPLICATED_PROCESS_ID_ERROR_MSG_FORMAT = "[%s] Process_id не унікальний";

  @Override
  public Set<ValidationError> validate(Collection<File> bpmnFiles, ValidationContext context) {
    Set<String> processIds = Sets.newHashSet();
    Set<ValidationError> errors = Sets.newHashSet();
    bpmnFiles.forEach(file -> Bpmn.readModelFromFile(file).getModelElementsByType(Process.class).forEach(process -> {
      if (processIds.contains(process.getId())) {
        errors.add(toValidationError(process, file, context));
      }
      processIds.add(process.getId());
    }));
    return errors;
  }

  private ValidationError toValidationError(Process process, File regulationFile,
      ValidationContext validationContext) {
    return ValidationError.builder()
        .errorMessage(String.format(DUPLICATED_PROCESS_ID_ERROR_MSG_FORMAT, process.getId()))
        .regulationFileType(validationContext.getRegulationFileType())
        .regulationFile(regulationFile)
        .build();
  }
}
