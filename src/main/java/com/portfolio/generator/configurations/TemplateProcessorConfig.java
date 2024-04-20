package com.portfolio.generator.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.FileTemplateResolver;

@Configuration
public class TemplateProcessorConfig {
  @Bean
  public FileTemplateResolver templateResolver() {
    FileTemplateResolver templateResolver =
        new FileTemplateResolver();
    templateResolver.setPrefix("");
    templateResolver.setSuffix(".txt");
    templateResolver.setCharacterEncoding("UTF-8");
    return templateResolver;
  }

  @Bean
  public SpringTemplateEngine templateEngine() {
    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(templateResolver());
    return engine;
  }
}
