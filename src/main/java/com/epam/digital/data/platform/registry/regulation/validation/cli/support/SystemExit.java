/*
 * Copyright 2021 EPAM Systems.
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

package com.epam.digital.data.platform.registry.regulation.validation.cli.support;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class SystemExit {

  private static final int SUCCESS_EXIT_CODE = 0;
  private static final int SYSTEM_ERROR_EXIT_CODE = 1;
  private static final int VALIDATION_FAILURE_EXIT_CODE = 10;

  private final ApplicationContext context;

  public SystemExit(ApplicationContext context) {
    this.context = context;
  }

  public void complete() {
    exit(SUCCESS_EXIT_CODE);
  }

  public void validationFailure() {
    exit(VALIDATION_FAILURE_EXIT_CODE);
  }

  public void systemError() {
    exit(SYSTEM_ERROR_EXIT_CODE);
  }

  private void exit(int exitCode) {
    SpringApplication.exit(context, () -> exitCode);
  }
}
