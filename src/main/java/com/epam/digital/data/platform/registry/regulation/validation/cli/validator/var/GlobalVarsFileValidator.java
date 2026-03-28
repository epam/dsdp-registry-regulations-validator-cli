/*
 * Copyright 2022 EPAM Systems.
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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.var;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.json.JsonSchemaFileValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class GlobalVarsFileValidator extends JsonSchemaFileValidator {
  private static final String EMAIL_REGEX = "^\\s*?(.+)@(.+?)\\s*$";
  /**
   * Accepted
   * This is an internal tool, and the identified issue is not critical.
   */
  @SuppressWarnings("java:S5852")
  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
  private static final Set<String> FORBIDDEN_DOMAINS = Set.of(
          "mail.ru", "internet.ru", "bk.ru", "inbox.ru", "mail.ua", "list.ru",
          "yandex.ru", "yandex.ua", "mail.yandex.ru", "mail.yandex.ua",  "ya.ru", "yandex.com", "ya.ua");
  public static final String SUPPORT_EMAIL = "supportEmail";
  public static final int EMAIL_DOMAIN_GROUP = 2;

  public GlobalVarsFileValidator(String jsonSchemaLocation, ResourceLoader resourceLoader, ObjectMapper fileObjectMapper) {
    super(jsonSchemaLocation, resourceLoader, fileObjectMapper);
  }

  @Override
  protected Set<ValidationError> validateSchema(File regulationFile, JsonNode jsonNode, RegulationFileType regulationFileType) {
    Set<ValidationError> errors = super.validateSchema(regulationFile, jsonNode, regulationFileType);
    errors.addAll(validateEmailDomain(regulationFile, jsonNode, regulationFileType));
    return errors;
  }

  private Set<ValidationError> validateEmailDomain(File regulationFile, JsonNode jsonNode, RegulationFileType regulationFileType) {
    var supportEmailNode = jsonNode.get(SUPPORT_EMAIL);
    if (Objects.isNull(supportEmailNode)) {
      return Collections.emptySet();
    }

    var supportEmail = supportEmailNode.asText();

    var matcher = EMAIL_PATTERN.matcher(supportEmail);

    if (!matcher.matches()) {
      return Collections.singleton(ValidationError.of(regulationFileType, regulationFile, "Wrong email format: " + supportEmail));
    }

    String supportEmailDomain = matcher.group(EMAIL_DOMAIN_GROUP);

    return FORBIDDEN_DOMAINS.contains(supportEmailDomain.toLowerCase()) ?
            Collections.singleton(ValidationError.of(regulationFileType, regulationFile, String.format("The domain %s is forbidden", supportEmailDomain)))
            : Collections.emptySet();

  }
}
