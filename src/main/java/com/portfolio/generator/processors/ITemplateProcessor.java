package com.portfolio.generator.processors;

import com.portfolio.generator.utilities.exceptions.TemplateProcessingFailedException;

public interface ITemplateProcessor {
  <T> void processTemplate(
      T data, String thymeContextVariable, String inputLocation, String outputLocationPath
  ) throws TemplateProcessingFailedException;
}
