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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import java.io.File;
import java.util.Objects;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
public class ValidationError {

  private RegulationFileType regulationFileType;
  private File regulationFile;
  private String errorMessage;
  private String originalCauseMessage;

  public static ValidationError of(RegulationFileType regulationFileType, File regulationFile, String errorMessage) {
    return ValidationError.builder()
        .regulationFileType(regulationFileType)
        .regulationFile(regulationFile)
        .errorMessage(errorMessage)
        .build();
  }

  public static ValidationError of(RegulationFileType regulationFileType, File regulationFile, String errorMessage, Exception ex) {
    return ValidationError.builder()
        .regulationFileType(regulationFileType)
        .regulationFile(regulationFile)
        .errorMessage(errorMessage)
        .originalCauseMessage(ex.getMessage())
        .build();
  }

  @Override
  public String toString() {
    var regulationParentDirectory = "";
    var regulationFileName = "";
    if (Objects.nonNull(regulationFile)) {
      regulationParentDirectory = getParentDirectoryIfAny(regulationFile);
      regulationFileName = regulationFile.getName();
    }

    var messageFormatBuilder = new StringBuilder(String.format("[%s]", regulationFileType));
    messageFormatBuilder.append(StringUtils.isBlank(regulationParentDirectory)
        ? String.format("[%s] ", regulationFileName)
        : String.format("[%s/%s] ", regulationParentDirectory, regulationFileName));
    messageFormatBuilder.append(StringUtils.isBlank(originalCauseMessage)
        ? String.format("%s", errorMessage)
        : String.format("%s: %s", errorMessage, originalCauseMessage));
    return messageFormatBuilder.toString();
  }

  private String getParentDirectoryIfAny(File regulationFile) {
    return regulationFile.getParentFile() != null ? regulationFile.getParentFile().getName() : null;
  }
}