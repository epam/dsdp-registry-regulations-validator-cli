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

package com.epam.digital.data.platform.registry.regulation.validation.cli;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.deliveredtechnologies.rulebook.model.RuleBook;
import com.deliveredtechnologies.rulebook.model.runner.RuleBookRunner;
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandManager;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.service.OpenShiftService;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.CommandLineArg;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.CommandLineArgsParser;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.CommandLineOptionsConverter;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.SystemExit;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidatorFactory;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.Sets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
class RegistryRegulationCommandLineRunnerTest {

  @Autowired
  private ResourceLoader resourceLoader;

  private SystemExit systemExit;
  private CommandLineArgsParser commandLineArgsParser;
  private OpenShiftService openShiftService;
  private JsonMapper jsonMapper;
  private CommandLineOptionsConverter commandLineOptionsConverter;
  private RegistryRegulationCommandLineRunner validationRunner;

  private static final String VALIDATE_COMMAND = "validate";
  private static final String HELP_COMMAND = "help";

  @BeforeEach
  void setUp() {
    this.systemExit = mock(SystemExit.class);
    this.commandLineArgsParser = mock(CommandLineArgsParser.class);
    this.commandLineOptionsConverter = mock(CommandLineOptionsConverter.class);
    this.openShiftService = mock(OpenShiftService.class);
    this.jsonMapper = mock(JsonMapper.class);
  }

  @Test
  void shouldDisplayHelpInfo() {
    validationRunner = newValidationRunner(resourceLoader, commandLineArgsParser,
        commandLineOptionsConverter, systemExit);

    validationRunner.run(HELP_COMMAND);

    verify(commandLineArgsParser, times(1)).printHelp();
    verify(systemExit, times(1)).complete();
  }

  @Test
  void shouldPassIfNoRegulationFilesSpecified() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(VALIDATE_COMMAND, "--bp-auth=");

    verify(systemExit, times(1)).complete();
  }

  @Test
  void shouldCompleteIfNoFilesPassed() throws ParseException {
    var emptyOptions = new Options();
    when(commandLineArgsParser.parse()).thenReturn(emptyOptions);
    when(commandLineOptionsConverter.convert(emptyOptions)).thenReturn(emptyRegulationFiles());

    validationRunner = newValidationRunner(resourceLoader, commandLineArgsParser,
        commandLineOptionsConverter, systemExit);

    validationRunner.run();

    verify(systemExit, times(1)).complete();
  }

  @Test
  void shouldDisplayHelpIfUnrecognizedOptionPassed() throws ParseException {
    when(commandLineArgsParser.parse("-unrecognized")).thenThrow(ParseException.class);

    validationRunner = newValidationRunner(resourceLoader, commandLineArgsParser,
        commandLineOptionsConverter, systemExit);

    validationRunner.run("-unrecognized");

    verify(commandLineArgsParser, times(1)).printHelp();
    verify(systemExit, times(1)).systemError();
  }

  @Test
  void shouldPassWithoutValidationErrorsLogged() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(correctRegistryRegulations().toArray(new String[0]));

    verify(systemExit, times(1)).complete();
  }

  @Test
  void shouldPassEmptyRegulationsWithoutValidationErrorsLogged() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(emptyRegistryRegulations());

    verify(systemExit, times(1)).complete();
  }

  @Test
  void shouldFailBpmnDueToDuplicatedProcessId() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(correctRegistryRegulations().toArray(new String[0]));
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.BPMN,
        testResourcePathOf("registry-regulation/broken/test-duplicated-process-id-bp-1.bpmn"),
        testResourcePathOf("registry-regulation/broken/test-duplicated-process-id-bp-2.bpmn"))
    );

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailBpAuthDueToDuplicates() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.BP_AUTH,
        testResourcePathOf("registry-regulation/broken/bp-auth-duplicates.yml")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailInboundIntegrationsDueToDuplicates() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.EXT_INTEGRATIONS_INBOUND,
        testResourcePathOf("registry-regulation/broken/inbound-integration-duplicates.yml")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailInboundIntegrationConfigDueToDeprecatedTrembitaExternalGatewayPropsPresent() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.EXT_INTEGRATIONS_OUTBOUND,
        testResourcePathOf(
            "registry-regulation/broken/broken-inbound-integration-deprecated-trembita-ex-gateway.yml")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailInboundIntegrationConfigDueToRenamedTrembitaPropsPresent() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
            new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.EXT_INTEGRATIONS_OUTBOUND,
            testResourcePathOf(
                    "registry-regulation/broken/broken-inbound-integration-deprecated-trembita-properties.yml")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailOutboundIntegrationConfigDueToIllegalExtSystemsPropsPresent() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.EXT_INTEGRATIONS_OUTBOUND,
        testResourcePathOf(
            "registry-regulation/broken/broken-outbound-integration-deprecated-ext-systems-properties.yml")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailDatafactorySettingYamlFileDueToInvalidParams() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.DATAFACTORY_SETTINGS,
        testResourcePathOf("registry-regulation/broken/settings.yaml")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailRegistrySettingsYamlFileDueToInvalidParams() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.REGISTRY_SETTINGS,
        testResourcePathOf("registry-regulation/broken/registry-settings-long-title.yaml")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldPassRegistrySettingsYamlFileWithOneCharacterTitle() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.REGISTRY_SETTINGS,
        testResourcePathOf("registry-regulation/broken/registry-settings-one-char-title.yaml")));

    verify(systemExit, times(1)).complete();
  }

  @Test
  void shouldPassRegistrySettingsYamlFileWithThreeCharacterTitleFull() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.REGISTRY_SETTINGS,
        testResourcePathOf("registry-regulation/broken/registry-settings-three-chars-title-full.yaml")));

    verify(systemExit, times(1)).complete();
  }

  @Test
  void shouldFailRegistrySettingsYamlFileDueTitleFullLessThreeCharacters() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.REGISTRY_SETTINGS,
        testResourcePathOf("registry-regulation/broken/registry-settings-two-chars-title-full.yaml")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailRegistrySettingsYamlFileDueToEmptyTitleOrTitleFull() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.REGISTRY_SETTINGS,
        testResourcePathOf("registry-regulation/broken/registry-settings-empty-title.yaml")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailReportQueriesUniqueNameValidations() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
            new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.REPORTS,
            testResourcePathOf("registry-regulation/broken/reports/officer/uk/queries/queries.json")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailAssetsNameValidations() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
            new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.ASSETS,
            testResourcePathOf("registry-regulation/broken/assets2")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailMainLiquibaseFileDueToInvalidParams() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.LIQUIBASE,
        testResourcePathOf("registry-regulation/broken/main-liquibase/test-main-liquibase.xml")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailEmailNotificationDueToInvalidMetadata() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.EMAIL_NOTIFICATION_TEMPLATE,
        testResourcePathOf("registry-regulation/broken/email")));

    verify(systemExit).validationFailure();
  }

  @Test
  void shouldFailEmailNotificationDueToNoTemplateFile() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.EMAIL_NOTIFICATION_TEMPLATE,
        testResourcePathOf("registry-regulation/broken/email2")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailInboxNotificationEmptyMetadata() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.INBOX_NOTIFICATION_TEMPLATE,
        testResourcePathOf("registry-regulation/broken/inbox")));

    verify(systemExit).validationFailure();
  }

  @Test
  void shouldFailInboxNotificationDueToNoMetadataFile() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.INBOX_NOTIFICATION_TEMPLATE,
        testResourcePathOf("registry-regulation/broken/inbox2")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailDiiaNotificationDueToInvalidParams() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.DIIA_NOTIFICATION_TEMPLATE,
        testResourcePathOf("registry-regulation/broken/diia")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldFailDiiaNotificationDueToTemplateFolderIsNotDirectory() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.DIIA_NOTIFICATION_TEMPLATE,
        testResourcePathOf("registry-regulation/correct/global-vars.yml")));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldPassIfDirectoryIsNotExists() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);
    validationRunner.run(VALIDATE_COMMAND, argOf(CommandLineArg.DIIA_NOTIFICATION_TEMPLATE,
        "registry-regulation/correct/absent_directory"));

    verify(systemExit, times(1)).complete();
  }

  @Test
  void shouldPassBpGroupingValidation() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(VALIDATE_COMMAND, bpmnArgsForBpGroupingRegistryRegulations(),
        argOf(CommandLineArg.BP_GROUPING,
            testResourcePathOf("registry-regulation/correct/bp-grouping/bp-grouping.yml"))
    );

    verify(systemExit).complete();
  }

  @Test
  void shouldPassBpGroupingValidationWithEmptyDefinitionsArray() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(VALIDATE_COMMAND, bpmnArgsForBpGroupingRegistryRegulations(),
        argOf(CommandLineArg.BP_GROUPING,
            testResourcePathOf(
                "registry-regulation/correct/bp-grouping/bp-grouping-empty-array.yml"))
    );

    verify(systemExit).complete();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "registry-regulation/broken/bp-grouping/bp-grouping-process-definition-id-duplicates.yml",
      "registry-regulation/broken/bp-grouping/bp-grouping-process-definition-id-not-exist.yml",
      "registry-regulation/broken/bp-grouping/bp-grouping-name-duplicates.yml",
      "registry-regulation/broken/bp-grouping/bp-grouping-invalid-name.yml"
  })
  void shouldFailBpGroupingValidation(String bpGroupsFile) {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(VALIDATE_COMMAND, bpmnArgsForBpGroupingRegistryRegulations(),
        argOf(CommandLineArg.BP_GROUPING, testResourcePathOf(bpGroupsFile))
    );

    verify(systemExit).validationFailure();
  }

  @Test
  void shouldFailBPMNFIleInputValidation() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    List<String> regulations = new ArrayList<>(correctRegistryRegulations());
    regulations.add(argOf(CommandLineArg.BPMN,
        testResourcePathOf("registry-regulation/broken/process-for-validating-inputs.bpmn")));
    validationRunner.run(regulations.toArray(new String[0]));

    verify(systemExit, times(1)).validationFailure();
  }

  @Test
  void shouldPassBPMNFIleInputValidation() {
    validationRunner = newValidationRunner(resourceLoader, new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit);

    validationRunner.run(correctRegistryRegulations().toArray(new String[0]));

    verify(systemExit, times(0)).validationFailure();
  }

  private String bpmnArgsForBpGroupingRegistryRegulations() {
    return argOf(CommandLineArg.BPMN,
        testResourcePathOf("registry-regulation/correct/bp-grouping/bpmn/process_for_group_1.bpmn"),
        testResourcePathOf(
            "registry-regulation/correct/bp-grouping//bpmn/process_for_group_2.bpmn"));
  }

  private RegistryRegulationCommandLineRunner newValidationRunner(ResourceLoader resourceLoader,
      CommandLineArgsParser commandLineArgsParser,
      CommandLineOptionsConverter commandLineOptionsConverter,
      SystemExit systemExit) {
    var validatorFactory = new RegulationValidatorFactory(resourceLoader, new YAMLMapper(),
        new JsonMapper(), settingsYamlRuleBook(), mainLiquibaseRuleBook());

    var elementTemplatePath = Objects.requireNonNull(
            getClass().getClassLoader().getResource("business-process-modeler-element-template.json"))
        .getPath();
    ReflectionTestUtils.setField(validatorFactory, "elementTemplatePath", elementTemplatePath);
    ReflectionTestUtils.setField(validatorFactory, "defaultRoles", List.of("testRole", "citizen", "officer"));

    return new RegistryRegulationCommandLineRunner(commandLineArgsParser,
        commandLineOptionsConverter, systemExit,
        new CommandManager(validatorFactory, commandLineArgsParser, systemExit, openShiftService,
            jsonMapper));
  }

  private RuleBook<Set<ValidationError>> settingsYamlRuleBook() {
    return getRuleBookRunner(
        "com.epam.digital.data.platform.registry.regulation.validation.cli.validator.datasettings.rules");
  }

  private RuleBook<Set<ValidationError>> mainLiquibaseRuleBook() {
    return getRuleBookRunner(
        "com.epam.digital.data.platform.registry.regulation.validation.cli.validator.mainliquibase.rules");
  }

  @SuppressWarnings("unchecked")
  private RuleBook<Set<ValidationError>> getRuleBookRunner(String rulePackage) {
    var springAwareRuleBookRunner = new RuleBookRunner(rulePackage);
    springAwareRuleBookRunner.setDefaultResult(Sets.newHashSet());
    return springAwareRuleBookRunner;
  }

  private List<String> correctRegistryRegulations() {
    return List.of(
        VALIDATE_COMMAND,
        argOf(CommandLineArg.GLOBAL_VARS,
            testResourcePathOf("registry-regulation/correct/global-vars.yml")),
        argOf(CommandLineArg.BP_AUTH,
            testResourcePathOf("registry-regulation/correct/bp-auth.yml")),
        argOf(CommandLineArg.EXT_INTEGRATIONS_INBOUND,
            testResourcePathOf("registry-regulation/correct/inbound-integration.yml")),
        argOf(CommandLineArg.EXT_INTEGRATIONS_OUTBOUND,
            testResourcePathOf("registry-regulation/correct/outbound-integration.yml")),
        argOf(CommandLineArg.ROLES,
            testResourcePathOf("registry-regulation/correct/officer.yml"),
            testResourcePathOf("registry-regulation/correct/citizen.yml")),
        argOf(CommandLineArg.BPMN,
            testResourcePathOf("registry-regulation/correct/process.bpmn"),
            testResourcePathOf("registry-regulation/correct/process-for-validating-inputs.bpmn"),
            testResourcePathOf("registry-regulation/correct/inbound-process.bpmn")),
        argOf(CommandLineArg.DMN, testResourcePathOf("registry-regulation/correct/rule.dmn")),
        argOf(CommandLineArg.FORMS,
            testResourcePathOf("registry-regulation/correct/ui-form.json")),
        argOf(CommandLineArg.DATAFACTORY_SETTINGS,
            testResourcePathOf("registry-regulation/correct/settings.yaml")),
        argOf(CommandLineArg.LIQUIBASE,
            testResourcePathOf("registry-regulation/correct/test-main-liquibase.xml")),
        argOf(CommandLineArg.EMAIL_NOTIFICATION_TEMPLATE,
            testResourcePathOf("registry-regulation/correct/email")),
        argOf(CommandLineArg.INBOX_NOTIFICATION_TEMPLATE,
            testResourcePathOf("registry-regulation/correct/inbox")),
        argOf(CommandLineArg.DIIA_NOTIFICATION_TEMPLATE,
            testResourcePathOf("registry-regulation/correct/diia")),
        argOf(CommandLineArg.EXCERPTS,
            testResourcePathOf("registry-regulation/correct/excerpts-docx")),
        argOf(CommandLineArg.MOCK_INTEGRATIONS,
            testResourcePathOf("registry-regulation/correct/mock-integrations.json")),
        argOf(CommandLineArg.REPORTS,
            testResourcePathOf("registry-regulation/correct/reports/citizen/uk/laboratory.json"),
            testResourcePathOf("registry-regulation/correct/reports/citizen/uk/queries/queries.json"),
            testResourcePathOf("registry-regulation/correct/reports/citizen/uk/registration.json"),
            testResourcePathOf("registry-regulation/correct/reports/officer/uk/laboratory.json"),
            testResourcePathOf("registry-regulation/correct/reports/officer/uk/queries/queries.json"),
            testResourcePathOf("registry-regulation/correct/reports/officer/uk/registration.json")),
        argOf(CommandLineArg.REPORTS_FOLDERS,
            testResourcePathOf("registry-regulation/correct/reports")),
        argOf(CommandLineArg.ASSETS,
            testResourcePathOf("registry-regulation/correct/assets"))
    );
  }

  private String[] emptyRegistryRegulations() {
    return new String[]{
        VALIDATE_COMMAND,
        argOf(CommandLineArg.BP_AUTH,
            testResourcePathOf("registry-regulation/empty/bp-auth-empty.yml")),
        argOf(CommandLineArg.EXT_INTEGRATIONS_INBOUND,
            testResourcePathOf("registry-regulation/empty/inbound-integration.yml")),
        argOf(CommandLineArg.ROLES,
            testResourcePathOf("registry-regulation/empty/roles-empty.yml")),
        argOf(CommandLineArg.BP_GROUPING,
            testResourcePathOf("registry-regulation/empty/bp-grouping-empty.yml"))
    };
  }

  private String argOf(CommandLineArg arg, Path... resourcePath) {
    return String.format("--%s=%s",
        arg.getArgOptionName(),
        Arrays.stream(resourcePath).map(Path::toString).collect(Collectors.joining(",")));
  }

  private String argOf(CommandLineArg arg, String... resourcePath) {
    return String.format("--%s=%s",
        arg.getArgOptionName(),
        String.join(",", resourcePath));
  }

  @SneakyThrows
  private Path testResourcePathOf(String resource) {
    var classLoader = getClass().getClassLoader();
    return Paths.get(classLoader.getResource(resource).toURI());
  }

  private RegulationFiles emptyRegulationFiles() {
    return RegulationFiles.builder().build();
  }
}