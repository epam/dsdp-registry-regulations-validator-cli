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

/**
 * The enum represents a set of predefined commands that can be executed within the application.
 * Each enum constant corresponds to a specific command, and it provides a unique identifier for
 * that command.
 *
 * <p>These commands define the primary actions that can be performed by the application, and they
 * are used to identify and execute the corresponding functionality.
 */
public enum Commands {

  /**
   * The "save" command, which is used to save checksums for files or directories.
   */
  SAVE("save"),

  /**
   * The "plan" command, which is used to calculate checksums for files or directories and compares
   * them with previously stored checksums.
   */
  PLAN("plan"),

  /**
   * The "validate" command, which validates regulation files and checks for compliance.
   */
  VALIDATE("validate"),

  /**
   * The "help" command, which provides information and guidance about available commands and their
   * usage.
   */
  HELP("help");

  /**
   * The command name associated with each enum constant.
   */
  private final String commandName;

  /**
   * Constructs a `Commands` enum constant with the specified command name.
   *
   * @param commandName The name of the command.
   */
  Commands(String commandName) {
    this.commandName = commandName;
  }

  /**
   * Retrieves the command name associated with the enum constant.
   *
   * @return The command name as a string.
   */
  public String getCommandName() {
    return commandName;
  }
}
