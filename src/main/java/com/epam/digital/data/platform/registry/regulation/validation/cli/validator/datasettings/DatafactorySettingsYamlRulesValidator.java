/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.datasettings;

import com.deliveredtechnologies.rulebook.Fact;
import com.deliveredtechnologies.rulebook.FactMap;
import com.deliveredtechnologies.rulebook.NameValueReferableMap;
import com.deliveredtechnologies.rulebook.model.RuleBook;
import com.epam.digital.data.platform.registry.regulation.validation.cli.model.DatafactorySettingsYaml;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.FactNames;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class DatafactorySettingsYamlRulesValidator implements RegulationValidator<File> {
    private final RuleBook<Set<ValidationError>> settingsYamlRuleBook;
    private final ObjectMapper yamlObjectMapper;

    public DatafactorySettingsYamlRulesValidator(ObjectMapper yamlObjectMapper, RuleBook<Set<ValidationError>> settingsYamlRuleBook) {
        this.yamlObjectMapper = yamlObjectMapper;
        this.settingsYamlRuleBook = settingsYamlRuleBook;
    }

    @Override
    public Set<ValidationError> validate(File regulationFile, ValidationContext context) {
        try {
            settingsYamlRuleBook.run(getSettingsYamlFacts(regulationFile, context));
            var result = settingsYamlRuleBook.getResult();
            return result.isPresent() ? result.get().getValue() : Collections.emptySet();
        } catch (IOException e) {
            return Collections.singleton(
                    ValidationError.of(context.getRegulationFileType(), regulationFile, "SettingsYaml file processing failure", e)
            );
        }
    }

    private NameValueReferableMap<Object> getSettingsYamlFacts(File regulationFile, ValidationContext context) throws IOException {
        NameValueReferableMap<Object> settingsYamlFacts = new FactMap<>();
        settingsYamlFacts.put(new Fact<>(FactNames.DATAFACTORY_SETTINGS_YAML, readSettingsYaml(regulationFile)));
        settingsYamlFacts.put(new Fact<>(FactNames.REGULATION_FILE, regulationFile));
        settingsYamlFacts.put(new Fact<>(FactNames.REGULATION_FILE_TYPE, context.getRegulationFileType()));

        return settingsYamlFacts;
    }

    private DatafactorySettingsYaml readSettingsYaml(File regulationFile) throws IOException {
        return yamlObjectMapper.readValue(new FileInputStream(regulationFile), DatafactorySettingsYaml.class);
    }
}