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

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.google.common.collect.Sets;
import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class RegulationFilesValidator implements RegulationValidator<RegulationFiles> {

  private final Map<RegulationFileType, RegulationValidator<File>> regulationTypeValidators;
  private final Map<RegulationFileType, RegulationValidator<Collection<File>>> groupRegulationTypeValidators;
  private final Map<RegulationFileType, RegulationValidator<RegulationFiles>> globalRegulationTypeValidators;

  public RegulationFilesValidator(
          Map<RegulationFileType, RegulationValidator<File>> regulationTypeValidators,
          Map<RegulationFileType, RegulationValidator<Collection<File>>> groupRegulationTypeValidators,
          Map<RegulationFileType, RegulationValidator<RegulationFiles>> globalRegulationTypeValidators) {
    this.regulationTypeValidators = Map.copyOf(regulationTypeValidators);
    this.groupRegulationTypeValidators = Map.copyOf(groupRegulationTypeValidators);
    this.globalRegulationTypeValidators = Map.copyOf(globalRegulationTypeValidators);
  }

  @Override
  public Set<ValidationError> validate(RegulationFiles regulationFiles, ValidationContext context) {
    var errors = new LinkedHashSet<ValidationError>();

    regulationFiles.getBpAuthFiles().forEach(file -> errors.addAll(validate(file, RegulationFileType.BP_AUTH)));

    regulationFiles.getExtIntegrationsInboundFiles().forEach(file -> errors.addAll(validate(file, RegulationFileType.EXT_INTEGRATIONS_INBOUND)));

    regulationFiles.getExtIntegrationsOutboundFiles().forEach(file -> errors.addAll(validate(file, RegulationFileType.EXT_INTEGRATIONS_OUTBOUND)));

    regulationFiles.getGlobalVarsFiles().forEach(file -> errors.addAll(validate(file, RegulationFileType.GLOBAL_VARS)));

    regulationFiles.getRolesFiles().forEach(file -> errors.addAll(validate(file, RegulationFileType.ROLES)));

    errors.addAll(validateBpmnFiles(regulationFiles.getBpmnFiles()));

    errors.addAll(validateReportsFiles(regulationFiles.getReportsFiles()));

    regulationFiles.getAssetsDirectory().forEach(file -> errors.addAll(validate(file, RegulationFileType.ASSETS)));

    regulationFiles.getDmnFiles().forEach(file -> errors.addAll(validate(file, RegulationFileType.DMN)));

    regulationFiles.getFormFiles().forEach(file -> errors.addAll(validate(file, RegulationFileType.FORMS)));

    regulationFiles.getDatafactorySettingsFiles().forEach(file -> errors.addAll(validate(file, RegulationFileType.DATAFACTORY_SETTINGS)));

    regulationFiles.getRegistrySettingsFiles().forEach(file -> errors.addAll(validate(file, RegulationFileType.REGISTRY_SETTINGS)));

    regulationFiles.getLiquibaseFiles().forEach(file -> errors.addAll(validate(file, RegulationFileType.LIQUIBASE)));

    regulationFiles.getMockIntegrationFiles().forEach(file -> errors.addAll(validate(file, RegulationFileType.MOCK_INTEGRATIONS)));

    regulationFiles.getEmailNotificationTemplateDirectory().forEach(file -> errors.addAll(validate(file, RegulationFileType.EMAIL_NOTIFICATION_TEMPLATE)));
    regulationFiles.getInboxNotificationTemplateDirectory().forEach(file -> errors.addAll(validate(file, RegulationFileType.INBOX_NOTIFICATION_TEMPLATE)));
    regulationFiles.getDiiaNotificationTemplateDirectory().forEach(file -> errors.addAll(validate(file, RegulationFileType.DIIA_NOTIFICATION_TEMPLATE)));
    
    errors.addAll(validateExcerptFiles(regulationFiles.getExcerptFiles()));

    errors.addAll(validateGlobalFiles(regulationFiles, RegulationFileType.BP_AUTH_TO_BPMN));

    errors.addAll(validateGlobalFiles(regulationFiles, RegulationFileType.INBOUND_INTEGRATION_TO_BPMN));

    errors.addAll(validateGlobalFiles(regulationFiles, RegulationFileType.BP_GROUPING_TO_BPMN));

    errors.addAll(validateGlobalFiles(regulationFiles, RegulationFileType.BPMN));

    errors.addAll(validateGlobalFiles(regulationFiles, RegulationFileType.BP_ROLE_EXISTENCE));

    errors.addAll(validateGlobalFiles(regulationFiles, RegulationFileType.REPORT_FOLDER));

    errors.addAll(validateGlobalFiles(regulationFiles, RegulationFileType.FORM_TO_SC));

    errors.addAll(validateGlobalFiles(regulationFiles, RegulationFileType.CITIZEN_SIGN_TASK_TO_BP_ROLES));

    errors.addAll(validateGlobalFiles(regulationFiles, RegulationFileType.FORM_CARD_ROLE_EXISTENCE));

    regulationFiles.getBpGroupingFiles().forEach(file -> errors.addAll(validate(file, RegulationFileType.BP_GROUPING)));

    return errors;
  }

  private Collection<ValidationError> validateGlobalFiles(RegulationFiles regulationFiles, RegulationFileType regulationFileType) {
    return globalRegulationTypeValidators.get(regulationFileType).validate(regulationFiles, ValidationContext.of(regulationFileType));
  }

  private Set<ValidationError> validate(File file, RegulationFileType regulationFileType) {
    var validator = this.regulationTypeValidators.get(regulationFileType);
    return validator.validate(file, ValidationContext.of(regulationFileType));
  }

  private Set<ValidationError> validateBpmnFiles(Collection<File> bpmnFiles) {
    Set<ValidationError> errors = Sets.newHashSet();
    bpmnFiles.forEach(file -> errors.addAll(validate(file, RegulationFileType.BPMN)));

    if (errors.isEmpty()) {
      var groupValidator = groupRegulationTypeValidators.get(RegulationFileType.BPMN);
      errors.addAll(groupValidator.validate(bpmnFiles, ValidationContext.of(RegulationFileType.BPMN)));
    }
    return errors;
  }

  private Set<ValidationError> validateExcerptFiles(Collection<File> excerptFolders) {
    Set<ValidationError> errors = Sets.newHashSet();
    var groupValidator = groupRegulationTypeValidators.get(RegulationFileType.EXCERPTS);
    errors.addAll(groupValidator.validate(excerptFolders, ValidationContext.of(RegulationFileType.EXCERPTS)));
    return errors;
  }

  private Set<ValidationError> validateReportsFiles(Collection<File> reportsFiles) {
    Set<ValidationError> errors = Sets.newHashSet();
    reportsFiles.forEach(file -> errors.addAll(validate(file, RegulationFileType.REPORTS)));

    var groupValidator = groupRegulationTypeValidators.get(RegulationFileType.REPORTS);
    errors.addAll(groupValidator.validate(reportsFiles, ValidationContext.of(RegulationFileType.REPORTS)));

    return errors;
  }
}
