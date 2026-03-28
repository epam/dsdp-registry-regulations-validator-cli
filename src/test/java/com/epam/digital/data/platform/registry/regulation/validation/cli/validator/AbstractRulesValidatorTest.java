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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator;

import com.deliveredtechnologies.rulebook.model.RuleBook;
import com.deliveredtechnologies.rulebook.model.runner.RuleBookRunner;
import com.google.common.collect.Sets;

import java.io.File;
import java.util.Set;

public abstract class AbstractRulesValidatorTest {

    protected File getFileFromClasspath(String filePath) {
        var classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(filePath).getFile());
    }

    @SuppressWarnings("unchecked")
    protected RuleBook<Set<ValidationError>> getRuleBookRunner(String rulePackage)  {
        var springAwareRuleBookRunner = new RuleBookRunner(rulePackage);
        springAwareRuleBookRunner.setDefaultResult(Sets.newHashSet());
        return springAwareRuleBookRunner;
    }
}