package com.portfolio.generator.utilities.helpers;

import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;

import java.util.Collections;
import java.util.List;

public class StaticSiteRequestManagerHelper {

  public <T> List<T> getArrayFromResumeManager(
      final StaticSiteRequestModel staticSiteRequestManager, final String key
  ) {
    if (key.equals("WORK_EXPERIENCE")) {
      return (List<T>) staticSiteRequestManager.resume.getWorkExperienceList();
    }
    if (key.equals("PROJECT")) {
      return (List<T>) staticSiteRequestManager.resume.getProjectsList();
    }
    if (key.equals("SKILL")) {
      return (List<T>) staticSiteRequestManager.resume.getSkills();
    }
    return Collections.singletonList((T) "");
  }
}
