package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file;

import com.epam.digital.data.platform.registry.regulation.validation.cli.model.RegulationFileType;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class IsNotDirectoryFileValidatorTest {

  @Test
  public void shouldPassForFile() {
    var validator = new IsNotDirectoryFileValidator();
    var notDirectoryFile = getFileFromClasspath("registry-regulation/correct/bp-auth.yml");

    var errors = validator.validate(notDirectoryFile, ValidationContext.of(RegulationFileType.DIIA_NOTIFICATION_TEMPLATE));

    assertThat(errors, is(empty()));
  }

  @Test
  public void shouldFailForDirectory() {
    var validator = new IsNotDirectoryFileValidator();
    var directoryFile = getFileFromClasspath("registry-regulation/correct");

    var errors = validator.validate(directoryFile, ValidationContext.of(RegulationFileType.DIIA_NOTIFICATION_TEMPLATE));

    assertThat(errors, is(not(empty())));
  }

  private File getFileFromClasspath(String filePath) {
    var classLoader = getClass().getClassLoader();
    return new File(classLoader.getResource(filePath).getFile());
  }
}
