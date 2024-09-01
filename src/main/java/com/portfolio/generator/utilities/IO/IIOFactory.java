package com.portfolio.generator.utilities.IO;

import java.io.*;
import java.nio.file.Path;

public interface IIOFactory {
  FileOutputStream getFileOutputStream(final File file) throws FileNotFoundException;
  FileOutputStream createFileOutputStreamUsingFileUtils(final File file) throws IOException;
  BufferedReader getBufferedReader(final InputStream inputStream) throws FileNotFoundException;
  Path getPath(final String first, final String... more);
  void copyDirectory(final File inputDirectory, final File outputDirectory) throws IOException;
  void copyFile(final File inputFile, final File outputFile) throws IOException;
  boolean exists(final Path inputFilePath);
}
