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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.dmn;

import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import java.io.File;
import java.util.Collections;
import java.util.Set;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelException;
import org.camunda.bpm.model.xml.ModelValidationException;

public class DmnFileValidator implements RegulationValidator<File> {

  @Override
  public Set<ValidationError> validate(File regulationFile, ValidationContext validationContext) {
    try {
      var dmnModelInstance = Dmn.readModelFromFile(regulationFile);
      Dmn.validateModel(dmnModelInstance);
      return Collections.emptySet();
    } catch (DmnModelException ex) {
      return Collections.singleton(
          ValidationError.of(validationContext.getRegulationFileType(), regulationFile, "DMN file parsing failure", ex)
      );
    } catch (ModelValidationException ex) {
      return Collections.singleton(
          ValidationError.of(validationContext.getRegulationFileType(), regulationFile, "DMN file validation against the schema failure", ex)
      );
    }
  }
}
