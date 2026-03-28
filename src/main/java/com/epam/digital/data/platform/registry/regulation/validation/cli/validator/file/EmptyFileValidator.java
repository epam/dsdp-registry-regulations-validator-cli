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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file;

import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

public class EmptyFileValidator implements RegulationValidator<File> {

  @Override
  public Set<ValidationError> validate(File regulationFile, ValidationContext validationContext) {
    try {
      var content = readFileContent(regulationFile);
      if (StringUtils.isBlank(content)) {
        return Collections.singleton(
            ValidationError.of(validationContext.getRegulationFileType(), regulationFile, "File must not be empty")
        );
      }
      return Collections.emptySet();
    } catch (IOException ex) {
      return Collections.singleton(
          ValidationError.of(validationContext.getRegulationFileType(), regulationFile, "File processing failure", ex)
      );
    }
  }

  private String readFileContent(File regulationFile) throws IOException {
    var strLines = Files.readLines(regulationFile, StandardCharsets.UTF_8);
    return String.join("", strLines);
  }
}
