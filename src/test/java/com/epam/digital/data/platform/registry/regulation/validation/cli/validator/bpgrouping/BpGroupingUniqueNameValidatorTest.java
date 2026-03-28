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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpgrouping;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.util.TestUtils;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BpGroupingUniqueNameValidatorTest {

  private BpGroupingUniqueNameValidator validator;

  @BeforeEach
  public void setUp() {
    this.validator = new BpGroupingUniqueNameValidator(new YAMLMapper());
  }

  @Test
  void shouldPassBpGroupingUniqueNameValidation() {
    var groupFile = TestUtils.getFile("registry-regulation/correct/bp-grouping/bp-grouping.yml");

    var errors = validator.validate(groupFile, ValidationContext.of(RegulationFileType.BP_GROUPING));

    assertThat(errors, is(empty()));
  }

  @Test
  void shouldFailBpGroupingNameDuplicatesValidation() {
    var groupFile = TestUtils.getFile("registry-regulation/broken/bp-grouping/bp-grouping-name-duplicates.yml");

    var errors = validator.validate(groupFile, ValidationContext.of(RegulationFileType.BP_GROUPING));

    assertThat(errors, is(not(empty())));
  }
}