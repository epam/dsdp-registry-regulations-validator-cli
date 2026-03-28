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
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandParameters;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.CommandLineArgsParser;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.SystemExit;
import lombok.RequiredArgsConstructor;

/**
 * The class represents a command that is used to display help information to the user. It provides
 * the capability to execute and print help content.
 *
 * <p>When executed, this command prints out help information, typically describing how to use
 * the application and its available commands, options, and arguments. The help content is obtained
 * from the {@link CommandLineArgsParser}.
 *
 * <p>After printing the help content, the command completes the system exit process, ensuring
 * a graceful exit from the application.
 *
 * <p>This class relies on the {@link CommandLineArgsParser} to access and display the help
 * information, and it uses the {@link SystemExit} to manage the exit process after help content is
 * printed.
 */
@RequiredArgsConstructor
public class HelpCommand implements Command {

  private final CommandLineArgsParser commandLineArgsParser;
  private final SystemExit systemExit;

  @Override
  public void execute(CommandParameters parameters) {
    this.commandLineArgsParser.printHelp();
    this.systemExit.complete();
  }
}
