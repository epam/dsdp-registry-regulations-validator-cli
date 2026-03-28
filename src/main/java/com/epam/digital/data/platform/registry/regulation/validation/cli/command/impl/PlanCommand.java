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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The class represents a command that is used to calculates checksums for files or directories and
 * compares them with previously stored checksums.
 *
 * <p>This class provides the ability to execute command with or without
 * detailed information based on the specified options.
 *
 * <p>It utilizes a {@link JsonMapper} for parsing JSON-string format data and an
 * {@link OpenShiftService} to retrieve checksums from a secret.
 */
@Slf4j
@RequiredArgsConstructor
public class PlanCommand implements Command {

  private final JsonMapper jsonMapper;
  private final OpenShiftService openShiftService;

  @SuppressWarnings("unchecked")
  @Override
  public void execute(CommandParameters parameters) {
    log.info("Get checksum from secret");
    var businessOperationChecksum = openShiftService.getBusinessOperationChecksumFromSecret(
        parameters.getBusinessOperation());
    Map<String, String> convertedChecksumFromSecret;
    try {
      if (Objects.nonNull(businessOperationChecksum)) {
        var decodedChecksumFromSecret = Base64.getDecoder().decode(businessOperationChecksum);
        convertedChecksumFromSecret = jsonMapper.readValue(decodedChecksumFromSecret, new TypeReference<>() {});
      } else {
        convertedChecksumFromSecret = new HashMap<>();
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to convert object to map", e);
    }
    Map<String, String> inputFilesChecksum;
    if (CommandFileOption.FILE_DETAILED.equals(parameters.getOption())) {
      var files = parameters.getRegulationFiles().getFilesDetailed();
      log.info("Generate detailed files checksum");
      inputFilesChecksum = FileChecksumGenerator.generateDetailedFilesChecksum(files);
      processDetailedFilesChecksum(inputFilesChecksum, convertedChecksumFromSecret);
    } else {
      var files = parameters.getRegulationFiles().getFiles();
      log.info("Generate files checksum");
      inputFilesChecksum = FileChecksumGenerator.generateFilesChecksum(files);
      processFilesChecksum(inputFilesChecksum, convertedChecksumFromSecret);
    }

  }

  private void processDetailedFilesChecksum(Map<String, String> inputFilesChecksum,
      Map<String, String> convertedChecksumFromSecret) {
    var changedFiles = new ArrayList<String>();
    var deletedFiles = new ArrayList<String>();
    log.info("Compare detailed files checksum");
    for (Map.Entry<String, String> entry : inputFilesChecksum.entrySet()) {
      var filePath = entry.getKey();
      var checksum = convertedChecksumFromSecret.get(filePath);
      if (!entry.getValue().equals(checksum)) {
        changedFiles.add(filePath);
      }
    }

    for (String filePath : convertedChecksumFromSecret.keySet()) {
      if (!inputFilesChecksum.containsKey(filePath)) {
        deletedFiles.add(filePath);
      }
    }

    printResult(String.join(",", changedFiles));
    printDeletedResult(String.join(",", deletedFiles));
  }

  private void processFilesChecksum(Map<String, String> inputFilesChecksum,
      Map<String, String> convertedChecksumFromSecret) {
    log.info("Compare files checksum");
    for (Map.Entry<String, String> entry : inputFilesChecksum.entrySet()) {
      var checksum = convertedChecksumFromSecret.get(entry.getKey());
      if (!entry.getValue().equals(checksum)) {
        printResult(Boolean.TRUE);
        return;
      }
    }
    printResult(Boolean.FALSE);
  }

  private void printResult(Object result) {
    System.out.printf("PlanCommandExecutionStart %s PlanCommandExecutionEnd%n", result);
  }

  private void printDeletedResult(Object result) {
    System.out.printf("PlanDeletedExecutionStart %s PlanDeletedExecutionEnd%n", result);
  }
}
