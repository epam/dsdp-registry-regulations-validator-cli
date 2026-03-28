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

package com.epam.digital.data.platform.registry.regulation.validation.cli;

import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandFileOption;
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandManager;
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandParameters;
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.Commands;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.CommandLineArg;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.CommandLineArgsParser;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.CommandLineOptionsConverter;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.SystemExit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.springframework.boot.CommandLineRunner;

@Slf4j
public class RegistryRegulationCommandLineRunner implements CommandLineRunner {

  private static final int MIN_ARGS_LENGTH = 3;
  private static final int COMMAND_INDEX = 0;
  private static final int BUSINESS_OPERATION_INDEX = 1;
  private final CommandLineArgsParser commandLineArgsParser;
  private final CommandLineOptionsConverter commandLineOptionsConverter;

  private CommandManager commandManager;
  private final SystemExit systemExit;

  public RegistryRegulationCommandLineRunner(CommandLineArgsParser commandLineArgsParser,
      CommandLineOptionsConverter commandLineOptionsConverter, SystemExit systemExit,
      CommandManager commandManager) {
    this.commandLineArgsParser = commandLineArgsParser;
    this.commandLineOptionsConverter = commandLineOptionsConverter;
    this.commandManager = commandManager;
    this.systemExit = systemExit;
  }

  @Override
  public void run(String... args) {
    try {
      if (!isValidArgsLength(args)) {
        this.commandLineArgsParser.printHelp();
        this.systemExit.complete();
        return;
      }
      var command = commandManager.getCommand(args[COMMAND_INDEX]);
      var commandParameters = prepareCommandParameters(args);
      command.execute(commandParameters);

    } catch (ParseException e) {
      handleCommandLineParseException(e);
    }
  }

  private boolean isValidArgsLength(String... args) {
    if (args.length == 0) {
      return false;
    }
    var commandName = args[COMMAND_INDEX];
    if (Commands.PLAN.getCommandName().equals(commandName) || Commands.SAVE.getCommandName()
        .equals(commandName)) {
      return args.length >= MIN_ARGS_LENGTH;
    }
    return true;
  }

  private CommandParameters prepareCommandParameters(String... args)
      throws ParseException {
    var params = new CommandParameters();
    var commandName = args[COMMAND_INDEX];
    if (Commands.HELP.getCommandName().equals(commandName)) {
      return params;
    }
    var options = this.commandLineArgsParser.parse(args);
    var regulationFiles = commandLineOptionsConverter.convert(options);
    params.setRegulationFiles(regulationFiles);
    if (!Commands.VALIDATE.getCommandName().equals(commandName)) {
      params.setBusinessOperation(args[BUSINESS_OPERATION_INDEX]);
      params.setOption(getOptionForPlanAndSaveCommands(options));
    }
    return params;
  }

  private CommandFileOption getOptionForPlanAndSaveCommands(Options options) {
    if (options.hasOption(CommandLineArg.FILES_DETAILED.getArgOptionName())) {
      return CommandFileOption.FILE_DETAILED;
    } else if (options.hasOption(CommandLineArg.FILES.getArgOptionName())) {
      return CommandFileOption.FILE;
    }
    throw new IllegalArgumentException("Invalid option for command");
  }

  private void handleCommandLineParseException(ParseException e) {
    log.error("Parsing failure" + (e.getMessage() != null ? ": " + e.getMessage() : "")
        + ". Find help below:");

    this.commandLineArgsParser.printHelp();
    this.systemExit.systemError();
  }
}