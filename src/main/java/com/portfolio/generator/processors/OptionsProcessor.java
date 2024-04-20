package com.portfolio.generator.processors;

import com.portfolio.generator.models.OptionType;
import com.portfolio.generator.models.ProcessorOptionsModel;
import com.portfolio.generator.models.resumeModels.CareerSummaryModel;
import com.portfolio.generator.models.resumeModels.EducationModel;
import com.portfolio.generator.models.resumeModels.ProjectModel;
import com.portfolio.generator.models.resumeModels.WorkExperienceModel;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OptionsProcessor implements IOptionsProcessor {

  @Override
  public boolean isValid(
      final ProcessorOptionsModel option, final StaticSiteRequestModel staticSiteRequestManager
  ) {
    if (option.getOptionType() == OptionType.REQUIRED) {
      if (option.getOptionValue().equals("WORK_EXPERIENCE")) {
        final List<WorkExperienceModel> workExperienceList =
            staticSiteRequestManager.resume.getWorkExperienceList();
        return workExperienceList != null && !workExperienceList.isEmpty();
      }
      if (option.getOptionValue().equals("PROJECT")) {
        final List<ProjectModel> projectList = staticSiteRequestManager.resume.getProjectsList();
        return projectList != null && !projectList.isEmpty();
      }
      if (option.getOptionValue().equals("EDUCATION")) {
        final List<EducationModel> educationList =
            staticSiteRequestManager.resume.getEducationList();
        return educationList != null && !educationList.isEmpty();
      }
      if (option.getOptionValue().equals("CAREER_SUMMARY")) {
        final CareerSummaryModel careerSummary = staticSiteRequestManager.resume.getCareerSummary();
        return careerSummary != null && careerSummary.getSummary() != null;
      }
      if (option.getOptionValue().equals("EXTRA_DETAILS")) {
        final Map<String, String> extraDetailsMap =
            staticSiteRequestManager.resume.getExtraDetails();
        return extraDetailsMap != null && extraDetailsMap.containsKey(option.getOptionKey());
      }
    }
    return false;
  }
}
