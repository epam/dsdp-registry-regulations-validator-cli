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

package com.epam.digital.data.platform.registry.regulation.validation.cli.model;

import java.util.Arrays;

public enum RegulationFileType {

  GLOBAL_VARS("yml", "yaml"),
  BP_AUTH("yml", "yaml"),
  EXT_INTEGRATIONS_INBOUND("yml", "yaml"),
  EXT_INTEGRATIONS_OUTBOUND("yml", "yaml"),
  ROLES("yml", "yaml"),
  BPMN("bpmn"),
  DMN("dmn"),
  FORMS("json"),
  DATAFACTORY_SETTINGS("yml", "yaml"),
  REGISTRY_SETTINGS("yml", "yaml"),
  LIQUIBASE("xml"),
  INBOUND_INTEGRATION_TO_BPMN("yml", "yaml", "bpmn"),
  BP_AUTH_TO_BPMN("yml", "yaml", "bpmn"),
  EXCERPTS(),
  EMAIL_NOTIFICATION_TEMPLATE(),
  INBOX_NOTIFICATION_TEMPLATE(),
  DIIA_NOTIFICATION_TEMPLATE(),
  BP_GROUPING("yml", "yaml"),
  BP_GROUPING_TO_BPMN(),
  MOCK_INTEGRATIONS("json"),
  BP_ROLE_EXISTENCE(),
  REPORT_FOLDER(),
  FORM_TO_SC("json", "xml"),
  REPORTS("json"),
  CITIZEN_SIGN_TASK_TO_BP_ROLES("yml", "yaml", "bpmn"),
  ASSETS("svg", "png"),
  FORM_CARD_ROLE_EXISTENCE();

  private final String[] fileExtensions;

  RegulationFileType(String... fileExtensions) {
    this.fileExtensions = fileExtensions;
  }

  public String[] getFileExtensions() {
    return fileExtensions.clone();
  }

  public boolean isExtensionSupported(String fileExtension) {
    return Arrays.asList(this.fileExtensions).contains(fileExtension);
  }
}
