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

package com.epam.digital.data.platform.registry.regulation.validation.cli.config;

import com.deliveredtechnologies.rulebook.model.RuleBook;
import com.deliveredtechnologies.rulebook.spring.SpringAwareRuleBookRunner;
import com.epam.digital.data.platform.registry.regulation.validation.cli.RegistryRegulationCommandLineRunner;
import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandManager;
import com.epam.digital.data.platform.registry.regulation.validation.cli.service.OpenShiftService;
import com.epam.digital.data.platform.registry.regulation.validation.cli.service.impl.OpenShiftServiceImpl;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.CommandLineArgsParser;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.CommandLineOptionsConverter;
import com.epam.digital.data.platform.registry.regulation.validation.cli.support.SystemExit;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidatorFactory;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.collect.Sets;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class AppConfig {

  @Bean
  @ConditionalOnProperty(name = "OPENSHIFT_NAMESPACE")
  public Config config(@Value("${OPENSHIFT_NAMESPACE}") String namespace) {
    return new OpenShiftConfigBuilder().withNamespace(namespace).build();
  }

  @Bean
  @ConditionalOnBean(Config.class)
  public OpenShiftService openShiftService(Config config) {
    return new OpenShiftServiceImpl(config);
  }

  @Bean
  public JsonMapper jsonMapper() {
    var jsonMapper = new JsonMapper();
    jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);
    return jsonMapper;
  }

  @Bean
  public CommandManager commandManager(RegulationValidatorFactory regulationValidatorFactory,
      SystemExit systemExit, @Lazy OpenShiftService openShiftService, JsonMapper jsonMapper) {
    return new CommandManager(regulationValidatorFactory, new CommandLineArgsParser(), systemExit,
        openShiftService, jsonMapper);
  }

  @Bean
  public CommandLineRunner commandLineRunner(SystemExit systemExit, CommandManager commandManager) {
    return new RegistryRegulationCommandLineRunner(new CommandLineArgsParser(),
        new CommandLineOptionsConverter(), systemExit, commandManager);
  }

  @Bean
  public RegulationValidatorFactory registryRegulationValidatorFactory(
      ResourceLoader resourceLoader, JsonMapper jsonMapper) {
    return new RegulationValidatorFactory(resourceLoader, yamlObjectMapper(), jsonMapper,
        datafactorySettingsYamlRuleBook(), mainLiquibaseRuleBook());
  }

  @Bean
  public SystemExit systemExit(ApplicationContext context) {
    return new SystemExit(context);
  }

  private YAMLMapper yamlObjectMapper() {
    return new YAMLMapper();
  }

  @Bean
  public RuleBook<Set<ValidationError>> datafactorySettingsYamlRuleBook() {
    return getRuleBookRunner(
        "com.epam.digital.data.platform.registry.regulation.validation.cli.validator.datasettings.rules");
  }

  @Bean
  public RuleBook<Set<ValidationError>> mainLiquibaseRuleBook() {
    return getRuleBookRunner(
        "com.epam.digital.data.platform.registry.regulation.validation.cli.validator.mainliquibase.rules");
  }

  @SuppressWarnings("unchecked")
  private RuleBook<Set<ValidationError>> getRuleBookRunner(String rulePackage) {
    var springAwareRuleBookRunner = new SpringAwareRuleBookRunner(rulePackage);

    springAwareRuleBookRunner.setDefaultResult(Sets.newHashSet());
    return springAwareRuleBookRunner;
  }
}
