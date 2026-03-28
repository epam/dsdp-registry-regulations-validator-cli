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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.excerpt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import java.io.File;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ExcerptTemplateUniqueNameValidatorTest {

  private final ExcerptTemplateUniqueNameValidator groupValidator = new ExcerptTemplateUniqueNameValidator();

  @Test
  void shouldFailWhenFoldersContainDuplicates() {
    var directories = Set.of(
        new File("src/test/resources/registry-regulation/broken/excerpts"),
        new File("src/test/resources/registry-regulation/broken/excerpts-docx")
    );

    var errors = this.groupValidator.validate(directories,
        ValidationContext.of(RegulationFileType.EXCERPTS));

    assertThat(errors, is(not(empty())));
    assertThat(errors.iterator().next().getErrorMessage(),
        is("Не унікальні імена витягів: [template]"));
  }
}
