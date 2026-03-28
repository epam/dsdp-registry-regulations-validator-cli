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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.BpAuthConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.RegulationConfigurationLoader;
import java.io.File;
import java.io.IOException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class TypedConfigurationValidatorTest {

  @Test
  @SuppressWarnings("unchecked")
  @SneakyThrows
  public void shouldLoadTypedConfigAndValidate() {
    var someFile = new File("");
    var bpAuthConfiguration = new BpAuthConfiguration();
    var validationContext = ValidationContext.empty();

    var bpAuthFileValidator = mock(RegulationValidator.class);
    var configurationLoader = mock(RegulationConfigurationLoader.class);

    when(configurationLoader.load(someFile, BpAuthConfiguration.class)).thenReturn(bpAuthConfiguration);

    var validator = TypedConfigurationValidator.<BpAuthConfiguration>builder()
        .configurationClass(BpAuthConfiguration.class)
        .configurationLoader(configurationLoader)
        .validator(bpAuthFileValidator)
        .build();

    validator.validate(someFile, validationContext);

    verify(configurationLoader, times(1)).load(someFile, BpAuthConfiguration.class);
    verify(bpAuthFileValidator, times(1)).validate(bpAuthConfiguration, validationContext);
  }

  @Test
  @SneakyThrows
  public void shouldFailIfUnableToLoadFile() {
    var someFile = new File("");
    var validationContext = ValidationContext.empty();

    var configurationLoader = mock(RegulationConfigurationLoader.class);
    when(configurationLoader.load(someFile, BpAuthConfiguration.class)).thenThrow(IOException.class);

    var validator = TypedConfigurationValidator.<BpAuthConfiguration>builder()
        .configurationClass(BpAuthConfiguration.class)
        .configurationLoader(configurationLoader)
        .build();

    var errors = validator.validate(someFile, validationContext);

    assertThat(errors, is(not(empty())));
  }
}