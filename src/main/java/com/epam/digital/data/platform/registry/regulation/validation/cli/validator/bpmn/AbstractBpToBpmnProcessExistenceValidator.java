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

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public abstract class AbstractBpToBpmnProcessExistenceValidator implements RegulationValidator<RegulationFiles> {

  public Set<ValidationError> validateBpFiles(Collection<File> bpFiles, Set<String> existingBpmnProcessIds, ValidationContext context) {
    Set<ValidationError> errors = Sets.newHashSet();

    for (File bpFile : bpFiles) {
      try {
        Set<String> bpFileProcessDefinitionsIds = getDefinitionsIdsFromBpFile(bpFile);
        bpFileProcessDefinitionsIds.removeAll(existingBpmnProcessIds);
        if (!bpFileProcessDefinitionsIds.isEmpty()) {
          errors.add(ValidationError.of(context.getRegulationFileType(),
                  bpFile, "Next process_definition_id's don't exist under BPMN files " + bpFileProcessDefinitionsIds));
        }
      } catch (IOException e) {
        errors.add(ValidationError.of(context.getRegulationFileType(), bpFile, "File processing failure", e));
      }
    }

    return errors;
  }

  protected abstract Set<String> getDefinitionsIdsFromBpFile(File bpFile) throws IOException;
}