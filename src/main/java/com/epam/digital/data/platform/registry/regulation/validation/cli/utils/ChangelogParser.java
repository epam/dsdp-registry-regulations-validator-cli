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

package com.epam.digital.data.platform.registry.regulation.validation.cli.utils;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.LiquibaseException;

import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;

import java.io.File;

public final class ChangelogParser {

  public static DatabaseChangeLog parseChangeLog(File changeLogFile) throws LiquibaseException {
    var fileOpener =
        new CompositeResourceAccessor(
            new FileSystemResourceAccessor(changeLogFile.getParentFile()),
            new ClassLoaderResourceAccessor(ChangelogParser.class.getClassLoader()));

    String changeLogFileName = changeLogFile.getName();
    ChangeLogParser parser = ChangeLogParserFactory.getInstance()
            .getParser(changeLogFileName, fileOpener);

    return parser.parse(changeLogFileName, new ChangeLogParameters(), fileOpener);
  }

  private ChangelogParser() {}
}
