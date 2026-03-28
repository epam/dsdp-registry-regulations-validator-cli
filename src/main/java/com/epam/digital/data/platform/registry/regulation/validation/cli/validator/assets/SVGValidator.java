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

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.assets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

@Slf4j
public class SVGValidator {

  private static final Pattern SAFE_XLINK_HREF_PATTERN = Pattern.compile("^#[\\w\\-]+$");

  /**
   * Validates an SVG file.
   *
   * @param svgFile The SVG file to validate
   * @return ValidationResult with the validation status
   */
  public ValidationResult validateSvg(File svgFile) {
    try {
      String svgContent = new String(Files.readAllBytes(svgFile.toPath()));
      return validateSvgContent(svgContent);
    } catch (IOException e) {
      log.trace("Could not read SVG file: {}", svgFile.getAbsolutePath(), e);
      return new ValidationResult(false, "Could not read SVG file: " + e.getMessage());
    }
  }

  /**
   * Validates an SVG content.
   *
   * @param svgContent The SVG content to validate
   * @return ValidationResult with the validation status
   */
  public ValidationResult validateSvgContent(String svgContent) {
    try {
      Document originalDoc = Jsoup.parse(svgContent, "", Parser.xmlParser());

      if (!originalDoc.select("script").isEmpty()) {
        return new ValidationResult(false, "SVG contains script tags");
      }

      for (Element element : originalDoc.getAllElements()) {
        ValidationResult attrCheck = checkAttributes(element);
        if (!attrCheck.isValid()){
          return attrCheck;
        }
      }

      return new ValidationResult(true, null);
    } catch (Exception e) {
      log.trace("Error validating SVG content", e);
      return new ValidationResult(false, "Error validating SVG: " + e.getMessage());
    }
  }

  private ValidationResult checkAttributes(Element element) {
    for (var attribute : element.attributes()) {
      String attrName = attribute.getKey().toLowerCase();

      ValidationResult eventCheck = checkEventHandler(attrName);
      if (!eventCheck.isValid()){
        return eventCheck;
      }

      String attrValue = attribute.getValue();
      ValidationResult hrefCheck = checkHref(attrName, attrValue);
      if (!hrefCheck.isValid()){
        return hrefCheck;
      }

      ValidationResult styleCheck = checkStyle(attrName, attrValue);
      if (!styleCheck.isValid()) {
        return styleCheck;
      }
    }
    return new ValidationResult(true, null);
  }

  private ValidationResult checkEventHandler(String attrName) {
    if (attrName.startsWith("on")) {
      return new ValidationResult(false, "SVG contains event handlers");
    }
    return new ValidationResult(true, null);
  }

  private ValidationResult checkHref(String attrName, String attrValue) {
    if ("xlink:href".equals(attrName) || "href".equals(attrName)) {
      if (!SAFE_XLINK_HREF_PATTERN.matcher(attrValue).matches() ) {
        return new ValidationResult(false,
          "SVG contains unsafe URL reference: " + attrValue);
      }
    }
    return new ValidationResult(true, null);
  }

  private ValidationResult checkStyle(String attrName, String attrValue) {
    if ("style".equals(attrName)) {
      if (attrValue.contains("javascript:") ||
        attrValue.contains("expression(") ||
        attrValue.contains("url(") && attrValue.contains(":")) {
        return new ValidationResult(false, "SVG contains dangerous style content");
      }
    }
    return new ValidationResult(true, null);
  }
}