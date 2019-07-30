package com.github.hornta.race.message;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MessageManager.class)
public class MessageManagerTest {

  @Test
  public void transformPatternReplacePlaceholder() {
    MessageManager.setValue("foo", "va");
    String result = MessageManager.transformPattern("ja<foo>");
    Assert.assertEquals("java", result);
  }

  @Test
  public void transformPatternReplaceWithDelimiter() {
    MessageManager.setValue("foo", Arrays.asList("j", "a", "v", "a"));
    String result = MessageManager.transformPattern("<foo|delimiter: \\n>!");
    Assert.assertEquals("j\\na\\nv\\na!", result);
  }
}