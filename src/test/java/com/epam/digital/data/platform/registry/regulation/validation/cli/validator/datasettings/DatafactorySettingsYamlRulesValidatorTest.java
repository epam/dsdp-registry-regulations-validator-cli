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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.datasettings;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.AbstractRulesValidatorTest;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

class DatafactorySettingsYamlRulesValidatorTest extends AbstractRulesValidatorTest {

    private RegulationValidator<File> validator;

    @BeforeEach
    public void setUp() {
        this.validator = new DatafactorySettingsYamlRulesValidator(new YAMLMapper(),
                getRuleBookRunner(
                        "com.epam.digital.data.platform.registry.regulation.validation.cli.validator.datasettings.rules"));
    }

    @Test
    void shouldPassSettingsYamlValidation() {
        var processFile = getFileFromClasspath("registry-regulation/correct/settings.yaml");

        var errors = validator.validate(processFile, ValidationContext.of(RegulationFileType.DATAFACTORY_SETTINGS));

        assertThat(errors, is(empty()));
    }

    @Test
    void shouldFailSettingsYamlValidation() {
        var processFile = getFileFromClasspath("registry-regulation/broken/settings.yaml");

        var errors = validator.validate(processFile, ValidationContext.of(RegulationFileType.DATAFACTORY_SETTINGS));

        assertThat(errors, is(not(empty())));
    }
}