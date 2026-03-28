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

package com.epam.digital.data.platform.registry.regulation.validation.cli.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class BpAuthConfiguration implements RegulationConfiguration {

  @JsonIgnore
  private File regulationFile;

  private Authorization authorization;

  @Data
  public static class Authorization {

    private String realm;

    @JsonProperty(value = "process_definitions")
    private List<ProcessDefinition> processDefinitions = new ArrayList<>();
  }

  @Data
  @EqualsAndHashCode(of = {"processDefinitionId"})
  public static class ProcessDefinition implements Identifiable<String> {

    @JsonProperty("process_definition_id")
    private String processDefinitionId;

    @JsonProperty("process_name")
    private String processName;

    @JsonProperty("process_description")
    private String processDescription;

    private List<String> roles = new ArrayList<>();

    @Override
    @JsonIgnore
    public String getId() {
      return processDefinitionId;
    }
  }
}