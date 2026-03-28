/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.role;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import com.epam.digital.data.platform.registry.regulation.validation.cli.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

class RoleUniqueNameValidatorTest {

    private RoleUniqueNameValidator validator;

    @BeforeEach
    public void setUp() {
        this.validator = new RoleUniqueNameValidator(new YAMLMapper());
    }

    @Test
    void shouldPassRoleUniqueNameValidation() {
        var roleFile = TestUtils.getFile("registry-regulation/correct/roles.yml");

        var errors = validator.validate(roleFile, ValidationContext.of(RegulationFileType.ROLES));

        assertThat(errors, is(empty()));
    }

    @Test
    void shouldFailRoleUniqueNameValidation() {
        var roleFile = TestUtils.getFile("registry-regulation/broken/roles-broken.yml");

        var errors = validator.validate(roleFile, ValidationContext.of(RegulationFileType.ROLES));

        assertThat(errors, is(not(empty())));
        assertThat(errors.iterator().next().getErrorMessage(),
                is("Duplicated role names found: Duplicated-role"));
    }
}