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

package com.epam.digital.data.platform.registry.regulation.validation.cli.utils;

import com.epam.digital.data.platform.registry.regulation.validation.cli.command.CommandFileOption;
import com.epam.digital.data.platform.registry.regulation.validation.cli.exception.FileProcessingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 * The class provides utility methods for generating checksums for files and directories using the
 * SHA-256 hashing algorithm.
 *
 * <p>It includes methods to generate checksums for both files and directories. When generating
 * checksums for directories, it supports two modes: detailed and non-detailed. In the detailed
 * mode, it computes checksums for each individual file within the directory, while in the
 * non-detailed mode, it computes a single checksum for the entire directory.
 *
 * <p>The class uses a {@link  HexBinaryAdapter} to convert byte arrays into hexadecimal strings.
 *
 * <p>Checksums are returned as maps, where keys are file paths relative to the processed
 * directory, and values are the corresponding checksums.
 */
public class FileChecksumGenerator {

  public static final String ALGORITHM = "SHA-256";

  private static final HexBinaryAdapter hexBinaryAdapter = new HexBinaryAdapter();

  private FileChecksumGenerator() {
  }

  /**
   * Generates checksums for a collection of files and directories.
   *
   * @param files A collection of {@link  File} objects for which checksums are generated.
   * @return A map containing file paths as keys and their respective checksums as values.
   */
  public static Map<String, String> generateFilesChecksum(Collection<File> files) {
    return processFilesChecksum(files, FileChecksumGenerator::getFilesChecksum);
  }

  /**
   * Generates detailed checksums for a collection of files and directories.
   *
   * @param files A collection of {@link  File} objects for which detailed checksums are generated.
   * @return A map containing file paths as keys and their respective checksums as values.
   */
  public static Map<String, String> generateDetailedFilesChecksum(Collection<File> files) {
    return processFilesChecksum(files, FileChecksumGenerator::getDetailedFilesChecksum);
  }

  private static Map<String, String> processFilesChecksum(Collection<File> files,
      Function<File, Map<String, String>> function) {
    var checksum = new HashMap<String, String>();
    for (File file : files) {
      if (file.exists()) {
        checksum.putAll(function.apply(file));
      }
    }
    return checksum;
  }

  private static Map<String, String> getDetailedFilesChecksum(File file) {
    return processChecksum(file, CommandFileOption.FILE_DETAILED);
  }

  private static Map<String, String> getFilesChecksum(File file) {
    return processChecksum(file, CommandFileOption.FILE);
  }

  private static Map<String, String> processChecksum(File file, CommandFileOption fileOption) {
    try {
      var messageDigest = MessageDigest.getInstance(ALGORITHM);
      var filePath = file.toPath();
      var directoryPathFromRelativize =
        Objects.isNull(filePath.getParent()) ? filePath : filePath.getParent();
      if (file.isDirectory()) {
        return processDirectoryChecksum(filePath, directoryPathFromRelativize, fileOption, messageDigest);
      } else {
        return processSingleFileChecksum(filePath, directoryPathFromRelativize, messageDigest);
      }
    } catch (Exception e) {
      throw new FileProcessingException("Error while generating checksums", e);
    }
  }

  private static Map<String, String> processDirectoryChecksum(Path filePath,
                                                              Path directoryPathFromRelativize,
                                                              CommandFileOption fileOption,
                                                              MessageDigest messageDigest) throws IOException {
    var checksum = new HashMap<String, String>();
    var filePathsFromDirectory = getFilePathsFromDirectory(filePath);
    Path relativizePath;

    for (Path path : filePathsFromDirectory) {
      messageDigest.update(Files.readAllBytes(path));
      if (CommandFileOption.FILE_DETAILED.equals(fileOption)) {
        relativizePath = directoryPathFromRelativize.relativize(path);
        checksum.put(relativizePath.toString(),
          hexBinaryAdapter.marshal(messageDigest.digest()));
        messageDigest.reset();
      }
    }

    if (CommandFileOption.FILE.equals(fileOption)) {
      relativizePath = directoryPathFromRelativize.relativize(filePath);
      checksum.put(relativizePath.toString(), hexBinaryAdapter.marshal(messageDigest.digest()));
    }

    return checksum;
  }

  private static Map<String, String> processSingleFileChecksum(Path filePath,
                                                               Path directoryPathFromRelativize,
                                                               MessageDigest messageDigest) throws IOException {
    var checksum = new HashMap<String, String>();
    var filePathFromRelativize =
      Objects.isNull(directoryPathFromRelativize.getParent()) ? directoryPathFromRelativize
        : directoryPathFromRelativize.getParent();
    var relativizePath = filePathFromRelativize.relativize(filePath);
    messageDigest.update(Files.readAllBytes(filePath));
    checksum.put(relativizePath.toString(), hexBinaryAdapter.marshal(messageDigest.digest()));
    return checksum;
  }

  private static List<Path> getFilePathsFromDirectory(Path directoryPath) {
    List<Path> filePaths = Collections.emptyList();
    try (Stream<Path> pathStream = Files.find(directoryPath, Integer.MAX_VALUE,
        (filePath, fileAttr) -> fileAttr.isRegularFile())) {
      if (Objects.nonNull(pathStream)) {
        filePaths = pathStream.collect(Collectors.toList());
      }
      return filePaths;
    } catch (IOException e) {
      throw new FileProcessingException(
          String.format("Error processing files, path: %s", directoryPath), e);
    }
  }
}
