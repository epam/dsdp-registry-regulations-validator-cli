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

package com.epam.digital.data.platform.registry.regulation.validation.cli.command;

import com.epam.digital.data.platform.registry.regulation.validation.cli.command.impl.HelpCommand;
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.impl.PlanCommand;
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.impl.SaveCommand;
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.impl.ValidateCommand;
import com.epam.digital.data.platform.registry.regulation.validation.cli.service.OpenShiftService;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.CommandLineArgsParser;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.SystemExit;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidatorFactory;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The class is responsible for managing and providing access to various commands within an
 * application. It registers and stores commands, allowing retrieval by their associated command
 * names.
 *
 * <p>This class centralizes the management of commands used in the application. It associates each
 * command with a unique command name and provides a way to retrieve a specific command instance
 * based on its name.
 *
 * <p>If a request is made for a command that is not registered, the `CommandManager` provides a
 * default command (a help command), to prevent exceptions or unexpected behavior.
 */
public class CommandManager {

  private static final Map<String, Command> COMMANDS = new HashMap<>();

  public CommandManager(RegulationValidatorFactory registryRegulationValidatorFactory,
      CommandLineArgsParser commandLineArgsParser, SystemExit systemExit,
      OpenShiftService openShiftService, JsonMapper jsonMapper) {
    registerCommand(Commands.SAVE.getCommandName(), new SaveCommand(jsonMapper, openShiftService));
    registerCommand(Commands.PLAN.getCommandName(), new PlanCommand(jsonMapper, openShiftService));
    registerCommand(Commands.VALIDATE.getCommandName(),
        new ValidateCommand(registryRegulationValidatorFactory, systemExit));
    registerCommand(Commands.HELP.getCommandName(),
        new HelpCommand(commandLineArgsParser, systemExit));
  }

  /**
   * Retrieves a registered command instance by its associated command name.
   *
   * @param commandName The name of the command to retrieve.
   * @return The command instance associated with the specified command name. If the specified
   * command name is not found, a default command (a help command) is returned.
   */
  public Command getCommand(String commandName) {
    return Optional.ofNullable(COMMANDS.get(commandName))
        .orElse(COMMANDS.get(Commands.HELP.getCommandName()));
  }

  private void registerCommand(String commandName, Command command) {
    COMMANDS.put(commandName, command);
  }
}
