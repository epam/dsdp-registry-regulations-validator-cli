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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.epam.digital.data.platform.registry.regulation.validation.cli.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SvgSanitizerTest {

  private SVGValidator sanitizer;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setup() {
    sanitizer = new SVGValidator();
  }

  @Test
  void shouldValidateSafeSvgFile() {
    File safeSvgFile = TestUtils.getFile("svg-samples/safe-svg.svg");

    ValidationResult result = sanitizer.validateSvg(safeSvgFile);

    assertTrue(result.isValid());
//    assertTrue(result.isValid(), "Safe SVG should be valid: " +
//        (result.getErrorMessage() != null ? result.getErrorMessage() : ""));
  }

  @Test
  void shouldDetectScriptInSvgFile() {
    File svgWithScript = TestUtils.getFile("svg-samples/svg-with-script.svg");

    ValidationResult result = sanitizer.validateSvg(svgWithScript);

    assertFalse(result.isValid());
//    assertThat(result.getErrorMessage(), containsString("script tags"));
  }

  @Test
  void shouldDetectEventHandlersInSvgFile() {
    File svgWithEvent = TestUtils.getFile("svg-samples/svg-with-event.svg");

    ValidationResult result = sanitizer.validateSvg(svgWithEvent);

    assertFalse(result.isValid());
//    assertThat(result.getErrorMessage(), containsString("event handlers"));
  }

  @Test
  void shouldAllowSafeXlinkHref() {
    File svgWithSafeLink = TestUtils.getFile("svg-samples/svg-with-safe-link.svg");

    ValidationResult result = sanitizer.validateSvg(svgWithSafeLink);

    assertTrue(result.isValid());
//    assertTrue(result.isValid(), "SVG with safe internal link should be valid: " +
//        (result.getErrorMessage() != null ? result.getErrorMessage() : ""));
  }

  @Test
  void shouldDetectUnsafeXlinkHref() {
    File svgWithUnsafeLink = TestUtils.getFile("svg-samples/svg-with-unsafe-link.svg");

    ValidationResult result = sanitizer.validateSvg(svgWithUnsafeLink);

    assertFalse(result.isValid());
  }

  @Test
  void shouldDetectJavaScriptUrl() {
    File svgWithJsUrl = TestUtils.getFile("svg-samples/svg-with-javascript-url.svg");

    ValidationResult result = sanitizer.validateSvg(svgWithJsUrl);

    assertFalse(result.isValid());
  }

  @Test
  void shouldDetectCSSInjectionAttempt() {
    File svgWithCSSInjection = TestUtils.getFile("svg-samples/svg-with-css-injection.svg");

    ValidationResult result = sanitizer.validateSvg(svgWithCSSInjection);

    assertFalse(result.isValid());
  }

  @Test
  void shouldHandleReadError() throws IOException {
    File tempFile = new File(tempDir.toFile(), "deleted.svg");
    Files.writeString(tempFile.toPath(), "<svg></svg>");

    assertTrue(tempFile.delete());

    ValidationResult result = sanitizer.validateSvg(tempFile);

    assertFalse(result.isValid());
//    assertThat(result.getErrorMessage(), containsString("Could not read SVG file"));
  }

  @Test
  void shouldHandleComplexButSafeSvg() {
    File complexSvg = TestUtils.getFile("svg-samples/complex-safe-svg.svg");

    ValidationResult result = sanitizer.validateSvg(complexSvg);

    assertTrue(result.isValid());
  }
}