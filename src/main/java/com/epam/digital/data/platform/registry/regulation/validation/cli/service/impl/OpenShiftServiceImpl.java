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

package com.epam.digital.data.platform.registry.regulation.validation.cli.service.impl;

import com.epam.digital.data.platform.registry.regulation.validation.cli.exception.OpenShiftCommunicationException;
import com.epam.digital.data.platform.registry.regulation.validation.cli.service.OpenShiftService;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.client.DefaultOpenShiftClient;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

/**
 * The class is an implementation of the {@link OpenShiftService} interface responsible for
 * interacting with an OpenShift container orchestration platform to manage business operation
 * checksums in secrets.
 */
@RequiredArgsConstructor
public class OpenShiftServiceImpl implements OpenShiftService {

  private static final String SECRET_NAME = "registry-regulation-state";

  private final Config openShiftConfig;

  @Override
  public void saveBusinessOperationChecksumToSecret(String businessOperation, String checksum) {
    execute(openShiftClient -> {
      var secret = getOrCreateSecret(openShiftClient);
      secret.getData().put(businessOperation, checksum);
      return openShiftClient.secrets().createOrReplace(secret);
    });
  }

  @Override
  public String getBusinessOperationChecksumFromSecret(String businessOperation) {
    return execute(
        openShiftClient -> getOrCreateSecret(openShiftClient).getData().get(businessOperation));
  }


  /**
   * Executes the provided function within a try-with-resources block, ensuring proper cleanup of
   * the OpenShift client resources.
   *
   * @param function The function to execute, which takes an {@link OpenShiftClient} as a
   *                 parameter.
   * @param <T>      The return type of the function.
   * @return The result of executing the function.
   * @throws OpenShiftCommunicationException if an error occurs while interacting with OpenShift.
   */
  private <T> T execute(Function<OpenShiftClient, T> function) {
    try (OpenShiftClient openShiftClient = new DefaultOpenShiftClient(openShiftConfig)) {
      return function.apply(openShiftClient);
    } catch (KubernetesClientException e) {
      throw new OpenShiftCommunicationException("Error processing OpenShift secret", e);
    }
  }

  /**
   * Retrieves an existing secret or creates a new one with the specified name.
   *
   * @param openShiftClient The OpenShift client used to interact with the platform.
   * @return The secret with the specified name, creating a new one if it doesn't exist.
   */
  private Secret getOrCreateSecret(OpenShiftClient openShiftClient) {
    var secret = openShiftClient.secrets().withName(SECRET_NAME).get();
    if (Objects.isNull(secret)) {
      secret = new Secret();
      secret.setData(new HashMap<>());
      var metaData = new ObjectMeta();
      metaData.setName(SECRET_NAME);
      secret.setMetadata(metaData);
    }
    return secret;
  }
}
