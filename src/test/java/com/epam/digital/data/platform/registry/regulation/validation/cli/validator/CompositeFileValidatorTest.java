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

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import java.io.File;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class CompositeFileValidatorTest {

  @Test
  public void shouldIterateOverAllValidatorsIfNoErrorsEncountered() {
    var validator1 = mock(RegulationValidator.class);
    when(validator1.validate(any(), any())).thenReturn(Collections.emptySet());
    var validator2 = mock(RegulationValidator.class);
    when(validator2.validate(any(), any())).thenReturn(Collections.emptySet());

    var compositeValidator = CompositeFileValidator.builder()
        .validator(validator1)
        .validator(validator2)
        .build();

    var errors = compositeValidator.validate(any(), any());

    assertThat(errors, is(empty()));

    verify(validator1, times(1)).validate(any(), any());
    verify(validator2, times(1)).validate(any(), any());
  }

  @Test
  public void shouldStopIteratingIfErrorsEncountered() {
    var validationError = someValidationError();

    var validator1 = mock(RegulationValidator.class);
    when(validator1.validate(any(), any())).thenReturn(Collections.singleton(validationError));
    var validator2 = mock(RegulationValidator.class);

    var compositeValidator = CompositeFileValidator.builder()
        .validator(validator1)
        .validator(validator2)
        .build();

    var errors = compositeValidator.validate(any(), any());

    assertThat(errors, hasSize(1));
    assertThat(errors, hasItems(validationError));

    verify(validator1, times(1)).validate(any(), any());
    verifyNoInteractions(validator2);
  }

  private ValidationError someValidationError() {
    return ValidationError.of(RegulationFileType.BPMN, new File(""), "any error");
  }
}