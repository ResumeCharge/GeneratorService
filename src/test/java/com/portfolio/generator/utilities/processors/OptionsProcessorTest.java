package com.portfolio.generator.utilities.processors;

import com.portfolio.generator.models.OptionType;
import com.portfolio.generator.models.ProcessorOptionsModel;
import com.portfolio.generator.models.ResumeModel;
import com.portfolio.generator.models.resumeModels.WorkExperienceModel;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.processors.IOptionsProcessor;
import com.portfolio.generator.processors.OptionsProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OptionsProcessorTest {

  private IOptionsProcessor optionsProcessor;

  @BeforeEach
  void setUp() {
    optionsProcessor = new OptionsProcessor();
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void isValid() {
    final ProcessorOptionsModel processorOptions = new ProcessorOptionsModel();
    processorOptions.setOptionType(OptionType.REQUIRED);
    processorOptions.setOptionValue("WORK_EXPERIENCE");
    final List<WorkExperienceModel> workExperienceList = new ArrayList<>();
    final WorkExperienceModel workExperience = new WorkExperienceModel();
    workExperienceList.add(workExperience);
    final ResumeModel resumeModel = new ResumeModel();
    resumeModel.setWorkExperienceList(workExperienceList);
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
        .setResume(resumeModel)
        .build();
    assertThat(optionsProcessor.isValid(processorOptions, request)).isTrue();
  }

  @Test
  void isValidEmptyWorkExperience() {
    final StaticSiteRequestModel request = new StaticSiteRequestModel.Builder()
        .setResume(new ResumeModel())
        .build();
    final ProcessorOptionsModel processorOptions = new ProcessorOptionsModel();
    processorOptions.setOptionType(OptionType.REQUIRED);
    processorOptions.setOptionValue("WORK_EXPERIENCE");
    assertThat(optionsProcessor.isValid(processorOptions, request)).isFalse();
  }
}