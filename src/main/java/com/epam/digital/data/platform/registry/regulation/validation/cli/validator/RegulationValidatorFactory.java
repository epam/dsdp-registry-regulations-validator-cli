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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator;

import com.deliveredtechnologies.rulebook.model.RuleBook;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.BpAuthConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.ExtIntegrationInboundConfiguration;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.RegulationConfigurationLoader;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.assets.AssetsDirectoryValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpgrouping.BpGroupingProcessDefinitionIdValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpgrouping.BpGroupingUniqueNameValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn.BpAuthToBpmnProcessExistenceValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn.BpAuthToBpmnRoleExistenceValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn.InboundIntegrationToBpmnProcessExistenceValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn.BpmnFileGroupUniqueProcessIdValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn.BpmnFileInputsValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn.BpmnFileValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn.CitizenSignTaskToBpAuthValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.channel.NotificationTemplateDirectoryValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.channel.NotificationTemplateValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.datasettings.DatafactorySettingsYamlRulesValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.dmn.DmnFileValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.excerpt.ExcerptTemplateUniqueNameValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file.EmptyFileValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file.FileExistenceValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file.FileExtensionValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file.FileGroupValidatorLoggingDecorator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file.FileValidatorLoggingDecorator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file.GlobalFileValidatorLoggingDecorator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file.IsNotDirectoryFileValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file.ValidationSkipOnDependentDecorator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.form.FormCardRoleExistenceValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.form.FormToSearchConditionExistenceValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.json.JsonSchemaFileValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.mainliquibase.MainLiquibaseRulesValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.registrysettings.RegistrySettingsFileValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.report.ReportFolderStructureValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.report.ReportGroupUniqueNameValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.report.ReportRoleExistenceValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.role.RoleUniqueNameValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.typed.BpAuthProcessUniquenessValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.typed.ExtIntegrationsInboundProcessUniquenessValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.var.GlobalVarsFileValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;

public class RegulationValidatorFactory {

  private static final String BP_AUTH_JSON_SCHEMA = "classpath:schema/bp-auth-schema.json";

  private static final String BP_GROUPING_SCHEMA = "classpath:schema/bp-grouping-schema.json";
  private static final String EXT_INTEGRATIONS_INBOUND_JSON_SCHEMA = "classpath:schema/ext-integrations-inbound-schema.json";
  private static final String EXT_INTEGRATIONS_OUTBOUND_JSON_SCHEMA = "classpath:schema/ext-integrations-outbound-schema.json";
  private static final String ROLES_JSON_SCHEMA = "classpath:schema/roles-schema.json";
  private static final String GLOBAL_VARS_JSON_SCHEMA = "classpath:schema/global-vars-schema.json";
  private static final String FORMS_JSON_SCHEMA = "classpath:schema/forms-schema.json";
  private static final String REGISTRY_SETTINGS_JSON_SCHEMA = "classpath:schema/registry-settings-schema.json";
  private static final String EMAIL_NOTIFICATION_ARGUMENTS_JSON_SCHEMA = "classpath:schema/email-notification-arguments-schema.json";
  private static final String INBOX_NOTIFICATION_ARGUMENTS_JSON_SCHEMA = "classpath:schema/inbox-notification-arguments-schema.json";
  private static final String DIIA_NOTIFICATION_ARGUMENTS_JSON_SCHEMA = "classpath:schema/diia-notification-arguments-schema.json";
  private static final String MOCK_INTEGRATIONS_JSON_SCHEMA = "classpath:schema/mock-integrations-schema.json";

  @Value("${element-template-path}")
  private String elementTemplatePath;

  @Value("${officer-permissions-file}")
  private String officerPermissionsFile;

  @Value("${default-roles}")
  private List<String> defaultRoles;

  private final ResourceLoader resourceLoader;
  private final ObjectMapper yamlObjectMapper;
  private final ObjectMapper jsonObjectMapper;
  private final RuleBook<Set<ValidationError>> settingsYamlRuleBook;
  private final RuleBook<Set<ValidationError>> mainLiquibaseRuleBook;

  public RegulationValidatorFactory(
      ResourceLoader resourceLoader,
      ObjectMapper yamlObjectMapper,
      ObjectMapper jsonObjectMapper,
      RuleBook<Set<ValidationError>> settingsYamlRuleBook,
      RuleBook<Set<ValidationError>> mainLiquibaseRuleBook) {
    this.resourceLoader = resourceLoader;
    this.yamlObjectMapper = yamlObjectMapper;
    this.jsonObjectMapper = jsonObjectMapper;
    this.settingsYamlRuleBook = settingsYamlRuleBook;
    this.mainLiquibaseRuleBook = mainLiquibaseRuleBook;
  }

  public RegulationValidator<RegulationFiles> newRegulationFilesValidator() {
    var regulationTypeValidators = regulationTypeValidators();
    var groupRegulationTypeValidators = regulationTypeGroupValidators();
    var globalRegulationTypeValidators = globalRegulationTypeValidators(yamlObjectMapper);
    return new RegulationFilesValidator(regulationTypeValidators, groupRegulationTypeValidators,
        globalRegulationTypeValidators);
  }

  private Map<RegulationFileType, RegulationValidator<File>> regulationTypeValidators() {
    Map<RegulationFileType, RegulationValidator<File>> validators = new EnumMap<>(
        RegulationFileType.class);
    validators.put(RegulationFileType.BP_AUTH, newBpAuthFileValidator());
    validators.put(RegulationFileType.EXT_INTEGRATIONS_INBOUND, newExtIntegrationInboundFileValidator());
    validators.put(RegulationFileType.EXT_INTEGRATIONS_OUTBOUND, newExtIntegrationOutboundFileValidator());
    validators.put(RegulationFileType.ROLES, newRolesFileValidator());
    validators.put(RegulationFileType.GLOBAL_VARS, newGlobalVarsFileValidator());
    validators.put(RegulationFileType.FORMS, newFormsFileValidator());
    validators.put(RegulationFileType.BPMN, newBpmnFileValidator());
    validators.put(RegulationFileType.DMN, newDmnFileValidator());
    validators.put(RegulationFileType.DATAFACTORY_SETTINGS, newDataFactorySettingsFileValidator());
    validators.put(RegulationFileType.REGISTRY_SETTINGS, newRegistrySettingsFileValidator());
    validators.put(RegulationFileType.LIQUIBASE, newMainLiquibaseFileValidator());
    validators.put(RegulationFileType.EMAIL_NOTIFICATION_TEMPLATE,
        newEmailNotificationTemplateValidator());
    validators.put(RegulationFileType.INBOX_NOTIFICATION_TEMPLATE,
        newInboxNotificationTemplateValidator());
    validators.put(RegulationFileType.DIIA_NOTIFICATION_TEMPLATE,
        newDiiaNotificationTemplateValidator());
    validators.put(RegulationFileType.BP_GROUPING, newBpGroupValidator());
    validators.put(RegulationFileType.MOCK_INTEGRATIONS, newMockIntegrationsFileValidator());
    validators.put(RegulationFileType.REPORTS, newReportsFileValidator());
    validators.put(RegulationFileType.ASSETS, newAssetsFileValidator());
    return validators;
  }

  private Map<RegulationFileType, RegulationValidator<Collection<File>>> regulationTypeGroupValidators() {
    return Map.of(
        RegulationFileType.BPMN, newBpmnFileGroupValidator(),
        RegulationFileType.EXCERPTS, newExcerptGroupValidator(),
        RegulationFileType.REPORTS, newReportFileGroupValidator()
    );
  }

  private Map<RegulationFileType, RegulationValidator<RegulationFiles>> globalRegulationTypeValidators(
      ObjectMapper yamlObjectMapper) {
    return Map.of(
        RegulationFileType.BP_AUTH_TO_BPMN,
        newBpAuthToProcessDefinitionIdsValidator(yamlObjectMapper),
        RegulationFileType.INBOUND_INTEGRATION_TO_BPMN,
        newExtInboundIntegrationToBpmnProcessDefinitionIdsValidator(yamlObjectMapper),
        RegulationFileType.BP_GROUPING_TO_BPMN,
        newBpGroupingToBpmnProcessDefinitionIdsValidator(yamlObjectMapper),
        RegulationFileType.BPMN,
        newBpmnFileInputsValidator(elementTemplatePath, defaultRoles),
        RegulationFileType.BP_ROLE_EXISTENCE,
        newBpAuthToBpmnRoleExistenceValidator(),
        RegulationFileType.REPORT_FOLDER,
        newReportFolderValidator(),
        RegulationFileType.FORM_TO_SC,
        newFormToSearchConditionExistenceValidator(),
        RegulationFileType.CITIZEN_SIGN_TASK_TO_BP_ROLES,
        newCitizenSignTaskValidator(),
        RegulationFileType.FORM_CARD_ROLE_EXISTENCE,
        newFormCardRoleExistenceValidator()
    );
  }

  private RegulationValidator<RegulationFiles> newBpAuthToProcessDefinitionIdsValidator(
      ObjectMapper yamlObjectMapper) {
    return decorateGlobalValidator(GlobalCompositeRegulationFilesValidator.builder()
        .validator(new BpAuthToBpmnProcessExistenceValidator(yamlObjectMapper))
        .build());
  }

  private RegulationValidator<RegulationFiles> newBpmnFileInputsValidator(
      String elementTemplatePath, List<String> defaultRoles) {
    return decorateGlobalValidator(GlobalCompositeRegulationFilesValidator.builder()
        .validator(new BpmnFileInputsValidator(elementTemplatePath, defaultRoles))
        .build());
  }

  private RegulationValidator<RegulationFiles> newExtInboundIntegrationToBpmnProcessDefinitionIdsValidator(
      ObjectMapper yamlObjectMapper) {
    return decorateGlobalValidator(GlobalCompositeRegulationFilesValidator.builder()
        .validator(new InboundIntegrationToBpmnProcessExistenceValidator(yamlObjectMapper))
        .build());
  }

  private RegulationValidator<RegulationFiles> newBpGroupingToBpmnProcessDefinitionIdsValidator(
      ObjectMapper yamlObjectMapper) {
    return decorateGlobalValidator(GlobalCompositeRegulationFilesValidator.builder()
        .validator(new BpGroupingProcessDefinitionIdValidator(yamlObjectMapper))
        .build());
  }

  private RegulationValidator<Collection<File>> newBpmnFileGroupValidator() {
    return decorateGroupValidator(
        CompositeFileGroupValidator.builder()
            .validator(new BpmnFileGroupUniqueProcessIdValidator())
            .build()
    );
  }

  private RegulationValidator<Collection<File>> newExcerptGroupValidator() {
    return decorateGroupValidator(
        CompositeFileGroupValidator.builder()
            .validator(new ExcerptTemplateUniqueNameValidator())
            .build()
    );
  }

  private RegulationValidator<Collection<File>> newReportFileGroupValidator() {
    return decorateGroupValidator(
        CompositeFileGroupValidator.builder()
            .validator(new ReportGroupUniqueNameValidator(jsonObjectMapper))
            .build());
  }

  private RegulationValidator<File> newBpAuthFileValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(new EmptyFileValidator())
            .validator(
                new JsonSchemaFileValidator(BP_AUTH_JSON_SCHEMA, resourceLoader, yamlObjectMapper))
            .validator(
                TypedConfigurationValidator.<BpAuthConfiguration>builder()
                    .configurationClass(BpAuthConfiguration.class)
                    .configurationLoader(new RegulationConfigurationLoader(yamlObjectMapper))
                    .validator(new BpAuthProcessUniquenessValidator())
                    .build())
            .build()
    );
  }

  private RegulationValidator<File> newExtIntegrationInboundFileValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(new EmptyFileValidator())
            .validator(new JsonSchemaFileValidator(EXT_INTEGRATIONS_INBOUND_JSON_SCHEMA, resourceLoader,
                yamlObjectMapper))
            .validator(
                TypedConfigurationValidator.<ExtIntegrationInboundConfiguration>builder()
                    .configurationClass(ExtIntegrationInboundConfiguration.class)
                    .configurationLoader(new RegulationConfigurationLoader(yamlObjectMapper))
                    .validator(new ExtIntegrationsInboundProcessUniquenessValidator())
                    .build())
            .build()
    );
  }

  private RegulationValidator<File> newExtIntegrationOutboundFileValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(new EmptyFileValidator())
            .validator(new JsonSchemaFileValidator(EXT_INTEGRATIONS_OUTBOUND_JSON_SCHEMA, resourceLoader,
                yamlObjectMapper))
            .build()
    );
  }

  private RegulationValidator<File> newRolesFileValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(new EmptyFileValidator())
            .validator(
                new JsonSchemaFileValidator(ROLES_JSON_SCHEMA, resourceLoader, yamlObjectMapper))
            .validator(new RoleUniqueNameValidator(yamlObjectMapper))
            .build()
    );
  }

  private RegulationValidator<File> newGlobalVarsFileValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(new EmptyFileValidator())
            .validator(new GlobalVarsFileValidator(GLOBAL_VARS_JSON_SCHEMA, resourceLoader,
                yamlObjectMapper))
            .build()
    );
  }

  private RegulationValidator<File> newFormsFileValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(new EmptyFileValidator())
            .validator(new FormsFileValidator(FORMS_JSON_SCHEMA, resourceLoader, jsonObjectMapper))
            .build()
    );
  }

  private RegulationValidator<File> newBpmnFileValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(new BpmnFileValidator())
            .build()
    );
  }

  private RegulationValidator<File> newDmnFileValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(new DmnFileValidator())
            .build()
    );
  }

  private RegulationValidator<File> newEmailNotificationTemplateValidator() {
    var notificationArgumentsValidator =
        ValidationSkipOnDependentDecorator.builder()
            .skippingValidator(new FileExistenceValidator())
            .validator(
                CompositeFileValidator.builder()
                    .validator(new IsNotDirectoryFileValidator())
                    .validator(new EmptyFileValidator())
                    .validator(
                        new JsonSchemaFileValidator(
                            EMAIL_NOTIFICATION_ARGUMENTS_JSON_SCHEMA,
                            resourceLoader,
                            yamlObjectMapper))
                    .build())
            .build();
    return newNotificationTemplateValidator("notification.ftlh", notificationArgumentsValidator);
  }

  private RegulationValidator<File> newInboxNotificationTemplateValidator() {
    var notificationArgumentsValidator =
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new IsNotDirectoryFileValidator())
            .validator(new EmptyFileValidator())
            .validator(
                new JsonSchemaFileValidator(
                    INBOX_NOTIFICATION_ARGUMENTS_JSON_SCHEMA, resourceLoader, yamlObjectMapper))
            .build();
    return newNotificationTemplateValidator("notification.ftl", notificationArgumentsValidator);
  }

  private RegulationValidator<File> newDiiaNotificationTemplateValidator() {
    var notificationArgumentsValidator =
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new IsNotDirectoryFileValidator())
            .validator(new EmptyFileValidator())
            .validator(
                new JsonSchemaFileValidator(
                    DIIA_NOTIFICATION_ARGUMENTS_JSON_SCHEMA, resourceLoader, yamlObjectMapper))
            .build();
    return newNotificationTemplateValidator("notification.diia", notificationArgumentsValidator);
  }

  private RegulationValidator<File> newNotificationTemplateValidator(
      String notificationTemplateFileName,
      RegulationValidator<File> notificationArgumentsValidator) {
    var templateFileValidator =
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new IsNotDirectoryFileValidator())
            .validator(new EmptyFileValidator())
            .build();
    var notificationTemplateValidator =
        new NotificationTemplateValidator(
            notificationTemplateFileName, templateFileValidator, notificationArgumentsValidator);
    var notificationTemplateDirectoryValidator =
        new NotificationTemplateDirectoryValidator(
            CompositeFileValidator.builder()
                .validator(new FileExistenceValidator())
                .validator(notificationTemplateValidator)
                .build());
    return decorate(notificationTemplateDirectoryValidator);
  }

  private RegulationValidator<File> newMockIntegrationsFileValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(new EmptyFileValidator())
            .validator(
                new JsonSchemaFileValidator(
                    MOCK_INTEGRATIONS_JSON_SCHEMA, resourceLoader, jsonObjectMapper))
            .build());
  }

  private RegulationValidator<File> newReportsFileValidator() {
    return decorate(
            CompositeFileValidator.builder()
                    .validator(new FileExistenceValidator())
                    .validator(new FileExtensionValidator())
                    .validator(new EmptyFileValidator())
                    .build());
  }

  private RegulationValidator<File> newAssetsFileValidator() {
    var assetsArgumentsValidator =
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new EmptyFileValidator())
            .validator(new FileExtensionValidator())
            .build();
    return decorate(new AssetsDirectoryValidator(assetsArgumentsValidator));
  }

  private RegulationValidator<File> decorate(RegulationValidator<File> validator) {
    return FileValidatorLoggingDecorator.wrap(validator);
  }

  private RegulationValidator<Collection<File>> decorateGroupValidator(
      RegulationValidator<Collection<File>> validator) {
    return FileGroupValidatorLoggingDecorator.wrap(validator);
  }

  private RegulationValidator<RegulationFiles> decorateGlobalValidator(
      RegulationValidator<RegulationFiles> validator) {
    return GlobalFileValidatorLoggingDecorator.wrap(validator);
  }

  private RegulationValidator<File> newDataFactorySettingsFileValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(
                new DatafactorySettingsYamlRulesValidator(yamlObjectMapper, settingsYamlRuleBook))
            .build()
    );
  }

  private RegulationValidator<File> newRegistrySettingsFileValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(new EmptyFileValidator())
            .validator(
                new RegistrySettingsFileValidator(
                    REGISTRY_SETTINGS_JSON_SCHEMA, resourceLoader, yamlObjectMapper))
            .build());
  }

  private RegulationValidator<File> newMainLiquibaseFileValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(new MainLiquibaseRulesValidator(mainLiquibaseRuleBook))
            .build()
    );
  }

  private RegulationValidator<File> newBpGroupValidator() {
    return decorate(
        CompositeFileValidator.builder()
            .validator(new FileExistenceValidator())
            .validator(new FileExtensionValidator())
            .validator(new EmptyFileValidator())
            .validator(
                new JsonSchemaFileValidator(BP_GROUPING_SCHEMA, resourceLoader, yamlObjectMapper))
            .validator(new BpGroupingUniqueNameValidator(yamlObjectMapper))
            .build()
    );
  }

  private RegulationValidator<RegulationFiles> newBpAuthToBpmnRoleExistenceValidator() {
    return decorateGlobalValidator(GlobalCompositeRegulationFilesValidator.builder()
        .validator(new BpAuthToBpmnRoleExistenceValidator(yamlObjectMapper, defaultRoles))
        .build());
  }

  private RegulationValidator<RegulationFiles> newReportFolderValidator() {
    return decorateGlobalValidator(
        GlobalCompositeRegulationFilesValidator.builder()
            .validator(new ReportFolderStructureValidator())
            .validator(
                new ReportRoleExistenceValidator(
                    yamlObjectMapper, officerPermissionsFile, defaultRoles))
            .build());
  }

  private RegulationValidator<RegulationFiles> newFormToSearchConditionExistenceValidator() {
    return decorateGlobalValidator(GlobalCompositeRegulationFilesValidator.builder()
        .validator(new FormToSearchConditionExistenceValidator(jsonObjectMapper))
        .build());
  }

  private RegulationValidator<RegulationFiles> newCitizenSignTaskValidator() {
    return decorateGlobalValidator(GlobalCompositeRegulationFilesValidator.builder()
        .validator(new CitizenSignTaskToBpAuthValidator(yamlObjectMapper, defaultRoles))
        .build());
  }

  private RegulationValidator<RegulationFiles> newFormCardRoleExistenceValidator() {
    return decorateGlobalValidator(GlobalCompositeRegulationFilesValidator.builder()
            .validator(new FormCardRoleExistenceValidator(yamlObjectMapper, jsonObjectMapper, defaultRoles))
            .build());
  }
}