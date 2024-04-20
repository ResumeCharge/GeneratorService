package com.portfolio.generator.utilities.IO;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class IOFactory implements IIOFactory {
  public FileOutputStream getFileOutputStream(final File file) throws FileNotFoundException {
    return new FileOutputStream(file);
  }

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

}
