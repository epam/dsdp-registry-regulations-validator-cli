/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.datasettings;

public final class RulesOrder {

    public static final int VERSION_DOES_NOT_MATCH_PATTERN_RULE = 1;
    public static final int PACKAGE_DOES_NOT_MATCH_PATTERN_RULE = 2;
    public static final int DATABASE_NAME_DOES_NOT_MATCH_PATTERN_RULE = 3;
    public static final int SUBPACKAGE_IS_JAVA_RESERVED_WORD_RULE = 4;
    public static final int RETENTION_POLICY_READ_RULE = 5;
    public static final int RETENTION_POLICY_WRITE_RULE = 6;

    private RulesOrder() {
    }
}
