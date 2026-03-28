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
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.SystemExit;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidatorFactory;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The class represents a command that is used for validating registry regulation files. It provides
 * the capability to execute the validation process and report the results.
 *
 * <p>When executed, this command validates a collection of registry regulation files, checking
 * them for compliance with predefined rules and expectations. The validation process is performed
 * using a validator obtained from the {@link RegulationValidatorFactory}.
 *
 * <p>If there are no registry regulation files to validate, the command will log an informative
 * message and exit without performing any validation.
 *
 * <p>If the validation process detects errors or issues in the regulation files, it will log the
 * details of the errors and report a validation failure. Otherwise, it will log a success message
 * and complete the system exit process.
 *
 * <p>This class relies on the {@link RegulationValidatorFactory} to create a regulation files
 * validator tailored to the specific validation requirements.
 *
 * <p>It also uses the {@link SystemExit} to manage the exit process based on the validation
 * outcome.
 */
@Slf4j
@RequiredArgsConstructor
public class ValidateCommand implements Command {

  private final RegulationValidatorFactory registryRegulationValidatorFactory;
  private final SystemExit systemExit;

  @Override
  public void execute(CommandParameters parameters) {
    var regulationFiles = parameters.getRegulationFiles();
    if (regulationFiles.isEmpty()) {
      log.info("No registry regulation files to validate.");
      this.systemExit.complete();
      return;
    }
    var regulationFilesValidator = registryRegulationValidatorFactory.newRegulationFilesValidator();

    log.info("Starting registry regulation validation...");

    var errors = regulationFilesValidator.validate(regulationFiles, ValidationContext.empty());

    if (errors.isEmpty()) {
      log.info("Registry regulation validation passed successfully.");
      this.systemExit.complete();
      return;
    }

    log.error("Registry validation failed due to:");
    errors.forEach(error -> log.error(error.toString()));

    systemExit.validationFailure();
  }
}
