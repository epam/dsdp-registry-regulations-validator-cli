/*
 * Copyright 2023 EPAM Systems.
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

package com.epam.digital.data.platform.registry.regulation.validation.cli.support;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommandLineArgsParserTest {

  private CommandLineArgsParser commandLineArgsParser;

  @BeforeEach
  public void setUp() {
    this.commandLineArgsParser = new CommandLineArgsParser();
  }

  @Test
  public void shouldSupportFileOption() throws ParseException {
    var options = commandLineArgsParser.parse("--file=test");

    assertTrue(options.hasOption("file"));
  }

  @Test
  public void shouldSupportFileDetailedOption() throws ParseException {
    var options = commandLineArgsParser.parse("--file-detailed=test");

    assertTrue(options.hasOption("file-detailed"));
  }

  @Test
  public void shouldSupportGlobalVarsOption() throws ParseException {
    var options = commandLineArgsParser.parse("--global-vars-files=test");

    assertTrue(options.hasOption("global-vars-files"));
  }

  @Test
  public void shouldRequireArgumentForGlobalVarsOption() {
    assertThrows(MissingArgumentException.class, () -> commandLineArgsParser.parse("--global-vars-file"));
  }

  @Test
  public void shouldSupportBpAuthOption() throws ParseException {
    var options = commandLineArgsParser.parse("--bp-auth-files=test");

    assertTrue(options.hasOption("bp-auth-files"));
  }

  @Test
  public void shouldSupportMultipleArgumentsForBpAuthOption() throws ParseException {
    var options = commandLineArgsParser.parse("--bp-auth-files=test1,test2");
    String[] values = options.getOption("bp-auth-files").getValues();

    assertArrayEquals(new String[]{"test1", "test2"}, values);
  }

  @Test
  public void shouldSupportExtIntegrationsInboundOption() throws ParseException {
    var options = commandLineArgsParser.parse("--ext-integrations-inbound=test");

    assertTrue(options.hasOption("ext-integrations-inbound"));
  }

  @Test
  public void shouldSupportRolesOption() throws ParseException {
    var options = commandLineArgsParser.parse("--roles-files=test");

    assertTrue(options.hasOption("roles-files"));
  }

  @Test
  public void shouldSupportBpmnOption() throws ParseException {
    var options = commandLineArgsParser.parse("--bpmn-files=test");

    assertTrue(options.hasOption("bpmn-files"));
  }

  @Test
  public void shouldSupportDmnOption() throws ParseException {
    var options = commandLineArgsParser.parse("--dmn-files=test");

    assertTrue(options.hasOption("dmn-files"));
  }

  @Test
  public void shouldSupportFormsOption() throws ParseException {
    var options = commandLineArgsParser.parse("--form-files=test");

    assertTrue(options.hasOption("form-files"));
  }

  @Test
  void shouldIgnoreUnrecognizedOption() throws ParseException {
    var options = commandLineArgsParser.parse("--liquibase-files=main-liquibase.xml",
        "--unknown-option", "--diia-notification-template-folder=path/to/template");

    assertTrue(options.hasOption("liquibase-files"));
    assertTrue(options.hasOption("diia-notification-template-folder"));
  }
  @Test
  void shouldSupportDatafactorySettingsYamlOption() throws ParseException {
    var options = commandLineArgsParser.parse("--datafactory-settings-files=settings.yaml");

    assertTrue(options.hasOption("datafactory-settings-files"));
  }

  @Test
  void shouldSupportRegistrySettingsYamlOption() throws ParseException {
    var options = commandLineArgsParser.parse("--registry-settings-files=registry-settings.yaml");

    assertTrue(options.hasOption("registry-settings-files"));
  }

  @Test
  void shouldSupportLiquibaseOption() throws ParseException {
    var options = commandLineArgsParser.parse("--liquibase-files=main-liquibase.xml");

    assertTrue(options.hasOption("liquibase-files"));
  }

  @Test
  void shouldSupportNotificationTemplateOptions() throws ParseException {
    var options =
        commandLineArgsParser.parse(
            "--email-notification-template-folder=path/to/emailTemplate",
            "--inbox-notification-template-folder=path/to/inboxTemplate",
            "--diia-notification-template-folder=path/to/diiaTemplate");

    assertTrue(options.hasOption("email-notification-template-folder"));
    assertTrue(options.hasOption("inbox-notification-template-folder"));
    assertTrue(options.hasOption("diia-notification-template-folder"));
  }
}