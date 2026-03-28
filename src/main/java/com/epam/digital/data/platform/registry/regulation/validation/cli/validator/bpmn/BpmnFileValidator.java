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

import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelException;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.Task;
import org.camunda.bpm.model.xml.ModelParseException;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;

@Slf4j
public class BpmnFileValidator implements RegulationValidator<File> {

  private static final String MISSING_PROCESS_NAME_ERROR_MSG_FORMAT = "Process definition '%s' must have 'name' attribute defined";
  private static final String PROCESS_NAME_VALIDATION_ERROR_MSG = "Error during validation attribute 'name' of process : '%s' ";
  private static final String PROCESS_ID_VALIDATION_ERROR_MSG = "Error during validation attribute 'id' of process : '%s' ";
  private static final String UNABLE_VALIDATE_JUEL_FUNCTION_MSG_FORMAT = "Unable to validate %s() juel function due to dynamic value: %s";
  private static final String PROCESS_NAME_REGULAR_EXPRESSION = "^[A-Za-zА-Яа-яєЄЁёІіЇїҐґ0-9\\-_',.' ]{3,100}$";
  private static final String PROCESS_ID_REGULAR_EXPRESSION = "^[a-zA-Z][a-zA-Z0-9_-]{1,48}[a-zA-Z]$";
  private static final Map<String, List<Class<?>>> juelFunctions = Map.of(
      "completer", List.of(Task.class),
      "message_payload", List.of(ModelElementInstance.class),
      "sign_submission", List.of(Task.class, StartEvent.class),
      "submission", List.of(Task.class, StartEvent.class));

  @Override
  public Set<ValidationError> validate(File regulationFile, ValidationContext validationContext) {
    try {
      var bpmnModel = loadProcessModel(regulationFile);
      var xmlContent = FileUtils.readFileToString(regulationFile);
      return validateProcessModel(bpmnModel, regulationFile, validationContext, xmlContent);
    } catch (BpmnModelException | ModelParseException | IOException ex) {
      return Collections.singleton(
          ValidationError.of(validationContext.getRegulationFileType(), regulationFile,
              createErrorMsg(ex), ex)
      );
    }
  }

  private String createErrorMsg(Throwable ex) {
    return new StringJoiner(", ")
        .add("BPMN file parsing failure")
        .add(Objects.nonNull(ex.getCause()) ? ex.getCause().getMessage() : ex.getMessage())
        .toString();
  }

  private Set<ValidationError> validateProcessModel(BpmnModelInstance bpmnModel,
      File regulationFile, ValidationContext validationContext, String xmlContent) {
    var errors = new LinkedHashSet<ValidationError>();
    errors.addAll(validateSchema(bpmnModel, regulationFile, validationContext));
    errors.addAll(validateConditionExpression(bpmnModel, regulationFile, validationContext));
    juelFunctions.forEach(
        (key, value) -> validateJuelFunction(key, value, bpmnModel, regulationFile,
            validationContext, xmlContent, errors));

    return errors;
  }

  /**
   * Accepted
   * This is an internal tool, and the identified issue is not critical.
   */
  @SuppressWarnings("findsecbugs:GROOVY_SHELL")
  private Set<ValidationError> validateConditionExpression(BpmnModelInstance bpmnModel, File regulationFile, ValidationContext validationContext) {
    var conditionExpressions = bpmnModel.getModelElementsByType(SequenceFlow.class);
    var errors = new HashSet<ValidationError>();
    var groovyShell = new GroovyShell();
    for (var exp : conditionExpressions) {
      try {
        var conditionExpression = exp.getConditionExpression();
        if(conditionExpression != null && conditionExpression.getLanguage() == null) {
          groovyShell.parse(exp.getConditionExpression().getTextContent()
                  .replaceAll("[${}]", ""));
        }
      } catch (MultipleCompilationErrorsException exception) {
        errors.add(ValidationError.of(validationContext.getRegulationFileType(), regulationFile,
                String.format("Condition expression '%s' is not valid", exp.getTextContent()), exception));
      }
    }
    return errors;
  }

  private Set<ValidationError> validateSchema(BpmnModelInstance bpmnModel, File regulationFile,
                                              ValidationContext validationContext) {
    //validate bpmnModel
    try {
      Bpmn.validateModel(bpmnModel);
    } catch (ModelValidationException ex) {
      return Collections.singleton(
              ValidationError.of(validationContext.getRegulationFileType(), regulationFile,
                      "BPMN file validation against the schema failure", ex)
      );
    }

    //validate processes List
    var processes = bpmnModel.getModelElementsByType(Process.class);
    Set<ValidationError> resultErrorsList = new HashSet<>();
    for (Process process : processes) {
      if (StringUtils.isBlank(process.getName())) {
        resultErrorsList.add(
                toValidationError(process, regulationFile, validationContext, MISSING_PROCESS_NAME_ERROR_MSG_FORMAT));
      } else if (!Pattern.matches(PROCESS_NAME_REGULAR_EXPRESSION, process.getName())) {
        resultErrorsList.add(
                toValidationError(process, regulationFile, validationContext, PROCESS_NAME_VALIDATION_ERROR_MSG));
      }
      if (!Pattern.matches(PROCESS_ID_REGULAR_EXPRESSION, process.getId())) {
        resultErrorsList.add(toValidationError(process, regulationFile, validationContext,
                PROCESS_ID_VALIDATION_ERROR_MSG));
      }
    }
    return resultErrorsList;
  }

  private void validateJuelFunction(String functionName,
      List<Class<?>> modelElementClass, BpmnModelInstance bpmnModel,
      File regulationFile, ValidationContext validationContext, String xmlContent,
      Set<ValidationError> errors) {
    var values = getJuelFunctionsValues(functionName, xmlContent);

    for (var value : values) {
      var valueWithoutQuotes = extractValue(value);
      if (Objects.isNull(valueWithoutQuotes)) {
        log.warn(
            String.format(UNABLE_VALIDATE_JUEL_FUNCTION_MSG_FORMAT, functionName, value));
        continue;
      }
      var element = bpmnModel.getModelElementById(valueWithoutQuotes);
      if (Objects.isNull(element) || modelElementClass.stream()
          .noneMatch(c -> c.isAssignableFrom(element.getClass()))) {
        errors.add(ValidationError.of(validationContext.getRegulationFileType(), regulationFile,
            String.format("Element id %s not found for %s() juel function", value, functionName)));
      }
    }
  }

  private List<String> getJuelFunctionsValues(String functionName, String xmlContent) {
    String pattern = "(?<!\\w)" + functionName + "\\((.*?)\\)";
    Pattern regexPattern = Pattern.compile(pattern);
    Matcher matcher = regexPattern.matcher(xmlContent);
    List<String> values = new ArrayList<>();
    while (matcher.find()) {
      values.add(matcher.group(1));
    }
    return values;
  }

  private String extractValue(String value) {
    String pattern = "^(\"[^\"']*\"|'[^\"']*')$";
    Pattern regexPattern = Pattern.compile(pattern);
    Matcher matcher = regexPattern.matcher(value);
    if (matcher.matches()) {
      return value.substring(1, value.length() - 1);
    }
    return null;
  }

  private ValidationError toValidationError(Process process, File regulationFile,
      ValidationContext validationContext, String message) {
    return ValidationError.builder()
        .regulationFileType(validationContext.getRegulationFileType())
        .regulationFile(regulationFile)
        .errorMessage(String.format(message, process.getId()))
        .build();
  }

  private BpmnModelInstance loadProcessModel(File regulationFile) {
    return Bpmn.readModelFromFile(regulationFile);
  }
}
