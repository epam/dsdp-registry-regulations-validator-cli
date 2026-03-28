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
 * The interface represents a generic command that can be executed within an application.
 * Implementing classes or subtypes of this interface define specific behaviors and actions to be
 * taken when the command is executed.
 *
 * <p>The primary method of this interface, {@link #execute(CommandParameters)}, is responsible for
 * carrying out the command's functionality based on the provided command parameters. It
 * encapsulates the logic and operations associated with the command's execution.
 *
 * <p>Typically, concrete implementations of this interface are created for each distinct command
 * supported by the application, and they encapsulate the unique behavior of that command.
 */
public interface Command {


  /**
   * Executes the command with the given command parameters.
   *
   * @param parameters The command parameters that provide context and input for the command's
   *                   execution.
   */
  void execute(CommandParameters parameters);
}
