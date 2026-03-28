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

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFiles;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.junit.jupiter.api.Test;

class RegulationFilesValidatorTest {

  @Test
  @SuppressWarnings("unchecked")
  public void shouldDelegateValidationForAllRegulationFileTypes() {
    RegulationValidator<File> bpAuthFileValidator = mock(RegulationValidator.class);
    RegulationValidator<File> inboundIntegrationFileValidator = mock(RegulationValidator.class);
    RegulationValidator<File> rolesFileValidator = mock(RegulationValidator.class);
    RegulationValidator<File> globalVarsFileValidator = mock(RegulationValidator.class);
    RegulationValidator<File> formsFileValidator = mock(RegulationValidator.class);
    RegulationValidator<File> bpmnFileValidator = mock(RegulationValidator.class);
    RegulationValidator<File> dmnFileValidator = mock(RegulationValidator.class);
    RegulationValidator<File> excerptFilesValidator = mock(RegulationValidator.class);
    RegulationValidator<Collection<File>> bpmnFilesValidator = mock(RegulationValidator.class);
    RegulationValidator<Collection<File>> reportFilesValidator = mock(RegulationValidator.class);
    RegulationValidator<Collection<File>> excerptsFilesValidator = mock(RegulationValidator.class);
    RegulationValidator<RegulationFiles> bpAuthToBpmnRegulationValidator = mock(RegulationValidator.class);
    RegulationValidator<RegulationFiles> inboundIntegrationToBpmnRegulationValidator = mock(RegulationValidator.class);
    RegulationValidator<File> bpGroupingUniqueNameValidator = mock(RegulationValidator.class);
    RegulationValidator<RegulationFiles> bpGroupingProcessDefinitionIdValidator = mock(RegulationValidator.class);
    RegulationValidator<RegulationFiles> bpInputValidator = mock(RegulationValidator.class);
    RegulationValidator<RegulationFiles> bpAuthToBpmnRoleExistenceValidator = mock(RegulationValidator.class);
    RegulationValidator<RegulationFiles> reportRoleExistenceValidator = mock(RegulationValidator.class);
    RegulationValidator<RegulationFiles> formToSearchConditionExistenceValidator = mock(RegulationValidator.class);
    RegulationValidator<RegulationFiles> citizenSignTaskToBpAuthValidator = mock(RegulationValidator.class);
    RegulationValidator<RegulationFiles> formCardRoleExistenceValidator = mock(RegulationValidator.class);

    var validators = Map.of(
        RegulationFileType.BP_AUTH, bpAuthFileValidator,
        RegulationFileType.EXT_INTEGRATIONS_INBOUND, inboundIntegrationFileValidator,
        RegulationFileType.ROLES, rolesFileValidator,
        RegulationFileType.GLOBAL_VARS, globalVarsFileValidator,
        RegulationFileType.FORMS, formsFileValidator,
        RegulationFileType.BPMN, bpmnFileValidator,
        RegulationFileType.DMN, dmnFileValidator,
        RegulationFileType.EXCERPTS, excerptFilesValidator,
        RegulationFileType.BP_GROUPING, bpGroupingUniqueNameValidator
    );
    var groupValidators = Map.of(
        RegulationFileType.BPMN, bpmnFilesValidator,
        RegulationFileType.EXCERPTS, excerptsFilesValidator,
        RegulationFileType.REPORTS, reportFilesValidator
    );

    var globalRegulationValidators = Map.of(
            RegulationFileType.BP_AUTH_TO_BPMN, bpAuthToBpmnRegulationValidator,
            RegulationFileType.INBOUND_INTEGRATION_TO_BPMN, inboundIntegrationToBpmnRegulationValidator,
            RegulationFileType.BP_GROUPING_TO_BPMN, bpGroupingProcessDefinitionIdValidator,
            RegulationFileType.BPMN, bpInputValidator,
            RegulationFileType.BP_ROLE_EXISTENCE, bpAuthToBpmnRoleExistenceValidator,
            RegulationFileType.REPORT_FOLDER, reportRoleExistenceValidator,
            RegulationFileType.FORM_TO_SC, formToSearchConditionExistenceValidator,
            RegulationFileType.CITIZEN_SIGN_TASK_TO_BP_ROLES, citizenSignTaskToBpAuthValidator,
            RegulationFileType.FORM_CARD_ROLE_EXISTENCE, formCardRoleExistenceValidator
    );

    var regulationFilesValidator = new RegulationFilesValidator(validators, groupValidators, globalRegulationValidators);

    var regulationFiles = RegulationFiles.builder()
        .bpAuthFiles(singleFile())
        .extIntegrationsInboundFiles(singleFile())
        .rolesFiles(singleFile())
        .globalVarsFiles(singleFile())
        .formFiles(singleFile())
        .bpmnFiles(singleFile())
        .dmnFiles(singleFile())
        .excerptFiles(singleFile())
        .bpGroupingFiles(singleFile())
        .reportsFolders(singleFile())
        .build();

    regulationFilesValidator.validate(regulationFiles, ValidationContext.empty());

    verify(bpAuthFileValidator, times(1)).validate(any(), any());
    verify(inboundIntegrationFileValidator, times(1)).validate(any(), any());
    verify(rolesFileValidator, times(1)).validate(any(), any());
    verify(globalVarsFileValidator, times(1)).validate(any(), any());
    verify(formsFileValidator, times(1)).validate(any(), any());
    verify(bpmnFileValidator, times(1)).validate(any(), any());
    verify(dmnFileValidator, times(1)).validate(any(), any());
    verify(bpmnFilesValidator, times(1)).validate(any(), any());
    verify(excerptsFilesValidator, times(1)).validate(any(), any());
    verify(bpGroupingUniqueNameValidator, times(1)).validate(any(), any());
    verify(formToSearchConditionExistenceValidator, times(1)).validate(any(), any());
    verify(formCardRoleExistenceValidator, times(1)).validate(any(), any());
  }

  private ArrayList<File> singleFile() {
    return newArrayList(new File("no-file"));
  }
}