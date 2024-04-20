package com.portfolio.generator.processors;

import com.portfolio.generator.models.ActionResultModel;
import com.portfolio.generator.models.ActionsModel;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;
import com.portfolio.generator.utilities.exceptions.ActionProcessingFailedException;

public interface IActionProcessor {
  ActionResultModel processAction(
      ActionsModel action, StaticSiteRequestModel staticSiteRequestManager
  ) throws ActionProcessingFailedException;
}
