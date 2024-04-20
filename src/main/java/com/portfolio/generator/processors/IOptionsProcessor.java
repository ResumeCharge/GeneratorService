package com.portfolio.generator.processors;

import com.portfolio.generator.models.ProcessorOptionsModel;
import com.portfolio.generator.models.staticsite.StaticSiteRequestModel;

public interface IOptionsProcessor {
  boolean isValid(
      ProcessorOptionsModel condition, StaticSiteRequestModel staticSiteRequestManager
  );
}
