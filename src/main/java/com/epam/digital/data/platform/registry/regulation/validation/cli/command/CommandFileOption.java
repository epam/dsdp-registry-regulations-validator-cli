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
 * The enum represents different options for handling files or directories when executing commands
 * within an application. It specifies whether to use a standard file or a detailed file processing
 * mode.
 *
 * <p>This enum is commonly used to control how files or directories are processed when executing
 * various commands within the application. It offers two options:
 * - {@link #FILE}: Indicates that standard file processing should be used. In this mode, the
 *    command typically operates on a file or a directory as a single entity.
 * - {@link #FILE_DETAILED}: Indicates that detailed file processing should be used. In this mode,
 *   the command may recursively process the contents of a directory.
 *
 * <p>Each command may interpret these options differently, allowing for flexibility in how files
 * and directories are processed based on the specific command's requirements.
 */
public enum CommandFileOption {

  /**
   * Represents the standard file processing option.
   */
  FILE,

  /**
   * Represents the detailed file processing option.
   */
  FILE_DETAILED
}
