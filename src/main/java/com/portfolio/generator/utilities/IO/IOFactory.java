package com.portfolio.generator.utilities.IO;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class IOFactory implements IIOFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(IOFactory.class);

  @Override
  public FileOutputStream createFileOutputStreamUsingFileUtils(File file) throws IOException {
    return FileUtils.openOutputStream(file);
  }

  @Override
  public BufferedReader getBufferedReader(final InputStream inputStream) {
    return new BufferedReader(new InputStreamReader(inputStream));
  }

  @Override
  public Path getPath(final String first, final String... more) {
    return Paths.get(first, more);
  }

  @Override
  public void copyDirectory(final File inputDirectory, final File outputDirectory) throws IOException {
    FileUtils.copyDirectory(inputDirectory, outputDirectory);
  }

  @Override
  public void copyFile(final File inputFile,final File outputFile) throws IOException {
    FileUtils.copyFile(inputFile, outputFile);
  }

  @Override
  public boolean exists(final Path inputFilePath) {
    return Files.exists(inputFilePath);
  }

  @Override
  public String readFileAsString(final File inputFile) {
    try (final InputStream inputStream = new FileInputStream(inputFile);
         final BufferedReader reader = this.getBufferedReader(inputStream)) {
      final StringBuilder content = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line).append("\n");
      }
      return content.toString();
    } catch (IOException e) {
      LOGGER.error("Encountered exception trying to get file as string", e);
      return null;
    }
  }
}
