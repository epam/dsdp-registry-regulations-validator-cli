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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.epam.digital.data.platform.registry.regulation.validation.cli.service.OpenShiftService;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.server.mock.EnableKubernetesMockClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import java.net.HttpURLConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@EnableKubernetesMockClient
@ExtendWith(SpringExtension.class)
class OpenShiftServiceTest {

  OpenShiftService openShiftService;
  KubernetesMockServer server;

  @BeforeEach
  void init() {
    this.openShiftService = new OpenShiftServiceImpl(server.createClient().getConfiguration());
  }

  @Test
  void shouldSaveBusinessOperationChecksumToSecret() {
    var businessOperation = "update-bpmn";
    var expectedChecksum = "CA7AC3F89FC5E0FAD3C303ED746FF8EF4D20918A4AF5290246C";
    server.expect()
        .get()
        .withPath("/api/v1/namespaces/test/secrets/registry-regulation-state")
        .andReturn(HttpURLConnection.HTTP_OK, "")
        .once();
    server.expect()
        .post()
        .withPath("/api/v1/namespaces/test/secrets")
        .andReturn(HttpURLConnection.HTTP_OK, "")
        .once();

    openShiftService.saveBusinessOperationChecksumToSecret(businessOperation, expectedChecksum);

    assertEquals(2, server.getRequestCount());
  }

  @Test
  void shouldGetBusinessOperationChecksumFromSecret() {
    var businessOperation = "update-bp-grouping";
    var expectedChecksum = "77038579BD8B3CA7AC3F89FC5E0FAD3C303ED746FF8EF4D20918A4AF5290246C";
    var secretWithData = new SecretBuilder()
        .addToData(businessOperation, expectedChecksum)
        .build();
    server.expect()
        .get()
        .withPath("/api/v1/namespaces/test/secrets/registry-regulation-state")
        .andReturn(HttpURLConnection.HTTP_OK, secretWithData)
        .once();

    var actualChecksum = openShiftService.getBusinessOperationChecksumFromSecret(businessOperation);

    assertEquals(expectedChecksum, actualChecksum);
  }

  @Test
  void shouldReturnNullIfChecksumNotFoundForBusinessOperationFromSecret() {
    var unknownOperation = "unknown-business-operation";
    var businessOperation = "update-bp-grouping";
    var checksum = "77038579BD8B3CA7AC3F89FC5E0FAD3C303ED746FF8EF4D20918A4AF5290246C";
    var secretWithData = new SecretBuilder()
        .addToData(businessOperation, checksum)
        .build();
    server.expect()
        .get()
        .withPath("/api/v1/namespaces/test/secrets/registry-regulation-state")
        .andReturn(HttpURLConnection.HTTP_OK, secretWithData)
        .once();

    var actualChecksum = openShiftService.getBusinessOperationChecksumFromSecret(unknownOperation);

    assertNull(actualChecksum);
  }
}
