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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn;

import com.epam.digital.data.platform.liquibase.extension.change.core.DdmCreateCompositeEntityChange;
import com.epam.digital.data.platform.liquibase.extension.change.core.DdmCreateSearchConditionChange;
import com.epam.digital.data.platform.liquibase.extension.change.core.DdmCreateTableChange;
import com.epam.digital.data.platform.liquibase.extension.change.core.DdmDropSearchConditionChange;
import com.epam.digital.data.platform.liquibase.extension.change.core.DdmPartialUpdateChange;
import com.epam.digital.data.platform.registry.regulation.validation.cli.exception.FileProcessingException;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.ExtIntegrationOutboundConfiguration.ExternalSystem;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.ElementTemplate;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.ElementTemplate.Property;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.bpmn.util.BpmnUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import java.util.stream.Stream;
import liquibase.change.Change;
import liquibase.exception.LiquibaseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOut;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.ModelParseException;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.digital.data.platform.registry.regulation.validation.cli.validator.mainliquibase.util.MainLiquibaseUtil.getAllChanges;
import static com.epam.digital.data.platform.registry.regulation.validation.cli.validator.mainliquibase.util.MainLiquibaseUtil.getDatabaseChangeLog;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

@Slf4j
public class BpmnFileInputsValidator implements RegulationValidator<RegulationFiles> {

  private static final Map<String, BiFunction<Activity, Property, Set<String>>> GET_VALUES_FOR_VALIDATION_FUNCTIONS = Map.of(
      "property", BpmnFileInputsValidator::getAttributeValuesFromActivity,
      "camunda:property", BpmnFileInputsValidator::getPropertyValueFromActivity,
      "camunda:in", BpmnFileInputsValidator::getInValueFromActivity,
      "camunda:out", BpmnFileInputsValidator::getOutValueFromActivity,
      "camunda:inputParameter", BpmnFileInputsValidator::getInputParameterValueFromActivity,
      "camunda:outputParameter", BpmnFileInputsValidator::getOutputParameterValueFromActivity
  );

  private Set<String> tableNames;
  private Set<String> compositeEntityNames;
  private Set<String> partialUpdateEntityNames;
  private Map<String, ExternalSystem> externalSystems;
  private Set<String> externalSystemNames;
  private Set<String> externalSystemOperationNames;
  private Set<String> searchConditionNames;
  private Set<String> existingFormFileNames;

  private final Map<String, Function<String, Boolean>> INPUT_VALIDATION_FUNCTIONS = Map.ofEntries(
      Map.entry("role.name", (role) -> this.allRoles.contains(role)),
      Map.entry("table.rest-api-name", (table) -> tableNames.contains(table)),
      Map.entry("composite-entity.rest-api-name", (compositeEntity) -> compositeEntityNames.contains(compositeEntity)),
      Map.entry("partial-update.rest-api-name", (partialUpdate) -> partialUpdateEntityNames.contains(partialUpdate)),
      Map.entry("external-system.name", (externalSystemName) -> externalSystemNames.contains(externalSystemName)),
      Map.entry("external-system.operation.name", (externalSystemOperationName) -> externalSystemOperationNames.contains(externalSystemOperationName)),
      Map.entry("search-condition.rest-api-name", (scName) -> searchConditionNames.contains(scName)),
      Map.entry("excerpt.name", (excerptName) -> this.excerptNames.contains(excerptName)),
      Map.entry("notification.template.name", (notificationName) -> this.notificationNames.contains(notificationName)),
      Map.entry("process.id", (processId) -> this.processIds.contains(processId)),
      Map.entry("form.name", (formName) -> existingFormFileNames.contains(formName))
  );

  private final Map<String, ElementTemplate> elementTemplates;
  private final List<String> defaultRoles;
  private Set<String> processIds;
  private Set<String> allRoles;
  private Set<String> excerptNames;
  private Set<String> notificationNames;

  public BpmnFileInputsValidator(String elementTemplatePath, List<String> defaultRoles) {
    this.defaultRoles = List.copyOf(Objects.requireNonNullElse(defaultRoles, List.of()));
    var jsonFile = new File(elementTemplatePath);

    var objectMapper = new Jackson2ObjectMapperBuilder()
        .failOnUnknownProperties(false)
        .build();

    try {
      elementTemplates = objectMapper.readValue(jsonFile, new ElementTemplateListTypeReference())
          .stream()
          .collect(Collectors.toMap(ElementTemplate::getId, Function.identity()));
    } catch (IOException e) {
      throw new IllegalStateException("During reading elementTemplates file occurred error.", e);
    }
  }

  @Override
  public Set<ValidationError> validate(RegulationFiles regulationFiles, ValidationContext context) {
    Set<ValidationError> errors = Sets.newHashSet();
    init(regulationFiles, context, errors);
    for (File bpmn : regulationFiles.getBpmnFiles()) {
      try {
        errors.addAll(
            validateElementTemplateParameters(loadProcessModel(bpmn), bpmn, context)
        );
      } catch (ModelParseException e) {
        log.trace("Error while parsing BPMN file: {}", bpmn.getName(), e);
        errors.add(
            ValidationError.of(context.getRegulationFileType(), bpmn,
                String.format("Exception during parsing file %s", bpmn.getName())));
      }
    }
    return errors;
  }

  private void init(RegulationFiles regulationFiles, ValidationContext context, Set<ValidationError> errors) {
    try {
      processIds = BpmnUtil.getBpmnFilesProcessDefinitionsId(regulationFiles);
    } catch (FileProcessingException e) {
      log.trace("Failed to process BPMN files", e);
      errors.add(ValidationError.of(context.getRegulationFileType(), e.getFile(), e.getMessage()));
    }
    allRoles = getAllRoles(regulationFiles);
    notificationNames = getNotificationNames(regulationFiles);
    existingFormFileNames = getAllFormName(regulationFiles);

    try {
      externalSystems = getExternalSystems(regulationFiles);
      externalSystemNames = getExternalSystemNames();
      externalSystemOperationNames = getExternalSystemOperationNames();
      excerptNames = getExcerptNames(regulationFiles);
      var liquibaseFiles = regulationFiles.getLiquibaseFiles();
      if (!liquibaseFiles.isEmpty()) {
        var mainLiquibase = liquibaseFiles.iterator().next();

        var changes = getChanges(mainLiquibase);
        tableNames = getTableNames(changes);
        compositeEntityNames = getCompositeEntityNames(changes);
        partialUpdateEntityNames = getPartialUpdateEntityNames(changes);
        searchConditionNames = getFilteredSearchConditions(changes);
      }
    } catch (FileProcessingException e) {
      log.trace("Failed to process external systems or Liquibase files: {}", e.getMessage(), e);
      errors.add(ValidationError.of(context.getRegulationFileType(), e.getFile(),
          e.getMessage(), e)
      );
    }
  }

  private Set<ValidationError> validateElementTemplateParameters(BpmnModelInstance bpmnModel,
                                                                 File regulationFile, ValidationContext validationContext) {
    var validationErrors = new HashSet<ValidationError>();
    var elements = bpmnModel.getModelElementsByType(Activity.class)
        .stream()
        .filter(element -> Objects.nonNull(
            element.getAttributeValueNs(CAMUNDA_NS, "modelerTemplate")))
        .toList();

    for (var element : elements) {
      var modelerTemplate = element.getAttributeValueNs(CAMUNDA_NS, "modelerTemplate");
      var elementTemplate = elementTemplates.get(modelerTemplate);
      if (Objects.isNull(elementTemplate)) {
        log.warn("No element template with id {} found", modelerTemplate);
        continue;
      }
      validationErrors.addAll(
          validateElementAgainstElementTemplate(element, elementTemplate, regulationFile, validationContext));
    }
    return validationErrors;
  }

  private Set<ValidationError> validateElementAgainstElementTemplate(Activity activity, ElementTemplate elementTemplate,
                                                                     File regulationFile, ValidationContext validationContext) {
    var properties = elementTemplate.getProperties();

    return properties.stream()
        .map(property -> validatePropertyInElement(activity, property, regulationFile,
            validationContext))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  private ValidationError validatePropertyInElement(Activity activity, ElementTemplate.Property property,
                                                    File regulationFile, ValidationContext validationContext) {
    var getValuesFunction = GET_VALUES_FOR_VALIDATION_FUNCTIONS.get(
        property.getBinding().getType());
    var propertyValueSet = getValuesFunction.apply(activity, property);

    if (propertyValueSet.size() > 1) {
      return ValidationError.of(validationContext.getRegulationFileType(), regulationFile,
          String.format("In task %s in process %s there are several values for input parameter %s",
              activity.getId(), regulationFile.getName(), property.getBinding()));
    }

    String propertyValue = CollectionUtils.firstElement(propertyValueSet);

    if (property.getConstraints().isNotEmpty() && StringUtils.isBlank(propertyValue)) {
      return ValidationError.of(validationContext.getRegulationFileType(), regulationFile,
          String.format("In task %s in process %s input parameter %s is empty",
              activity.getId(), regulationFile.getName(), property.getBinding()));
    }

    String type = property.getConstraints().getType();
    if (!StringUtils.isBlank(type) && !StringUtils.isBlank(propertyValue)) {
      if (isExpression(propertyValue)){
        log.warn(String.format("Unable to validate property due to dynamic value: %s", propertyValue));
        return null;
      }
      var inputValidationFunction = INPUT_VALIDATION_FUNCTIONS.get(type);
      try {
        if (Objects.nonNull(inputValidationFunction) && !inputValidationFunction.apply(propertyValue)) {
          return ValidationError.of(validationContext.getRegulationFileType(), regulationFile,
              String.format("In task %s of process %s, the input parameter %s doesn't exist",
                  activity.getId(), regulationFile.getName(), property.getBinding()));
        }
      } catch (FileProcessingException e) {
        log.trace("Failed to validate property: {}", e.getMessage(), e);
        return ValidationError.of(validationContext.getRegulationFileType(), regulationFile, e.getMessage());
      }
    }
    return null;
  }

  private BpmnModelInstance loadProcessModel(File regulationFile) {
    return Bpmn.readModelFromFile(regulationFile);
  }

  @VisibleForTesting
  static Set<String> getAttributeValuesFromActivity(Activity activity, Property property) {
    var attributeName = property.getBinding().getName();
    var namespaceAndName = attributeName.split(":");

    var attributeValue = namespaceAndName.length == 1 ?
        activity.getAttributeValue(attributeName) :
        activity.getAttributeValueNs(CAMUNDA_NS, namespaceAndName[1]);

    return Objects.isNull(attributeValue) ? Set.of() : Set.of(attributeValue);
  }

  @VisibleForTesting
  static Set<String> getPropertyValueFromActivity(Activity activity, Property property) {
    var extensionElements = activity.getExtensionElements();
    if (Objects.isNull(extensionElements)) {
      return Set.of();
    }
    var propertyName = property.getBinding().getName();
    return extensionElements.getElementsQuery()
        .filterByType(CamundaProperties.class)
        .list()
        .stream()
        .flatMap(camundaProperties -> camundaProperties.getCamundaProperties().stream())
        .filter(prop -> prop.getCamundaName().equals(propertyName))
        .map(CamundaProperty::getCamundaValue)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  @VisibleForTesting
  static Set<String> getInValueFromActivity(Activity activity, Property property) {
    var extensionElements = activity.getExtensionElements();
    if (Objects.isNull(extensionElements)) {
      return Set.of();
    }
    var propertyName = property.getBinding().getName();
    return extensionElements.getElementsQuery()
        .filterByType(CamundaIn.class)
        .list()
        .stream()
        .filter(in -> in.getAttributeValue("target").equals(propertyName))
        .map(in -> in.getAttributeValue("sourceExpression"))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  @VisibleForTesting
  static Set<String> getOutValueFromActivity(Activity activity, Property property) {
    var extensionElements = activity.getExtensionElements();
    if (Objects.isNull(extensionElements)) {
      return Set.of();
    }
    var propertyName = property.getBinding().getSource();
    return extensionElements.getElementsQuery()
        .filterByType(CamundaOut.class)
        .list()
        .stream()
        .filter(in -> in.getAttributeValue("source").equals(propertyName))
        .map(in -> in.getAttributeValue("target"))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  @VisibleForTesting
  static Set<String> getInputParameterValueFromActivity(Activity activity, Property property) {
    var extensionElements = activity.getExtensionElements();
    if (Objects.isNull(extensionElements)) {
      return Set.of();
    }
    var propertyName = property.getBinding().getName();
    return extensionElements.getElementsQuery()
        .filterByType(CamundaInputOutput.class)
        .list()
        .stream()
        .flatMap(cio -> cio.getCamundaInputParameters().stream())
        .filter(cip -> cip.getCamundaName().equals(propertyName))
        .map(CamundaInputParameter::getTextContent)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  @VisibleForTesting
  static Set<String> getOutputParameterValueFromActivity(Activity activity, Property property) {
    var extensionElements = activity.getExtensionElements();
    if (Objects.isNull(extensionElements)) {
      return Set.of();
    }
    var propertyName = property.getBinding().getSource();
    return extensionElements.getElementsQuery()
        .filterByType(CamundaInputOutput.class)
        .list()
        .stream()
        .flatMap(cio -> cio.getCamundaOutputParameters().stream())
        .filter(cop -> cop.getTextContent().equals(propertyName))
        .map(CamundaOutputParameter::getCamundaName)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  private Set<String> getAllRoles(RegulationFiles regulationFiles) {
    Set<String> roles = BpmnUtil.getRoles(regulationFiles);
    roles.addAll(Objects.requireNonNullElse(defaultRoles, Collections.emptySet()));
    return roles;
  }

  private Set<String> getAllFormName(RegulationFiles regulationFiles) {
    return regulationFiles.getFormFiles().stream()
        .map(file -> FilenameUtils.removeExtension(file.getName()))
        .collect(Collectors.toSet());
  }

  private Set<String> getTableNames(List<Change> changes) {
    return changes.stream()
        .filter(change -> DdmCreateTableChange.class.isAssignableFrom(change.getClass()))
        .map(change -> ((DdmCreateTableChange) change).getTableName().replaceAll("_", "-"))
        .collect(Collectors.toSet());
  }

  private Set<String> getFilteredSearchConditions(List<Change> changes) {
    Set<String> searchConditionsNames = changes.stream()
        .filter(Objects::nonNull)
        .flatMap(change -> {
          if (change instanceof DdmCreateSearchConditionChange) {
            return Stream.of(((DdmCreateSearchConditionChange) change).getName());
          }
          if (change instanceof DdmDropSearchConditionChange) {
            return Stream.of(((DdmDropSearchConditionChange) change).getName());
          } else {
            return Stream.empty();
          }
        })
        .collect(Collectors.toSet());

    searchConditionsNames.removeIf(name -> getLastCriteriaChangeForName(changes,
        name) instanceof DdmDropSearchConditionChange);

    return searchConditionsNames.stream()
        .map(s -> s.replaceAll("_", "-"))
        .collect(Collectors.toSet());
  }

  private Change getLastCriteriaChangeForName(List<Change> changes, String name) {
    return changes.stream()
        .filter(change -> change instanceof DdmCreateSearchConditionChange ||
            change instanceof DdmDropSearchConditionChange)
        .filter(change -> {
          if (change instanceof DdmCreateSearchConditionChange) {
            return ((DdmCreateSearchConditionChange) change).getName().equals(name);
          } else {
            return ((DdmDropSearchConditionChange) change).getName().equals(name);
          }
        })
        .reduce((first, second) -> second)
        .orElse(null);
  }

  private Set<String> getCompositeEntityNames(List<Change> changes) {
    return changes.stream()
        .filter(change -> DdmCreateCompositeEntityChange.class.isAssignableFrom(change.getClass()))
        .map(change -> ((DdmCreateCompositeEntityChange) change).getName().replaceAll("_", "-"))
        .collect(Collectors.toSet());
  }

  private Set<String> getPartialUpdateEntityNames(List<Change> changes) {
    return changes.stream()
        .filter(change -> DdmPartialUpdateChange.class.isAssignableFrom(change.getClass()))
        .map(change -> ((DdmPartialUpdateChange) change).getName().replaceAll("_", "-"))
        .collect(Collectors.toSet());
  }

  private Map<String, ExternalSystem> getExternalSystems(RegulationFiles regulationFiles) {
    return BpmnUtil.getOutboundIntegrationExternalSystems(regulationFiles);
  }

  private Set<String> getExternalSystemNames() {
    return externalSystems.keySet();
  }

  private Set<String> getExternalSystemOperationNames() {
    return externalSystems.values().stream()
        .map(externalSystem -> externalSystem.getOperations().keySet())
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  private Set<String> getExcerptNames(RegulationFiles regulationFiles) {
    return regulationFiles.getExcerptFiles().stream()
        .flatMap(dir -> Arrays.stream(Objects.requireNonNull(dir.listFiles(file -> !file.isHidden()))))
        .map(file -> file.isDirectory() ? file.getName() : FilenameUtils.getBaseName(file.getName()))
        .filter(StringUtils::isNotBlank)
        .collect(Collectors.toSet());
  }

  private Set<String> getNotificationNames(RegulationFiles regulationFiles) {
    return Stream.of(regulationFiles.getDiiaNotificationTemplateDirectory(),
            regulationFiles.getEmailNotificationTemplateDirectory(),
            regulationFiles.getInboxNotificationTemplateDirectory())
        .flatMap(dir -> getNotificationNamesByDir(dir).stream())
        .collect(Collectors.toSet());
  }

  private Set<String> getNotificationNamesByDir(Collection<File> notificationDir) {
    return notificationDir.stream()
        .filter(File::isDirectory)
        .flatMap(dir -> Arrays.stream(Objects.requireNonNull(dir.listFiles(File::isDirectory))))
        .map(File::getName)
        .collect(Collectors.toSet());
  }

  private Boolean isExpression(String input) {
    return (input.startsWith("${") || input.startsWith("#{")) && input.endsWith("}");
  }

  private List<Change> getChanges(File mainLiquibase) {
    try {
      return getAllChanges(getDatabaseChangeLog(mainLiquibase));
    } catch (LiquibaseException e) {
      throw new FileProcessingException(
          String.format("A failure occurred while processing file %s", mainLiquibase.getName()), mainLiquibase, e);

    }
  }

  private static class ElementTemplateListTypeReference extends
      TypeReference<List<ElementTemplate>> {

  }
}
