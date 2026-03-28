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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.assets;

import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.google.common.collect.Sets;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssetsDirectoryValidator implements RegulationValidator<File> {

  private static final Set<String> ASSETS_NAMES =
      Set.of("header-logo.svg", "loader-logo.svg", "favicon.png");
  private static final String FILE_IS_NOT_DIRECTORY_ERROR_MSG = "Input file is not directory";
  private static final String DIRECTORY_NOT_EXISTS_ERROR_MSG = "Assets directory is missing";
  private static final String FILE_IS_MISSING_ERROR_MSG_FORMAT = "Assets file is missing: %s";
  private static final String TOO_BIG_ERROR_MSG =
      "Assets logo files total size is too big, valid size is less than 1MiB";
  private static final String UNSAFE_SVG_CONTENT_MSG =
      "SVG file contains potentially unsafe content: %s";
  public static final long ONE_MEGABYTE = 1048576L;

  private final RegulationValidator<File> fileValidator;
  private final SVGValidator svgSanitizer = new SVGValidator();

  @Override
  public Set<ValidationError> validate(File directory, ValidationContext context) {
    Set<ValidationError> errors = checkDirectoryValidity(directory, context);
    if (!errors.isEmpty()) {
      return errors;
    }

    Set<String> existingAssetsNames = new HashSet<>();
    long totalSize = 0L;
    File[] files = directory.listFiles(File::isFile);
    if (files != null) {
      totalSize = processFiles(files, context, existingAssetsNames, errors);
    }

    ASSETS_NAMES.stream()
        .filter(asset -> !existingAssetsNames.contains(asset))
        .forEach(
            asset ->
                errors.add(
                    ValidationError.builder()
                        .errorMessage(String.format(FILE_IS_MISSING_ERROR_MSG_FORMAT, asset))
                        .regulationFileType(context.getRegulationFileType())
                        .build()));
    if (totalSize > ONE_MEGABYTE) {
      errors.add(
          ValidationError.builder()
              .errorMessage(TOO_BIG_ERROR_MSG)
              .regulationFileType(context.getRegulationFileType())
              .build());
    }
    return errors;
  }

  private Set<ValidationError> checkDirectoryValidity(File directory, ValidationContext context) {
    if (!directory.exists()) {
      return Collections.singleton(
        ValidationError.builder()
          .regulationFileType(context.getRegulationFileType())
          .regulationFile(directory)
          .errorMessage(DIRECTORY_NOT_EXISTS_ERROR_MSG)
          .build());
    }

    if (!directory.isDirectory()) {
      return Collections.singleton(
        ValidationError.builder()
          .regulationFileType(context.getRegulationFileType())
          .regulationFile(directory)
          .errorMessage(FILE_IS_NOT_DIRECTORY_ERROR_MSG)
          .build());
    }
    return Sets.newHashSet();
  }

  private long processFiles(File[] files, ValidationContext context, Set<String> existingAssetsNames,
                            Set<ValidationError> errors) {
    long totalSize = 0L;
    for (File file : files) {
      if (!ASSETS_NAMES.contains(file.getName())) {
        continue;
      }

      errors.addAll(fileValidator.validate(file, context));

      if (file.getName().endsWith(".svg")) {
        ValidationResult validationResult = svgSanitizer.validateSvg(file);
        if (!validationResult.isValid()) {
          errors.add(
            createSvgValidationError(validationResult.getErrorMessage(), file, context));
        }
      }

      existingAssetsNames.add(file.getName());
      totalSize += file.length();
    }

    return totalSize;
  }

  private ValidationError createSvgValidationError(String specifics, File file,
      ValidationContext context) {
    return ValidationError.builder()
        .errorMessage(String.format(UNSAFE_SVG_CONTENT_MSG, specifics))
        .regulationFileType(context.getRegulationFileType())
        .regulationFile(file)
        .build();
  }

}