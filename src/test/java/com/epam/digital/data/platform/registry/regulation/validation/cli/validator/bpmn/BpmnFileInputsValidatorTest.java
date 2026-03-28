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

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.CAMUNDA_NS;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.ElementTemplate;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import com.epam.digital.data.platform.registry.regulation.validation.cli.util.TestUtils;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.instance.Activity;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaIn;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputOutput;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaInputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOut;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaOutputParameter;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.util.CollectionUtils;

class BpmnFileInputsValidatorTest {

  @ParameterizedTest
  @MethodSource("getAttributeValuesFromActivity_noNamespaceParams")
  void getAttributeValuesFromActivity_noNamespace(String propName, Set<String> expectedValue) {
    var activity = Mockito.mock(Activity.class);
    var property = createEmptyProperty();
    property.getBinding().setName(propName);

    Mockito.doReturn(CollectionUtils.firstElement(expectedValue)).when(activity)
        .getAttributeValue(propName);

    var actualSet = BpmnFileInputsValidator.getAttributeValuesFromActivity(activity, property);

    Assertions.assertThat(actualSet).isEqualTo(expectedValue);
  }

  static Object[][] getAttributeValuesFromActivity_noNamespaceParams() {
    return new Object[][]{{"propName", Set.of("propValue")}, {"propName", Set.of()}};
  }

  @ParameterizedTest
  @MethodSource("getAttributeValuesFromActivity_namespaceParams")
  void getAttributeValuesFromActivity_namespace(String propName, Set<String> expectedValue) {
    var activity = Mockito.mock(Activity.class);
    var property = createEmptyProperty();
    property.getBinding().setName(propName);

    Mockito.doReturn(CollectionUtils.firstElement(expectedValue)).when(activity)
        .getAttributeValueNs(CAMUNDA_NS, propName.split(":")[1]);

    var actualSet = BpmnFileInputsValidator.getAttributeValuesFromActivity(activity, property);

    Assertions.assertThat(actualSet).isEqualTo(expectedValue);
  }

  static Object[][] getAttributeValuesFromActivity_namespaceParams() {
    return new Object[][]{{"camunda:propName", Set.of("propValue")}, {"ns:propName", Set.of()}};
  }

  @Test
  @SuppressWarnings("unchecked")
  void getPropertyValueFromActivity() {
    var activity = Mockito.mock(Activity.class);
    var extElements = Mockito.mock(ExtensionElements.class);
    Mockito.doReturn(extElements).when(activity).getExtensionElements();
    Query<ModelElementInstance> query = Mockito.mock(Query.class);
    Mockito.doReturn(query).when(extElements).getElementsQuery();
    Mockito.doReturn(query).when(query).filterByType(CamundaProperties.class);
    var camundaProperties = Mockito.mock(CamundaProperties.class);
    Mockito.doReturn(List.of(camundaProperties)).when(query).list();

    var propertyWithNeededName = Mockito.mock(CamundaProperty.class);
    Mockito.doReturn("propName").when(propertyWithNeededName).getCamundaName();
    Mockito.doReturn("propValue").when(propertyWithNeededName).getCamundaValue();
    var nullPropertyWithNeededName = Mockito.mock(CamundaProperty.class);
    Mockito.doReturn("propName").when(nullPropertyWithNeededName).getCamundaName();
    var propWithOtherName = Mockito.mock(CamundaProperty.class);
    Mockito.doReturn("propName2").when(propWithOtherName).getCamundaName();
    Mockito.doReturn(Set.of(propertyWithNeededName, nullPropertyWithNeededName, propWithOtherName))
        .when(camundaProperties).getCamundaProperties();

    var property = createEmptyProperty();
    property.getBinding().setName("propName");

    var actualSet = BpmnFileInputsValidator.getPropertyValueFromActivity(activity, property);

    Assertions.assertThat(actualSet).hasSize(1).contains("propValue");
  }

  @Test
  void getPropertyValueFromActivity_nullValue() {
    var activity = Mockito.mock(Activity.class);
    Mockito.doReturn(null).when(activity).getExtensionElements();

    var property = createEmptyProperty();
    property.getBinding().setName("propName");

    var actualSet = BpmnFileInputsValidator.getPropertyValueFromActivity(activity, property);

    Assertions.assertThat(actualSet).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void getInValueFromActivity() {
    var activity = Mockito.mock(Activity.class);
    var extElements = Mockito.mock(ExtensionElements.class);
    Mockito.doReturn(extElements).when(activity).getExtensionElements();
    Query<ModelElementInstance> query = Mockito.mock(Query.class);
    Mockito.doReturn(query).when(extElements).getElementsQuery();
    Mockito.doReturn(query).when(query).filterByType(CamundaIn.class);

    var inWithNeededName = Mockito.mock(CamundaIn.class);
    Mockito.doReturn("propName").when(inWithNeededName).getAttributeValue("target");
    Mockito.doReturn("propValue").when(inWithNeededName).getAttributeValue("sourceExpression");
    var nullInWithNeededName = Mockito.mock(CamundaIn.class);
    Mockito.doReturn("propName").when(nullInWithNeededName).getAttributeValue("target");
    var inWithOtherName = Mockito.mock(CamundaIn.class);
    Mockito.doReturn("propName2").when(inWithOtherName).getAttributeValue("target");
    Mockito.doReturn(List.of(inWithNeededName, nullInWithNeededName, inWithOtherName)).when(query)
        .list();

    var property = createEmptyProperty();
    property.getBinding().setName("propName");

    var actualSet = BpmnFileInputsValidator.getInValueFromActivity(activity, property);

    Assertions.assertThat(actualSet).hasSize(1).contains("propValue");
  }

  @Test
  void getInValueFromActivity_nullValue() {
    var activity = Mockito.mock(Activity.class);
    Mockito.doReturn(null).when(activity).getExtensionElements();

    var property = createEmptyProperty();
    property.getBinding().setName("propName");

    var actualSet = BpmnFileInputsValidator.getInValueFromActivity(activity, property);

    Assertions.assertThat(actualSet).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void getOutValueFromActivity() {
    var activity = Mockito.mock(Activity.class);
    var extElements = Mockito.mock(ExtensionElements.class);
    Mockito.doReturn(extElements).when(activity).getExtensionElements();
    Query<ModelElementInstance> query = Mockito.mock(Query.class);
    Mockito.doReturn(query).when(extElements).getElementsQuery();
    Mockito.doReturn(query).when(query).filterByType(CamundaOut.class);

    var outWithNeededName = Mockito.mock(CamundaOut.class);
    Mockito.doReturn("{ propValue }").when(outWithNeededName).getAttributeValue("source");
    Mockito.doReturn("propName").when(outWithNeededName).getAttributeValue("target");
    var nullOutWithNeededName = Mockito.mock(CamundaOut.class);
    Mockito.doReturn("{ propValue }").when(nullOutWithNeededName).getAttributeValue("source");
    var outWithOtherName = Mockito.mock(CamundaOut.class);
    Mockito.doReturn("{ propValue2 }").when(outWithOtherName).getAttributeValue("source");
    Mockito.doReturn(List.of(outWithNeededName, nullOutWithNeededName, outWithOtherName))
        .when(query).list();

    var property = createEmptyProperty();
    property.getBinding().setSource("{ propValue }");

    var actualSet = BpmnFileInputsValidator.getOutValueFromActivity(activity, property);

    Assertions.assertThat(actualSet).hasSize(1).contains("propName");
  }

  @Test
  void getOutValueFromActivity_nullValue() {
    var activity = Mockito.mock(Activity.class);
    Mockito.doReturn(null).when(activity).getExtensionElements();

    var property = createEmptyProperty();
    property.getBinding().setSource("{ propValue }");

    var actualSet = BpmnFileInputsValidator.getOutValueFromActivity(activity, property);

    Assertions.assertThat(actualSet).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void getInputParameterValueFromActivity() {
    var activity = Mockito.mock(Activity.class);
    var extElements = Mockito.mock(ExtensionElements.class);
    Mockito.doReturn(extElements).when(activity).getExtensionElements();
    Query<ModelElementInstance> query = Mockito.mock(Query.class);
    Mockito.doReturn(query).when(extElements).getElementsQuery();
    Mockito.doReturn(query).when(query).filterByType(CamundaInputOutput.class);
    var camundaInputOutput = Mockito.mock(CamundaInputOutput.class);
    Mockito.doReturn(List.of(camundaInputOutput)).when(query).list();

    var inputParameterWithNeededName = Mockito.mock(CamundaInputParameter.class);
    Mockito.doReturn("propName").when(inputParameterWithNeededName).getCamundaName();
    Mockito.doReturn("propValue").when(inputParameterWithNeededName).getTextContent();
    var nullInputParameterWithNeededName = Mockito.mock(CamundaInputParameter.class);
    Mockito.doReturn("propName").when(nullInputParameterWithNeededName).getCamundaName();
    var inputParameterWithOtherName = Mockito.mock(CamundaInputParameter.class);
    Mockito.doReturn("propName2").when(inputParameterWithOtherName).getCamundaName();
    Mockito.doReturn(List.of(inputParameterWithNeededName, nullInputParameterWithNeededName,
        inputParameterWithOtherName)).when(camundaInputOutput).getCamundaInputParameters();

    var property = createEmptyProperty();
    property.getBinding().setName("propName");

    var actualSet = BpmnFileInputsValidator.getInputParameterValueFromActivity(activity, property);

    Assertions.assertThat(actualSet).hasSize(1).contains("propValue");
  }

  @Test
  void getInputParameterValueFromActivity_nullValue() {
    var activity = Mockito.mock(Activity.class);
    Mockito.doReturn(null).when(activity).getExtensionElements();

    var property = createEmptyProperty();
    property.getBinding().setName("propName");

    var actualSet = BpmnFileInputsValidator.getInputParameterValueFromActivity(activity, property);

    Assertions.assertThat(actualSet).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  void getOutputParameterValueFromActivity() {
    var activity = Mockito.mock(Activity.class);
    var extElements = Mockito.mock(ExtensionElements.class);
    Mockito.doReturn(extElements).when(activity).getExtensionElements();
    Query<ModelElementInstance> query = Mockito.mock(Query.class);
    Mockito.doReturn(query).when(extElements).getElementsQuery();
    Mockito.doReturn(query).when(query).filterByType(CamundaInputOutput.class);
    var camundaInputOutput = Mockito.mock(CamundaInputOutput.class);
    Mockito.doReturn(List.of(camundaInputOutput)).when(query).list();

    var outputParameterWithNeededName = Mockito.mock(CamundaOutputParameter.class);
    Mockito.doReturn("{ propValue }").when(outputParameterWithNeededName).getTextContent();
    Mockito.doReturn("propName").when(outputParameterWithNeededName).getCamundaName();
    var nullOutputParameterWithNeededName = Mockito.mock(CamundaOutputParameter.class);
    Mockito.doReturn("{ propValue }").when(nullOutputParameterWithNeededName).getTextContent();
    var outputParameterWithOtherName = Mockito.mock(CamundaOutputParameter.class);
    Mockito.doReturn("{ propValue2 }").when(outputParameterWithOtherName).getTextContent();
    Mockito.doReturn(List.of(outputParameterWithNeededName, nullOutputParameterWithNeededName,
        outputParameterWithOtherName)).when(camundaInputOutput).getCamundaOutputParameters();

    var property = createEmptyProperty();
    property.getBinding().setSource("{ propValue }");

    var actualSet = BpmnFileInputsValidator.getOutputParameterValueFromActivity(activity, property);

    Assertions.assertThat(actualSet).hasSize(1).contains("propName");
  }
  @Test
  void getOutputParameterValueFromActivity_nullValue() {
    var activity = Mockito.mock(Activity.class);
    Mockito.doReturn(null).when(activity).getExtensionElements();

    var property = createEmptyProperty();
    property.getBinding().setSource("{ propValue }");

    var actualSet = BpmnFileInputsValidator.getOutputParameterValueFromActivity(activity, property);

    Assertions.assertThat(actualSet).isEmpty();
  }

  @Test
  void validateCorrect() {
    var templatePath = Objects.requireNonNull(
            getClass().getClassLoader().getResource("business-process-modeler-element-template.json"))
        .getPath();
    var defaultRoles = List.of("testRole");
    var validator = new BpmnFileInputsValidator(templatePath, defaultRoles);

    var correctFile = TestUtils.getFile("registry-regulation/correct/process-for-validating-inputs.bpmn");
    var mainLiquibase = TestUtils.getFile("registry-regulation/correct/test-main-liquibase.xml");
    var outboundIntegration = TestUtils.getFile("registry-regulation/correct/outbound-integration.yml");
    var excerptsDocx = TestUtils.getFile("registry-regulation/correct/excerpts-docx");
    var notificationTemplate = TestUtils.getFile("registry-regulation/correct/diia");
    var formFile = TestUtils.getFile("registry-regulation/correct/ui-form.json");

    RegulationFiles regulationFiles = RegulationFiles.builder()
        .bpmnFiles(List.of(correctFile))
        .liquibaseFiles(List.of(mainLiquibase))
        .extIntegrationsOutboundFiles(List.of(outboundIntegration))
        .excerptFiles(List.of(excerptsDocx))
        .diiaNotificationTemplateDirectory(List.of(notificationTemplate))
        .formFiles(List.of(formFile))
        .build();

    var result = validator.validate(regulationFiles, ValidationContext.empty());

    Assertions.assertThat(result).isEmpty();
  }

  @Test
  void validateBroken() {
    var templatePath = Objects.requireNonNull(
            getClass().getClassLoader().getResource("business-process-modeler-element-template.json"))
        .getPath();
    var defaultRoles = List.of("testRole");
    var validator = new BpmnFileInputsValidator(templatePath, defaultRoles);

    var correctFile = TestUtils.getFile("registry-regulation/broken/process-for-validating-inputs.bpmn");
    var mainLiquibase = TestUtils.getFile("registry-regulation/broken/test-main-liquibase.xml");
    var outboundIntegration = TestUtils.getFile("registry-regulation/correct/outbound-integration.yml");
    var excerptsDocx = TestUtils.getFile("registry-regulation/broken/excerpts-docx");
    var notificationTemplate = TestUtils.getFile("registry-regulation/broken/diia");
    var formFile = TestUtils.getFile("registry-regulation/broken/ui-form-broken.json");

    RegulationFiles regulationFiles = RegulationFiles.builder()
        .bpmnFiles(List.of(correctFile))
        .liquibaseFiles(List.of(mainLiquibase))
        .extIntegrationsOutboundFiles(List.of(outboundIntegration))
        .excerptFiles(List.of(excerptsDocx))
        .diiaNotificationTemplateDirectory(List.of(notificationTemplate))
        .formFiles(List.of(formFile))
        .build();
    var result = validator.validate(regulationFiles, ValidationContext.empty());

    Assertions.assertThat(result)
        .hasSize(24);
  }

  @Test
  void validateIllegalStateIfNoTemplatesFound() {
    var defaultRoles = List.of("testRole");
    Assertions.assertThatThrownBy(() -> new BpmnFileInputsValidator("nonExistedFile", defaultRoles))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("During reading elementTemplates file occurred error.")
        .hasCauseInstanceOf(FileNotFoundException.class);
  }

  private static ElementTemplate.Property createEmptyProperty() {
    var property = new ElementTemplate.Property();
    var binding = new ElementTemplate.Property.Binding();
    property.setBinding(binding);
    return property;
  }
}
