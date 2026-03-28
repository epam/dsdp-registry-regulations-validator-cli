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

package com.epam.digital.data.platform.registry.regulation.validation.cli.command.impl;

import com.epam.digital.data.platform.registry.regulation.validation.cli.command.Command;
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandFileOption;
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandParameters;
import com.epam.digital.data.platform.registry.regulation.validation.cli.service.OpenShiftService;
import com.epam.digital.data.platform.registry.regulation.validation.cli.utils.FileChecksumGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The class represents a command that is used to calculate checksums for files or directories and
 * save them to an OpenShift secret.
 *
 * <p>This class provides the ability to execute the command with or without detailed information
 * based on the specified options. When the detailed option is enabled, and the input is a
 * directory, it generates checksums for each nested file; otherwise, it generates a checksum for
 * the directory itself.
 *
 * <p>It utilizes a {@link JsonMapper} to convert the checksum data represented as a map into a
 * JSON-string format, and an {@link OpenShiftService} for saving the checksums to the secret.
 */
@Slf4j
@RequiredArgsConstructor
public class SaveCommand implements Command {

  private final JsonMapper jsonMapper;
  private final OpenShiftService openShiftService;

  @Override
  public void execute(CommandParameters parameters) {
    Map<String, String> filesChecksum;
    if (CommandFileOption.FILE_DETAILED.equals(parameters.getOption())) {
      var files = parameters.getRegulationFiles().getFilesDetailed();
      log.info("Generate detailed files checksum");
      filesChecksum = FileChecksumGenerator.generateDetailedFilesChecksum(files);
    } else {
      var files = parameters.getRegulationFiles().getFiles();
      log.info("Generate files checksum");
      filesChecksum = FileChecksumGenerator.generateFilesChecksum(files);
    }
    try {
      var stringFilesChecksum = jsonMapper.writeValueAsString(filesChecksum);
      var encodedStringFilesChecksum = Base64.getEncoder()
          .encodeToString(stringFilesChecksum.getBytes(StandardCharsets.UTF_8));
      log.info("Save files checksum to secret");
      openShiftService.saveBusinessOperationChecksumToSecret(parameters.getBusinessOperation(),
          encodedStringFilesChecksum);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Unable to convert object to string", e);
    }
  }
}
