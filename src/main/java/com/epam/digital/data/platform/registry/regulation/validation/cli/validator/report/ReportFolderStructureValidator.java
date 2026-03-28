/*
 * Copyright 2024 EPAM Systems.
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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.report;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class ReportFolderStructureValidator implements RegulationValidator<RegulationFiles> {

  private static final List<String> ISO_LANGUAGES = Arrays.asList(Locale.getISOLanguages());

  @Override
  public Set<ValidationError> validate(RegulationFiles regulation, ValidationContext context) {
    Set<ValidationError> errors = new HashSet<>();

    regulation
        .getReportsFolders()
        .forEach(
            reportFolder -> {
              var roleFolders = getSubfolders(reportFolder);
              roleFolders.forEach(
                  roleFolder -> validateFolderContainsOnlySubfolders(roleFolder, errors, context));
              var roleLanguageFolders =
                  roleFolders.stream().map(this::getSubfolders).flatMap(List::stream).toList();
              roleLanguageFolders.forEach(
                  langFolder -> validateLanguageFolder(langFolder, errors, context));
            });
    return errors;
  }

  private void validateFolderContainsOnlySubfolders(
      File folder, Set<ValidationError> errors, ValidationContext context) {
    var folderSize = Optional.ofNullable(folder.listFiles()).map(files -> files.length).orElse(0);
    var subfolders = getSubfolders(folder);
    if (folderSize != subfolders.size()) {
      errors.add(
          ValidationError.of(
              context.getRegulationFileType(),
              folder,
              String.format(
                  "Invalid structure, folder %s should not contain anything except subfolders",
                  folder)));
    }
  }

  private List<File> getSubfolders(File folder) {
    return Optional.ofNullable(folder.listFiles(File::isDirectory))
        .map(Arrays::asList)
        .orElse(Collections.emptyList());
  }

  private void validateLanguageFolder(File langFolder, Set<ValidationError> errors, ValidationContext context) {
    if (!ISO_LANGUAGES.contains(langFolder.getName())) {
      errors.add(
          ValidationError.of(
              context.getRegulationFileType(),
              langFolder,
              String.format(
                  "Language %s is a non ISO-639, rename folder %s",
                  langFolder.getName(), langFolder)));
    }
  }
}
