package com.portfolio.generator.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProcessorOptionsModelTest {

  @Test
  void test() {
    final ProcessorOptionsModel processorOptions = new ProcessorOptionsModel();
    processorOptions.setOptionValue("value");
    processorOptions.setOptionType(OptionType.REQUIRED);
    processorOptions.setOptionKey("key");
    assertThat(processorOptions.getOptionKey()).isEqualTo("key");
    assertThat(processorOptions.getOptionValue()).isEqualTo("value");
    assertThat(processorOptions.getOptionType()).isEqualTo(OptionType.REQUIRED);
  }

}