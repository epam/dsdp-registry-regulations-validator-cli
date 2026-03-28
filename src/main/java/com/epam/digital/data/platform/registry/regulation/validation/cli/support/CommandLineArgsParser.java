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

import java.util.Arrays;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineArgsParser {

  private static final String JAR_NAME = "registry-regulations-cli";

  private final CommandLineParser parser;
  private final HelpFormatter helpFormatter;

  public CommandLineArgsParser() {
    this.parser = new DefaultParser();
    this.helpFormatter = newHelpFormatter();
  }

  public Options parse(String... args) throws ParseException {
    var options = new Options();
    var restArgs = Arrays.asList(args);
    while (!restArgs.isEmpty()) {
      var argArray = new String[restArgs.size()];
      restArgs.toArray(argArray);
      var commandLine = parser.parse(commandLineOptions(), argArray, true);
      Arrays.stream(commandLine.getOptions()).forEach(options::addOption);
      restArgs = commandLine.getArgList();
      if (!restArgs.isEmpty()) {
        restArgs.remove(0);
      }
    }
    return options;
  }

  public void printHelp() {
    var usageExamplePlanSaveCommand = new StringBuilder("[plan/save] command:");
    usageExamplePlanSaveCommand
        .append(System.lineSeparator())
        .append(String.format(
            "java -jar %s.jar -DOPENSHIFT_NAMESPACE=name plan update-bp-grouping --file=/bp-grouping",
            JAR_NAME))
        .append(System.lineSeparator())
        .append(String.format(
            "java -jar %s.jar -DOPENSHIFT_NAMESPACE=name save update-bp-grouping --file-detailed=/bp-grouping",
            JAR_NAME));
    this.helpFormatter.printHelp(usageExamplePlanSaveCommand.toString(), "Options:",
        saveAndPlanCommandOptions(), null, false);

    var usageExampleValidateCommand = new StringBuilder("[validate] command:");
    usageExampleValidateCommand
        .append(System.lineSeparator())
        .append(String.format(
            "java -jar %s.jar validate [--bp-auth-files=<arg>] [--bp-grouping-files=<arg>]",
            JAR_NAME));
    this.helpFormatter.printHelp(usageExampleValidateCommand.toString(),
        "Options:", validationOptions(), null, false);

    var usageExampleHelpCommand = new StringBuilder("[help] command:");
    usageExampleHelpCommand
        .append(System.lineSeparator())
        .append(String.format("java -jar %s.jar help", JAR_NAME));
    this.helpFormatter.printHelp(usageExampleHelpCommand.toString(), null, new Options(),
        "Exit codes: 0 (success), 1 (system error), 10 (validation failure)", false);
  }

  private HelpFormatter newHelpFormatter() {
    var formatter = new HelpFormatter();
    formatter.setSyntaxPrefix(System.lineSeparator() + "Usage ");
    formatter.setLongOptSeparator("=");
    formatter.setWidth(150);
    return formatter;
  }

  private Options commandLineOptions() {
    var allOptions = new Options();
    validationOptions().getOptions().forEach(allOptions::addOption);
    saveAndPlanCommandOptions().getOptions().forEach(allOptions::addOption);
    return allOptions;
  }

  private Options validationOptions() {
    var options = new Options();
    addAuthorizationAndIntegrationOptions(options);
    addProcessAndFormOptions(options);
    addNotificationTemplateOptions(options);
    addSettingsAndReportOptions(options);
    return options;
  }

  private void addAuthorizationAndIntegrationOptions(Options options) {
    options.addOption(Option.builder()
      .longOpt(CommandLineArg.BP_AUTH.getArgOptionName())
      .numberOfArgs(3)
      .valueSeparator(',')
      .desc("BP authorization regulation files (accepts multiple values separated by ',')")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.EXT_INTEGRATIONS_INBOUND.getArgOptionName())
      .hasArgs()
      .numberOfArgs(1)
      .desc("Configuration of inbound external integration files (accepts multiple values separated by ',')")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.EXT_INTEGRATIONS_OUTBOUND.getArgOptionName())
      .hasArgs()
      .numberOfArgs(1)
      .desc("Configuration of outbound external integration")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.ROLES.getArgOptionName())
      .numberOfArgs(3)
      .valueSeparator(',')
      .desc("Authorization roles regulation files (accepts multiple values separated by ',')")
      .build());
  }

  private void addProcessAndFormOptions(Options options) {
    options.addOption(Option.builder()
      .longOpt(CommandLineArg.BPMN.getArgOptionName())
      .hasArgs()
      .valueSeparator(',')
      .desc("Business processes regulation files (accepts multiple values separated by ',')")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.DMN.getArgOptionName())
      .hasArgs()
      .valueSeparator(',')
      .desc("Business rules regulation files (accepts multiple values separated by ',')")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.FORMS.getArgOptionName())
      .hasArgs()
      .valueSeparator(',')
      .desc("UI forms regulation files (accepts multiple values separated by ',')")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.DATAFACTORY_SETTINGS.getArgOptionName())
      .hasArgs()
      .numberOfArgs(1)
      .desc("Datafactory Settings regulation files with yml, yaml extensions")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.REGISTRY_SETTINGS.getArgOptionName())
      .hasArgs()
      .numberOfArgs(1)
      .desc("Registry Settings regulation files with yml, yaml extensions")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.LIQUIBASE.getArgOptionName())
      .hasArgs()
      .numberOfArgs(1)
      .desc("Liquibase regulation files introduce Database change set with xml extensions")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.EXCERPTS.getArgOptionName())
      .hasArgs()
      .valueSeparator(',')
      .desc("Folders that contain excerpts in different formats")
      .build());
  }

  private void addNotificationTemplateOptions(Options options) {
    options.addOption(Option.builder()
      .longOpt(CommandLineArg.EMAIL_NOTIFICATION_TEMPLATE.getArgOptionName())
      .hasArgs()
      .numberOfArgs(1)
      .desc("Email notification template directory")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.INBOX_NOTIFICATION_TEMPLATE.getArgOptionName())
      .hasArgs()
      .numberOfArgs(1)
      .desc("Inbox notification template directory")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.DIIA_NOTIFICATION_TEMPLATE.getArgOptionName())
      .hasArgs()
      .numberOfArgs(1)
      .desc("Diia notification template directory")
      .build());
  }

  private void addSettingsAndReportOptions(Options options) {
    options.addOption(Option.builder()
      .longOpt(CommandLineArg.GLOBAL_VARS.getArgOptionName())
      .numberOfArgs(1)
      .desc("Global variables regulation files (accepts multiple values separated by ',')")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.BP_GROUPING.getArgOptionName())
      .numberOfArgs(1)
      .desc("Business process grouping file")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.MOCK_INTEGRATIONS.getArgOptionName())
      .hasArgs()
      .valueSeparator(',')
      .desc("Mock integration regulation files (accepts multiple values separated by ',')")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.REPORTS_FOLDERS.getArgOptionName())
      .hasArgs()
      .valueSeparator(',')
      .desc("Folders with reports files (accepts multiple values separated by ',')")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.REPORTS.getArgOptionName())
      .hasArgs()
      .valueSeparator(',')
      .desc("Reports files (accepts multiple values separated by ',')")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.ASSETS.getArgOptionName())
      .hasArgs()
      .numberOfArgs(1)
      .desc("Directory with assets files")
      .build());
  }

  private Options saveAndPlanCommandOptions() {
    var options = new Options();

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.FILES.getArgOptionName())
      .hasArgs()
      .valueSeparator(',')
      .desc("List of folders and files (accepts multiple values separated by ',')")
      .build());

    options.addOption(Option.builder()
      .longOpt(CommandLineArg.FILES_DETAILED.getArgOptionName())
      .hasArgs()
      .valueSeparator(',')
      .desc(
        "List of folders and files, provides detailed information (accepts multiple values separated by ',')")
      .build());

    return options;
  }
}
