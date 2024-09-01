package com.portfolio.generator.processors;

import com.portfolio.generator.utilities.exceptions.TemplateProcessingFailedException;

import java.io.FileWriter;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Component
public class TemplateProcessor implements ITemplateProcessor {
    private final SpringTemplateEngine templateEngine;

    public TemplateProcessor(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public <T> void processTemplate(
            T data, String thymeContextVariable, String inputLocation, String outputLocationPath
    ) throws TemplateProcessingFailedException {
        try (FileWriter fileWriter = new FileWriter(outputLocationPath)) {
            Context thymeContext = new Context();
            thymeContext.setVariable(thymeContextVariable, data);
            templateEngine.process(inputLocation, thymeContext, fileWriter);
        } catch (Exception e) {
            throw new TemplateProcessingFailedException(
                    String.format("Failed to process template at location: %s", inputLocation), e);
        }
    }
}
