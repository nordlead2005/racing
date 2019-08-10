package com.github.hornta.race;

import com.github.hornta.race.message.MessageManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MessageManager.class)
public class UtilTest {

  @Test
  public void testGetTimeLeft() {
    String result = Util.getTimeLeft(60 * 1000 + 1500);
    Assert.assertEquals("1 <minute>, 1.5 <seconds>", result);
  }
}
