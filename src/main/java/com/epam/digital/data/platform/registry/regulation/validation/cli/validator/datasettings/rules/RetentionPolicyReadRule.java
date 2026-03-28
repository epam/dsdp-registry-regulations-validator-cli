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
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.datasettings.RulesOrder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RuleBean
@Rule(order = RulesOrder.RETENTION_POLICY_READ_RULE)
public class RetentionPolicyReadRule extends AbstractSettingsValidationRule {

    private static final int RETENTION_POLICY_READ_DAYS = 100;

    @When
    public boolean isRetentionPolicyLessThenN() {
        return datafactorySettingsYaml
                .getSettings()
                .getKafka()
                .getRetentionPolicyInDays()
                .getRead() < RETENTION_POLICY_READ_DAYS;
    }

    @Then
    public RuleState then() {
        log.warn("Are you sure you want to save the data from file {} " +
                        "in the READ topic for less than {} days? ", regulationFile, RETENTION_POLICY_READ_DAYS);
        return RuleState.NEXT;
    }
}
