/*
 * Copyright 2022 EPAM Systems.
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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.datasettings.rules;

import com.deliveredtechnologies.rulebook.RuleState;
import com.deliveredtechnologies.rulebook.annotation.Rule;
import com.deliveredtechnologies.rulebook.annotation.Then;
import com.deliveredtechnologies.rulebook.annotation.When;
import com.deliveredtechnologies.rulebook.spring.RuleBean;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.datasettings.RulesOrder;

@RuleBean
@Rule(order = RulesOrder.VERSION_DOES_NOT_MATCH_PATTERN_RULE)
public class VersionDoesNotMatchPatternRule extends AbstractSettingsValidationRule {

    private static final String PLATFORM_VERSION_PATTERN = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";

    @When
    public boolean isVersionIncorrect() {
        return !datafactorySettingsYaml
                .getSettings()
                .getGeneral()
                .getVersion()
                .matches(PLATFORM_VERSION_PATTERN);
    }

    @Then
    public RuleState then() {
        errors.add(ValidationError.of(regulationFileType, regulationFile, "Version should have next format x.x.x"));
        return RuleState.NEXT;
    }
}
