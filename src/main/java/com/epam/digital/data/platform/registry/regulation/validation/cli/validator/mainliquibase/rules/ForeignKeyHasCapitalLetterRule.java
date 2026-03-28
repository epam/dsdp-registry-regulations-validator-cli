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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.mainliquibase.rules;

import com.deliveredtechnologies.rulebook.RuleState;
import com.deliveredtechnologies.rulebook.annotation.Rule;
import com.deliveredtechnologies.rulebook.annotation.Then;
import com.deliveredtechnologies.rulebook.annotation.When;
import com.deliveredtechnologies.rulebook.spring.RuleBean;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.mainliquibase.RulesOrder;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.core.CreateTableChange;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RuleBean
@Rule(order = RulesOrder.FOREIGN_KEY_HAS_CAPITAL_LETTER_RULE)
public class ForeignKeyHasCapitalLetterRule extends AbstractMainLiquibaseValidationRule {

    private static final String HAS_UPPER_CASE_SYMBOL_PATTERN = "^.*\\p{javaUpperCase}.*$";

    private List<String> foreignKeyIdentifiers;

    @When
    public boolean checkForeignKeys() {
        foreignKeyIdentifiers = getForeignKeys(
                getChangesByType(CreateTableChange.class))
                .stream()
                .filter(fk -> fk.matches(HAS_UPPER_CASE_SYMBOL_PATTERN))
                .collect(Collectors.toList());

        return !foreignKeyIdentifiers.isEmpty();
    }

    @Then
    public RuleState then() {
        errors.add(ValidationError.of(regulationFileType, regulationFile,
                "The following foreign keys contain uppercase characters, "
                        + "which is invalid: " + foreignKeyIdentifiers));
        return RuleState.NEXT;
    }


    protected Set<String> getForeignKeys(List<CreateTableChange> changes) {
        return changes.stream()
                .flatMap(x -> x.getColumns().stream())
                .map(ColumnConfig::getConstraints)
                .filter(Objects::nonNull)
                .map(ConstraintsConfig::getForeignKeyName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
