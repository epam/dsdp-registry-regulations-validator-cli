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


package com.epam.digital.data.platform.registry.regulation.validation.cli.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

public final class TestUtils {

  private TestUtils() {
  }

  public static File getFile(String resourcePath) {
    try {
      URL resource = Objects.requireNonNull(
        TestUtils.class.getClassLoader().getResource(resourcePath),
        "Resource not found: " + resourcePath
      );
      return Path.of(resource.toURI()).normalize().toFile();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}
