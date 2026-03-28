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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

@Getter
@Builder
public class RegulationFiles {

  @Default
  private Collection<File> extIntegrationsInboundFiles = new ArrayList<>();
  @Default
  private Collection<File> extIntegrationsOutboundFiles = new ArrayList<>();
  @Default
  private Collection<File> globalVarsFiles = new ArrayList<>();
  @Default
  private Collection<File> bpAuthFiles = new ArrayList<>();
  @Default
  private Collection<File> rolesFiles = new ArrayList<>();
  @Default
  private Collection<File> bpmnFiles = new ArrayList<>();
  @Default
  private Collection<File> dmnFiles = new ArrayList<>();
  @Default
  private Collection<File> formFiles = new ArrayList<>();
  @Default
  private Collection<File> datafactorySettingsFiles = new ArrayList<>();
  @Default
  private Collection<File> registrySettingsFiles = new ArrayList<>();
  @Default
  private Collection<File> liquibaseFiles = new ArrayList<>();
  @Default
  private Collection<File> excerptFiles = new ArrayList<>();
  @Default
  private Collection<File> emailNotificationTemplateDirectory = new ArrayList<>();
  @Default
  private Collection<File> inboxNotificationTemplateDirectory = new ArrayList<>();
  @Default
  private Collection<File> diiaNotificationTemplateDirectory = new ArrayList<>();
  @Default
  private Collection<File> bpGroupingFiles = new ArrayList<>();
  @Default
  private Collection<File> mockIntegrationFiles = new ArrayList<>();
  @Default
  private Collection<File> reportsFolders = new ArrayList<>();
  @Default
  private Collection<File> files = new ArrayList<>();
  @Default
  private Collection<File> filesDetailed = new ArrayList<>();
  @Default
  private Collection<File> reportsFiles = new ArrayList<>();
  @Default
  private Collection<File> assetsDirectory = new ArrayList<>();

  public boolean isEmpty() {
    return Stream.of(
      extIntegrationsInboundFiles,
      extIntegrationsOutboundFiles,
      globalVarsFiles,
      bpAuthFiles,
      rolesFiles,
      bpmnFiles,
      dmnFiles,
      formFiles,
      datafactorySettingsFiles,
      registrySettingsFiles,
      liquibaseFiles,
      excerptFiles,
      emailNotificationTemplateDirectory,
      inboxNotificationTemplateDirectory,
      diiaNotificationTemplateDirectory,
      bpGroupingFiles,
      mockIntegrationFiles,
      reportsFolders,
      reportsFiles,
      assetsDirectory
    ).allMatch(Collection::isEmpty);
  }
}
