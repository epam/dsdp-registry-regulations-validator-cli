/*
 * Copyright 2021 EPAM Systems.
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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.RegulationConfigurationLoader;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Getter
@Builder
public class TypedConfigurationValidator<T extends RegulationConfiguration> implements RegulationValidator<File> {

  private final RegulationConfigurationLoader configurationLoader;
  private final Class<T> configurationClass;

  @Singular
  private List<RegulationValidator<T>> validators;

  @Override
  public Set<ValidationError> validate(File regulationFile, ValidationContext validationContext) {
    try {
      T configuration = this.configurationLoader.load(regulationFile, this.configurationClass);
      var errors = new LinkedHashSet<ValidationError>();
      for (var validator : validators) {
        errors.addAll(validator.validate(configuration, validationContext));
      }
      return errors;
    } catch (IOException ex) {
      return Collections.singleton(
          ValidationError.of(validationContext.getRegulationFileType(), regulationFile, "File processing failure", ex)
      );
    }
  }
}