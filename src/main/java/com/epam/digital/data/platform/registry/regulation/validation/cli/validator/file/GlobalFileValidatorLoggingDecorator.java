/*
 * Copyright 2022 EPAM Systems.
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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Set;

@Slf4j
@AllArgsConstructor
public class GlobalFileValidatorLoggingDecorator implements RegulationValidator<RegulationFiles> {

  private RegulationValidator<RegulationFiles> validator;

  public static RegulationValidator<RegulationFiles> wrap(
          RegulationValidator<RegulationFiles> validator) {
    Assert.notNull(validator, "Validator must be not NULL");

    return new GlobalFileValidatorLoggingDecorator(validator);
  }

  @Override
  public Set<ValidationError> validate(RegulationFiles regulationFiles, ValidationContext context) {
    var errors = this.validator.validate(regulationFiles, context);

    if (errors.isEmpty()) {
      log.info("{} regulation files passed global validation successfully.",
              context.getRegulationFileType());
      return errors;
    }

    errors.forEach(error -> log.error(error.toString()));
    return errors;
  }
}