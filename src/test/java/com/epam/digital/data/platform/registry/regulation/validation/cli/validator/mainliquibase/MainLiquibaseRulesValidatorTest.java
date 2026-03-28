package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.mainliquibase;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.AbstractRulesValidatorTest;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainLiquibaseRulesValidatorTest extends AbstractRulesValidatorTest {

    private static final String MAIN_LIQUIBASE_BROKEN_BASE_PATH = "registry-regulation/broken/main-liquibase/";

    private RegulationValidator<File> validator;

    @BeforeEach
    public void setUp() {
        this.validator = new MainLiquibaseRulesValidator(getRuleBookRunner(
                "com.epam.digital.data.platform.registry.regulation.validation.cli.validator.mainliquibase.rules"));
    }

    @Test
    void shouldPassMainLiquibaseValidation() {
        var processFile = getFileFromClasspath("registry-regulation/correct/test-main-liquibase.xml");
        var errors = validator.validate(processFile, ValidationContext.of(RegulationFileType.LIQUIBASE));

        assertThat(errors, is(empty()));
    }

    @Test
    void shouldFailMainLiquibaseValidation() {
        var processFile = getFileFromClasspath(
            MAIN_LIQUIBASE_BROKEN_BASE_PATH + "test-main-liquibase.xml");
        var errors = validator.validate(processFile, ValidationContext.of(RegulationFileType.LIQUIBASE));

        assertThat(errors, is(not(empty())));
    }

    @Test
    void shouldFailMainLiquibaseValidationDueToMissedChangeLog() {
        var processFile = getFileFromClasspath(
            MAIN_LIQUIBASE_BROKEN_BASE_PATH + "test-main-liquibase-broken.xml");
        var errors = validator.validate(processFile, ValidationContext.of(RegulationFileType.LIQUIBASE));

        assertThat(errors, is(not(empty())));
    }

    @Test
    void shouldWriteErrorWhenColumnNameHasCyrillic() {
        var processFile = getFileFromClasspath(MAIN_LIQUIBASE_BROKEN_BASE_PATH + "cyrillic-column-name.xml");
        var errors = getErrorMessages(processFile);

        assertThat(errors.size(), is(1));
        assertTrue(errors.contains("The following columns contain Cyrillic characters, which is not allowed: [прізвище]"));
    }

    @Test
    void shouldWriteErrorWhenColumnNameStartsFromDigit() {
        var processFile = getFileFromClasspath(MAIN_LIQUIBASE_BROKEN_BASE_PATH + "starts-from-digit-column-name.xml");
        var errors = getErrorMessages(processFile);

        assertThat(errors.size(), is(1));
        assertTrue(errors.contains("The names of the following columns begin with a number, which is not allowed: [1age]"));
    }

    @Test
    void shouldWriteErrorWhenColumnNameIsTooLong() {
        var processFile = getFileFromClasspath(MAIN_LIQUIBASE_BROKEN_BASE_PATH + "too-long-column-name.xml");
        var errors = getErrorMessages(processFile);

        assertThat(errors.size(), is(1));
        assertTrue(errors.contains("The following columns are named longer than 63 characters, which is invalid: [ten_chars1ten_chars2ten_chars3ten_chars4ten_chars5ten_chars6____]"));
    }

    @Test
    void shouldWriteErrorWhenPrimaryKeyHasCapitalLetter() {
        var processFile = getFileFromClasspath(MAIN_LIQUIBASE_BROKEN_BASE_PATH + "primary-key-capital-letter.xml");
        var errors = getErrorMessages(processFile);

        assertThat(errors.size(), is(1));
        assertTrue(errors.contains("The following foreign keys contain uppercase characters, which is invalid: [fk_with_capital_letteR]"));
    }

    @Test
    void shouldWriteErrorWhenColumnNameIsReservedJavaWord() {
        var processFile = getFileFromClasspath(MAIN_LIQUIBASE_BROKEN_BASE_PATH + "reserved-java-word-column-name.xml");
        var errors = getErrorMessages(processFile);

        assertThat(errors.size(), is(1));
        assertTrue(errors.contains("The following column names are equal to Java reserved words, which is not allowed: [class]"));
    }

    @Test
    void shouldWriteErrorWhenTableNameIsReservedJavaWord() {
        var processFile = getFileFromClasspath(MAIN_LIQUIBASE_BROKEN_BASE_PATH + "reserved-java-word-table-name.xml");
        var errors = getErrorMessages(processFile);

        assertThat(errors.size(), is(1));
        assertTrue(errors.contains("The following table names are equal to Java reserved words, which is not allowed: [class]"));
    }

    private Set<String> getErrorMessages(File processFile) {
        return validator.validate(processFile, ValidationContext.of(RegulationFileType.LIQUIBASE))
            .stream()
            .map(ValidationError::getErrorMessage)
            .collect(Collectors.toSet());
    }
}