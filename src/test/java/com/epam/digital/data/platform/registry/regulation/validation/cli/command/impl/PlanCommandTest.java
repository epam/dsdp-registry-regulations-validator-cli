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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandFileOption;
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandParameters;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.service.OpenShiftService;
import com.epam.digital.data.platform.registry.regulation.validation.cli.util.TestUtils;
import com.epam.digital.data.platform.registry.regulation.validation.cli.utils.FileChecksumGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlanCommandTest {

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @Mock
  private JsonMapper jsonMapper;
  @Mock
  private OpenShiftService openShiftService;
  @InjectMocks
  private PlanCommand planCommand;

  @BeforeEach
  public void setUpStreams() {
    System.setOut(new PrintStream(outputStream));
  }

  @AfterEach
  public void restoreStreams() {
    System.setOut(originalOut);
  }

  @SneakyThrows
  @Test
  void shouldPrintFalseForNotChangedFilesWhenRunPlanCommand() {
    var checksumFromSecret = "dGVzdA==";
    var decodedChecksumFromSecret = Base64.getDecoder().decode(checksumFromSecret);
    var inputDir = TestUtils.getFile("registry-regulation/correct/bp-grouping");
    var regulationFiles = RegulationFiles.builder()
        .files(Collections.singleton(inputDir))
        .build();

    var parameters = new CommandParameters();
    parameters.setOption(CommandFileOption.FILE);
    parameters.setRegulationFiles(regulationFiles);
    parameters.setBusinessOperation("update-bp-grouping");

    var convertedChecksumFromSecret = FileChecksumGenerator.generateFilesChecksum(
        Collections.singleton(inputDir));
    when(openShiftService.getBusinessOperationChecksumFromSecret(parameters.getBusinessOperation()))
        .thenReturn(checksumFromSecret);
    when(jsonMapper.readValue(eq(decodedChecksumFromSecret), any(TypeReference.class)))
        .thenReturn(convertedChecksumFromSecret);

    planCommand.execute(parameters);

    assertEquals(
        "PlanCommandExecutionStart false PlanCommandExecutionEnd", outputStream.toString().trim());
  }

  @SneakyThrows
  @Test
  void shouldPrintTrueForChangedFilesWhenRunPlanCommand() {
    var checksumFromSecret = "dGVzdA==";
    var decodedChecksumFromSecret = Base64.getDecoder().decode(checksumFromSecret);
    var inputDir = TestUtils.getFile("registry-regulation/correct/bp-grouping");
    var regulationFiles = RegulationFiles.builder()
        .files(Collections.singleton(inputDir))
        .build();

    var parameters = new CommandParameters();
    parameters.setOption(CommandFileOption.FILE);
    parameters.setRegulationFiles(regulationFiles);
    parameters.setBusinessOperation("update-bp-grouping");

    var convertedChecksumFromSecret = new HashMap<String, String>();
    convertedChecksumFromSecret.put("bp-grouping",
        "77038579BD8B3CA7AC3F89FC5E0FAD3C303ED746FF8EF4D20918A4AF5290246C");
    when(openShiftService.getBusinessOperationChecksumFromSecret(parameters.getBusinessOperation()))
        .thenReturn(checksumFromSecret);
    when(jsonMapper.readValue(eq(decodedChecksumFromSecret), any(TypeReference.class)))
        .thenReturn(convertedChecksumFromSecret);

    planCommand.execute(parameters);

    assertEquals(
        "PlanCommandExecutionStart true PlanCommandExecutionEnd", outputStream.toString().trim());
  }

  @SneakyThrows
  @Test
  void shouldPrintChangedListFilesWhenRunPlanCommandWithDetailedOption() {
    var checksumFromSecret = "dGVzdA==";
    var decodedChecksumFromSecret = Base64.getDecoder().decode(checksumFromSecret);
    var inputDir = TestUtils.getFile("registry-regulation/correct/bp-grouping");
    var regulationFiles = RegulationFiles.builder()
        .filesDetailed(Collections.singleton(inputDir))
        .build();

    var parameters = new CommandParameters();
    parameters.setOption(CommandFileOption.FILE_DETAILED);
    parameters.setRegulationFiles(regulationFiles);
    parameters.setBusinessOperation("update-bp-grouping");

    var convertedChecksumFromSecret = FileChecksumGenerator.generateDetailedFilesChecksum(
        Collections.singleton(inputDir));
    var bpGroupingKey = "bp-grouping" + File.separator + "bp-grouping.yml";
    var processForGroup1Key = "bp-grouping" + File.separator + "bpmn" + File.separator
        + "process_for_group_1.bpmn";
    convertedChecksumFromSecret.put(bpGroupingKey, "changed-bp-grouping-checksum");
    convertedChecksumFromSecret.put(processForGroup1Key, "changed-process_for_group_1-checksum");
    when(openShiftService.getBusinessOperationChecksumFromSecret(parameters.getBusinessOperation()))
        .thenReturn(checksumFromSecret);
    when(jsonMapper.readValue(eq(decodedChecksumFromSecret), any(TypeReference.class)))
        .thenReturn(convertedChecksumFromSecret);

    planCommand.execute(parameters);

    String actualOutput = outputStream.toString().trim();
    assertTrue(actualOutput.contains("PlanCommandExecutionStart "));
    assertTrue(actualOutput.contains(" PlanCommandExecutionEnd"));
    assertTrue(actualOutput.contains(bpGroupingKey));
    assertTrue(actualOutput.contains(processForGroup1Key));
    assertTrue(actualOutput.contains("PlanDeletedExecutionStart"));
    assertTrue(actualOutput.contains(" PlanDeletedExecutionEnd"));
  }

  @SneakyThrows
  @Test
  void shouldPrintEmptyListFilesWhenRunPlanCommandWithDetailedOption() {
    var checksumFromSecret = "dGVzdA==";
    var decodedChecksumFromSecret = Base64.getDecoder().decode(checksumFromSecret);
    var inputDir = TestUtils.getFile("registry-regulation/correct/bp-grouping");
    var regulationFiles = RegulationFiles.builder()
        .filesDetailed(Collections.singleton(inputDir))
        .build();

    var parameters = new CommandParameters();
    parameters.setOption(CommandFileOption.FILE_DETAILED);
    parameters.setRegulationFiles(regulationFiles);
    parameters.setBusinessOperation("update-bp-grouping");

    var convertedChecksumFromSecret = FileChecksumGenerator.generateDetailedFilesChecksum(
        Collections.singleton(inputDir));
    when(openShiftService.getBusinessOperationChecksumFromSecret(parameters.getBusinessOperation()))
        .thenReturn(checksumFromSecret);
    when(jsonMapper.readValue(eq(decodedChecksumFromSecret), any(TypeReference.class)))
        .thenReturn(convertedChecksumFromSecret);

    planCommand.execute(parameters);

    String actualOutput = outputStream.toString().trim();
    assertTrue(actualOutput.contains("PlanCommandExecutionStart  PlanCommandExecutionEnd"));
    assertTrue(actualOutput.contains("PlanDeletedExecutionStart  PlanDeletedExecutionEnd"));
  }
}