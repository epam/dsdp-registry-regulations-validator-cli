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

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

public class CommandLineOptionsConverter {

  public RegulationFiles convert(Options options) {
    return RegulationFiles.builder()
        .globalVarsFiles(getFilesIfAny(CommandLineArg.GLOBAL_VARS, options))
        .bpAuthFiles(getFilesIfAny(CommandLineArg.BP_AUTH, options))
        .extIntegrationsInboundFiles(getFilesIfAny(CommandLineArg.EXT_INTEGRATIONS_INBOUND, options))
        .extIntegrationsOutboundFiles(getFilesIfAny(CommandLineArg.EXT_INTEGRATIONS_OUTBOUND, options))
        .rolesFiles(getFilesIfAny(CommandLineArg.ROLES, options))
        .bpmnFiles(getFilesIfAny(CommandLineArg.BPMN, options))
        .dmnFiles(getFilesIfAny(CommandLineArg.DMN, options))
        .formFiles(getFilesIfAny(CommandLineArg.FORMS, options))
        .datafactorySettingsFiles(getFilesIfAny(CommandLineArg.DATAFACTORY_SETTINGS, options))
        .registrySettingsFiles(getFilesIfAny(CommandLineArg.REGISTRY_SETTINGS, options))
        .liquibaseFiles(getFilesIfAny(CommandLineArg.LIQUIBASE, options))
        .excerptFiles(getFilesIfAny(CommandLineArg.EXCERPTS, options))
        .emailNotificationTemplateDirectory(getFilesIfAny(CommandLineArg.EMAIL_NOTIFICATION_TEMPLATE, options))
        .inboxNotificationTemplateDirectory(getFilesIfAny(CommandLineArg.INBOX_NOTIFICATION_TEMPLATE, options))
        .diiaNotificationTemplateDirectory(getFilesIfAny(CommandLineArg.DIIA_NOTIFICATION_TEMPLATE, options))
        .bpGroupingFiles(getFilesIfAny(CommandLineArg.BP_GROUPING, options))
        .mockIntegrationFiles(getFilesIfAny(CommandLineArg.MOCK_INTEGRATIONS, options))
        .reportsFolders(getFilesIfAny(CommandLineArg.REPORTS_FOLDERS, options))
        .files(getFilesIfAny(CommandLineArg.FILES, options))
        .filesDetailed(getFilesIfAny(CommandLineArg.FILES_DETAILED, options))
        .reportsFiles(getFilesIfAny(CommandLineArg.REPORTS, options))
        .assetsDirectory(getFilesIfAny(CommandLineArg.ASSETS, options))
        .build();
  }

  private Collection<File> getFilesIfAny(CommandLineArg regulationTypeArgOption, Options options) {
    if (options.hasOption(regulationTypeArgOption.getArgOptionName())) {
      var option = options.getOption(regulationTypeArgOption.getArgOptionName());
      var optionValues = option.getValues();
      return Stream.of(optionValues).filter(StringUtils::isNotBlank).map(File::new).collect(Collectors.toSet());
    }
    return Collections.emptySet();
  }
}
