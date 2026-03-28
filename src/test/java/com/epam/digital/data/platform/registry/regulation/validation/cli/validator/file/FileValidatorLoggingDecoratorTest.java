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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import java.io.File;
import org.junit.jupiter.api.Test;

public class FileValidatorLoggingDecoratorTest {

  @Test
  @SuppressWarnings("unchecked")
  public void shouldDelegateValidationToDecorated() {
    var fileToValidate = new File("");
    var context = ValidationContext.empty();
    var decoratedValidator = mock(RegulationValidator.class);

    FileValidatorLoggingDecorator.wrap(decoratedValidator).validate(fileToValidate, context);

    verify(decoratedValidator, times(1)).validate(fileToValidate, context);
  }
}