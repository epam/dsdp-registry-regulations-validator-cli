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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.assets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.util.TestUtils;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.CompositeFileValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file.EmptyFileValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file.FileExistenceValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AssetsDirectoryValidatorTest {

  private AssetsDirectoryValidator validator;

  @BeforeEach
  public void setUp() {
    var assetsArgumentsValidator =
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new EmptyFileValidator())
            .build();
    this.validator = new AssetsDirectoryValidator(assetsArgumentsValidator);
  }

  @Test
  void shouldPassAssetsGroupFileValidation() {
    var assetsDirectory = TestUtils.getFile("registry-regulation/correct/assets");

    var errors =
        validator.validate(assetsDirectory, ValidationContext.of(RegulationFileType.ASSETS));

    assertThat(errors, is(empty()));
  }

  @Test
  void shouldFailAssetsGroupFileValidation() {
    var assetsDirectory = TestUtils.getFile("registry-regulation/broken/assets2");

    var errors =
        validator.validate(assetsDirectory, ValidationContext.of(RegulationFileType.ASSETS));

    assertThat(errors, is(not(empty())));
    assertThat(
        errors.iterator().next().getErrorMessage(), is("Assets file is missing: header-logo.svg"));
  }

  @Test
  void shouldFailAssetsGroupFileSizeValidation() {
    var assetsDirectory = TestUtils.getFile("registry-regulation/broken/assets");

    var errors =
        validator.validate(assetsDirectory, ValidationContext.of(RegulationFileType.ASSETS));

    assertThat(errors, is(not(empty())));
    assertThat(
        errors.iterator().next().getErrorMessage(),
        is("Assets logo files total size is too big, valid size is less than 1MiB"));
  }
}
