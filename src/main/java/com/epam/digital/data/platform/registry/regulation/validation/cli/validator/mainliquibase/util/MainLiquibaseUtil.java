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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.mainliquibase.util;

import com.epam.digital.data.platform.registry.regulation.validation.cli.utils.ChangelogParser;
import liquibase.change.Change;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public final class MainLiquibaseUtil {

  private MainLiquibaseUtil() {
  }

  public static DatabaseChangeLog getDatabaseChangeLog(File regulationFile) throws LiquibaseException {
    return ChangelogParser.parseChangeLog(regulationFile);
  }

  public static List<Change> getAllChanges(DatabaseChangeLog databaseChangeLog) {
    return databaseChangeLog.getChangeSets()
        .stream()
        .flatMap(x -> x.getChanges().stream())
        .collect(Collectors.toList());
  }
}
