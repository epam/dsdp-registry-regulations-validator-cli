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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandFileOption;
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandParameters;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.service.OpenShiftService;
import com.epam.digital.data.platform.registry.regulation.validation.cli.util.TestUtils;
import com.epam.digital.data.platform.registry.regulation.validation.cli.utils.FileChecksumGenerator;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaveCommandTest {

  private final JsonMapper jsonMapper = new JsonMapper();
  @Mock
  private JsonMapper jsonMapperMock;
  @Mock
  private OpenShiftService openShiftService;
  @InjectMocks
  private SaveCommand saveCommand;

  @SneakyThrows
  @Test
  void shouldSaveChecksumsWhenRunSaveCommand() {
    var inputDir = TestUtils.getFile("registry-regulation/correct/bp-grouping");
    var regulationFiles = RegulationFiles.builder()
        .files(Collections.singleton(inputDir))
        .build();

    var parameters = new CommandParameters();
    parameters.setOption(CommandFileOption.FILE);
    parameters.setRegulationFiles(regulationFiles);
    parameters.setBusinessOperation("update-bp-grouping");
    var fileChecksums = FileChecksumGenerator.generateFilesChecksum(
        Collections.singleton(inputDir));
    var stringFilesChecksum = jsonMapper.writeValueAsString(fileChecksums);
    var encodedStringFilesChecksum = Base64.getEncoder()
        .encodeToString(stringFilesChecksum.getBytes(StandardCharsets.UTF_8));
    when(jsonMapperMock.writeValueAsString(fileChecksums)).thenReturn(stringFilesChecksum);

    saveCommand.execute(parameters);

    verify(openShiftService).saveBusinessOperationChecksumToSecret(
        parameters.getBusinessOperation(), encodedStringFilesChecksum);
  }

  @SneakyThrows
  @Test
  void shouldSaveChecksumsWhenRunSaveCommandWithDetailedOption() {
    var inputDir = TestUtils.getFile("registry-regulation/correct/bp-grouping");
    var regulationFiles = RegulationFiles.builder()
        .filesDetailed(Collections.singleton(inputDir))
        .build();

    var parameters = new CommandParameters();
    parameters.setOption(CommandFileOption.FILE_DETAILED);
    parameters.setRegulationFiles(regulationFiles);
    parameters.setBusinessOperation("update-bp-grouping");

    var fileChecksums = FileChecksumGenerator.generateDetailedFilesChecksum(
        Collections.singleton(inputDir));
    var stringFilesChecksum = jsonMapper.writeValueAsString(fileChecksums);
    var encodedStringFilesChecksum = Base64.getEncoder()
        .encodeToString(stringFilesChecksum.getBytes(StandardCharsets.UTF_8));
    when(jsonMapperMock.writeValueAsString(fileChecksums)).thenReturn(stringFilesChecksum);

    saveCommand.execute(parameters);

    verify(openShiftService).saveBusinessOperationChecksumToSecret(
        parameters.getBusinessOperation(), encodedStringFilesChecksum);
  }
}