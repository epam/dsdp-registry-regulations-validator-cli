package com.epam.digital.data.platform.registry.regulation.validation.cli.validator.file;

import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.RegulationValidator;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationContext;
import com.epam.digital.data.platform.registry.regulation.validation.cli.validator.ValidationError;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public class IsDirectoryFileValidator implements RegulationValidator<File> {

  private static final String FILE_IS_NOT_DIRECTORY_ERROR_MSG = "Input file is not directory";

  @Override
  public Set<ValidationError> validate(File regulationFile, ValidationContext validationContext) {
    if (regulationFile.isDirectory()) {
      return Collections.emptySet();
    }

    var error = ValidationError.builder()
        .regulationFileType(validationContext.getRegulationFileType())
        .regulationFile(regulationFile)
        .errorMessage(FILE_IS_NOT_DIRECTORY_ERROR_MSG)
        .build();

    return Collections.singleton(error);
  }
}
