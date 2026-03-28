/*
 * Copyright 2022 EPAM Systems.
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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.channel;

import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class NotificationTemplateDirectoryValidator implements RegulationValidator<File> {

  private static final String FILE_IS_NOT_DIRECTORY_ERROR_MSG = "Input file is not directory";

  private final RegulationValidator<File> templateValidator;

  @Override
  public Set<ValidationError> validate(File directory, ValidationContext context) {
    if (!directory.exists()) {
      return Collections.emptySet();
    }

    if (!directory.isDirectory()) {
      return Collections.singleton(ValidationError.builder()
          .regulationFileType(context.getRegulationFileType())
          .regulationFile(directory)
          .errorMessage(FILE_IS_NOT_DIRECTORY_ERROR_MSG)
          .build());
    }

    return Arrays.stream(Objects.requireNonNull(directory.listFiles(File::isDirectory)))
        .map(subDir -> templateValidator.validate(subDir, context))
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }
}
