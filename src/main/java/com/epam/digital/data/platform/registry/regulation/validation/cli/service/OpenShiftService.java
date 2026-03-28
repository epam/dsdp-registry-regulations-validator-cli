/*
 * Copyright 2023 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.registry.regulation.validation.cli.service;

/**
 * The interface defines the contract for interacting with an OpenShift container orchestration
 * platform to manage business operation checksums in secrets. It provides methods for saving and
 * retrieving checksums associated with specific business operations.
 *
 * <p>The primary responsibilities of the {@code OpenShiftService} interface are to save the
 * checksum for a business operation to an OpenShift secret and retrieve the checksum for a
 * specified business operation from the secret.
 *
 * <p>Implementations of this interface will handle the details of integrating with OpenShift and
 * ensuring the security and accessibility of stored checksums.
 */
public interface OpenShiftService {

  /**
   * Saves the checksum associated with a specific business operation to an OpenShift secret.
   *
   * @param businessOperation The identifier of the business operation for which the checksum is
   *                          being saved.
   * @param checksum          The checksum value to be saved.
   */
  void saveBusinessOperationChecksumToSecret(String businessOperation, String checksum);


  /**
   * Retrieves the checksum associated with a specific business operation from an OpenShift secret.
   *
   * @param businessOperation The identifier of the business operation for which the checksum is
   *                          being retrieved.
   * @return The checksum value associated with the specified business operation, or {@code null} if
   * no checksum is found for the operation.
   */
  String getBusinessOperationChecksumFromSecret(String businessOperation);
}
