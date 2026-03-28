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

package com.epam.digital.data.platform.registry.regulation.validation.cli.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ElementTemplate {

  private String id;
  private String name;
  private List<String> appliesTo;
  private List<Property> properties = new ArrayList<>();

  @Data
  public static class Property {

    private String label;
    private String type;
    private String value;
    private String description;
    private boolean editable = true;
    private List<Choice> choices;
    private Binding binding = new Binding();
    private Constraints constraints = new Constraints();

    @Data
    public static class Binding {

      private String type;
      private String source;
      @JsonAlias("target")
      private String name;
    }

    @Data
    public static class Constraints {

      private boolean notEmpty = false;
      private String type;
    }

    @Data
    public static class Choice {

      private String name;
      private String value;
    }
  }
}
