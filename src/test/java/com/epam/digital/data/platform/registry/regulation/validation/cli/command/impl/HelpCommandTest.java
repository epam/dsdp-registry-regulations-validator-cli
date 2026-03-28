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

import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandParameters;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.CommandLineArgsParser;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.SystemExit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HelpCommandTest {

  @Mock
  private CommandLineArgsParser commandLineArgsParser;
  @Mock
  private SystemExit systemExit;
  @InjectMocks
  private HelpCommand helpCommand;

  @Test
  void shouldPrintHelpAndComplete() {
    helpCommand.execute(new CommandParameters());

    verify(commandLineArgsParser).printHelp();
    verify(systemExit).complete();
  }
}
