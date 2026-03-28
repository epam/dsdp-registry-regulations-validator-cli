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

package com.epam.digital.data.platform.registry.regulation.validation.cli.exception;

/**
 * An exception that represents a runtime error related to communication or interaction with
 * OpenShift or Kubernetes resources. This exception is thrown when there is a failure or error
 * during communication with an OpenShift cluster, such as when retrieving or updating resources.
 *
 * <p>This exception extends {@link RuntimeException}, making it an unchecked exception that
 * does not require explicit exception handling (catch or declare). It can be used to signal
 * exceptional situations in OpenShift or Kubernetes communication code.
 */
public class OpenShiftCommunicationException extends RuntimeException {

  /**
   * Constructs a new {@code OpenShiftCommunicationException} with the specified error message.
   *
   * @param message A descriptive error message that provides information about the communication
   *                error or issue with OpenShift.
   * @param cause   the original exception that caused this exception.
   */
  public OpenShiftCommunicationException(String message, Throwable cause) {
    super(message, cause);
  }
}
