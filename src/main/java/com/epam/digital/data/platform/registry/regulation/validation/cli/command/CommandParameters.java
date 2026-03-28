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

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import lombok.Data;

/**
 * The class represents the parameters required for executing various application commands. It
 * encapsulates essential information needed by commands to perform their respective operations.
 *
 * <p>This class serves as a container for command-specific parameters and is used to pass data
 * between the application's command-line argument parser and individual command implementations..
 *
 * <p>Instances of this class are typically created and populated by the command-line argument
 * parsing logic before being passed to the relevant command for execution. The structure and
 * contents of the `CommandParameters` object depend on the specific command being executed.
 */
@Data
public class CommandParameters {

  /**
   * The regulation files associated with the command. These files may represent the input data that
   * the command operates on. The structure and contents of the `RegulationFiles` object depend on
   * the specific command being executed.
   */
  private RegulationFiles regulationFiles;

  /**
   * The name of the business operation relevant to the command. This name may serve as a reference
   * to a specific operation or task within the application.
   */
  private String businessOperation;

  /**
   * The command option that determines the behavior of the command. It specifies whether the
   * command should operate in detailed mode, standard mode, or with other variations, depending on
   * the application's requirements.
   */
  private CommandFileOption option;
}
