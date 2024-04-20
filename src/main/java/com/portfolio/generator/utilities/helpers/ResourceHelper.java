package com.portfolio.generator.utilities.helpers;

import com.portfolio.generator.utilities.IO.IOFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ResourceHelper implements IResourceHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceHelper.class);
  private final ResourceLoader resourceLoader;
  private final IOFactory ioFactory;

  public ResourceHelper(ResourceLoader resourceLoader,
                        IOFactory ioFactory) {
    this.resourceLoader = resourceLoader;
    this.ioFactory = ioFactory;
  }

  @Override
  public String getResourceAsString(String resourcePath) {
    final Resource resource = resourceLoader.getResource("classpath:" + resourcePath);
    try (final InputStream inputStream = resource.getInputStream();
         final BufferedReader reader = ioFactory.getBufferedReader(inputStream)) {
      final StringBuilder content = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line).append("\n");
      }
      return content.toString();
    } catch (IOException e) {
      LOGGER.error("Encountered exception trying to get resource as string", e);
      return null;
    }
  }
}
