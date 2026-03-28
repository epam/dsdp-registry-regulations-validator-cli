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

import com.deliveredtechnologies.rulebook.annotation.Given;
import com.deliveredtechnologies.rulebook.annotation.Result;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.FactNames;
import liquibase.change.Change;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractMainLiquibaseValidationRule {

    @Given(FactNames.DATABASE_ALL_CHANGES)
    protected List<Change> allChanges;

    @Given(FactNames.REGULATION_FILE_TYPE)
    protected RegulationFileType regulationFileType;

    @Given(FactNames.REGULATION_FILE)
    protected File regulationFile;

    @Result
    protected Set<ValidationError> errors;

    public <T> List<T> getChangesByType(Class<T> type) {
        return allChanges.stream()
                .filter(c -> type.isAssignableFrom(c.getClass()))
                .map(x -> (T) x)
                .collect(Collectors.toList());
    }
}