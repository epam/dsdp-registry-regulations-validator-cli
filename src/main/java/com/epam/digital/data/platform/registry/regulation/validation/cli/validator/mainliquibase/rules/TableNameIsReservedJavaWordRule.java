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
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.SourceVersion;
import liquibase.change.core.CreateTableChange;

@RuleBean
@Rule(order = RulesOrder.TABLE_NAME_IS_RESERVED_JAVA_WORD_RULE)
public class TableNameIsReservedJavaWordRule extends AbstractMainLiquibaseValidationRule {

  private List<String> tableNames;

  @When
  public boolean checkTableNames() {
    tableNames = getChangesByType(CreateTableChange.class)
        .stream()
        .map(CreateTableChange::getTableName)
        .filter(SourceVersion::isKeyword)
        .collect(Collectors.toList());

    return !tableNames.isEmpty();
  }

  @Then
  public RuleState then() {
    errors.add(ValidationError.of(regulationFileType, regulationFile,
        "The following table names are equal to Java reserved words, which is not allowed: "  + tableNames));
    return RuleState.NEXT;
  }
}
