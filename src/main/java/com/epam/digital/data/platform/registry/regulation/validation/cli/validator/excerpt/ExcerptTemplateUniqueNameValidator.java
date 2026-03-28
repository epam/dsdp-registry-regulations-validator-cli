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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.excerpt;

import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class ExcerptTemplateUniqueNameValidator implements RegulationValidator<Collection<File>> {

  private static final String DUPLICATED_FILE_NAME_ERROR_MSG_FORMAT = "Не унікальні імена витягів: %s";

  @Override
  public Set<ValidationError> validate(Collection<File> directories, ValidationContext context) {
    Set<String> allExcerptNames = new HashSet<>();
    Set<ValidationError> errors = new HashSet<>();

    for (File dir : directories) {
      if (!dir.exists()) {
        continue;
      }
      
      var files = Arrays.asList(Objects.requireNonNull(dir.listFiles(file -> !file.isHidden())));
      var excerptNames = files.stream()
          .map(this::convertToExcerptName)
          .filter(StringUtils::isNotBlank)
          .collect(Collectors.toSet());

      Collection<String> duplicates = getDuplicates(allExcerptNames, excerptNames);
      if (!duplicates.isEmpty()) {
        errors.add(toValidationError(duplicates, dir, context));
      }
      allExcerptNames.addAll(excerptNames);
    }
    return errors;
  }

  private String convertToExcerptName(File file) {
    return file.isDirectory() ? file.getName() : FilenameUtils.getBaseName(file.getName());
  }

  private Collection<String> getDuplicates(Collection<String> c1, Collection<String> c2) {
    return c1.stream().filter(c2::contains).collect(Collectors.toSet());
  }

  private ValidationError toValidationError(
      Collection<String> duplicates,
      File regulationFile,
      ValidationContext validationContext) {
    return ValidationError.builder()
        .errorMessage(String.format(DUPLICATED_FILE_NAME_ERROR_MSG_FORMAT, duplicates))
        .regulationFileType(validationContext.getRegulationFileType())
        .regulationFile(regulationFile)
        .build();
  }
}
