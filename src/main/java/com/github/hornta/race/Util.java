package com.github.hornta.race;

import com.github.hornta.race.message.MessageKey;
import com.github.hornta.race.message.MessageManager;
import com.github.hornta.race.objects.DurationUnit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Util {
  private static final double HALF_RIGHT_ANGLE = 45;
  private static final int SECONDS_IN_ONE_DAY = 86400;
  private static final int SECONDS_IN_ONE_HOUR = 3600;
  private static final int SECONDS_IN_ONE_MINUTE = 60;
  private static final int MAX_DURATION_UNITS = 2;

  public static Location snapAngles(Location location) {
    location.setPitch((float) (HALF_RIGHT_ANGLE * (Math.round(location.getPitch() / HALF_RIGHT_ANGLE))));
    location.setYaw((float) (HALF_RIGHT_ANGLE * (Math.round(location.getYaw() / HALF_RIGHT_ANGLE))));
    return location;
  }

  public static Location centerOnBlockHorizontally(Location location) {
    Location newLoc = location.clone();
    newLoc.setX(newLoc.getBlockX() + 0.5);
    newLoc.setZ(newLoc.getBlockZ() + 0.5);
    return newLoc;
  }

  public static Location centerOnBlock(Location location) {
    Location newLoc = location.clone();
    newLoc.setX(newLoc.getBlockX() + 0.5);
    newLoc.setY(newLoc.getBlockY() + 0.5);
    newLoc.setZ(newLoc.getBlockZ() + 0.5);
    return newLoc;
  }

  public static String getFilenameWithoutExtension(File file) {
    String filename = file.getName();
    int lastDotIndex = filename.lastIndexOf('.');

    if(lastDotIndex == -1) {
      return filename;
    }

    return filename.substring(0, lastDotIndex);
  }

  public static float randomRangeFloat(float min, float max) {
    return (float)((Math.random() < 0.5) ? ((1.0 - Math.random()) * (max - min) + min) : (Math.random() * (max - min) + min));
  }

  public static int randomRangeInt(int min, int max) {
    return (int)((Math.random() < 0.5) ? ((1.0 - Math.random()) * (max - min + 1) + min) : (Math.random() * (max - min + 1) + min));
  }

  private static byte[] createChecksum(InputStream input) throws NoSuchAlgorithmException, IOException {
    byte[] buffer = new byte[1024];
    MessageDigest complete = MessageDigest.getInstance("MD5");
    int numRead;
    do {
      numRead = input.read(buffer);
      if (numRead > 0) {
        complete.update(buffer, 0, numRead);
      }
    } while (numRead != -1);
    input.close();
    return complete.digest();
  }

  public static String getMD5Checksum(InputStream input) throws NoSuchAlgorithmException, IOException {
    StringBuilder stringBuilder = new StringBuilder();
    for(byte b : createChecksum(input)) {
      stringBuilder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
    }
    return stringBuilder.toString();
  }

  public static String getTimeLeft(Duration duration) {
    return getTimeLeft((int)duration.getSeconds());
  }

  public static String getTimeLeft(int duration) {
    if(duration == 0) {
      return null;
    }

    int days = duration / SECONDS_IN_ONE_DAY;
    int hours = (duration % SECONDS_IN_ONE_DAY) / SECONDS_IN_ONE_HOUR;
    int minutes = ((duration % SECONDS_IN_ONE_DAY) % SECONDS_IN_ONE_HOUR) / SECONDS_IN_ONE_MINUTE;
    int seconds = ((duration % SECONDS_IN_ONE_DAY) % SECONDS_IN_ONE_HOUR) % SECONDS_IN_ONE_MINUTE;

    List<DurationUnit> units = new ArrayList<>();
    units.add(new DurationUnit(days, "<day>", "<days>"));
    units.add(new DurationUnit(hours, "<hour>", "<hours>"));
    units.add(new DurationUnit(minutes, "<minute>", "<minutes>"));
    units.add(new DurationUnit(seconds, "<second>", "<seconds>"));

    StringBuilder stringBuilder = new StringBuilder();

    int count = 0;

    for(DurationUnit unit : units) {
      int amount = unit.getAmount();

      // make sure that we never get a format like 1 hour, 4 seconds, e.g. no skipping unit
      if(count != 0 && amount == 0) {
        break;
      }

      if(amount == 0) {
        continue;
      }

      stringBuilder.append(amount);
      stringBuilder.append(" ");
      stringBuilder.append(unit.getNumerus());
      stringBuilder.append(", ");

      count += 1;
      if(count == MAX_DURATION_UNITS) {
        break;
      }
    }

    String string = stringBuilder.toString();

    // remove last space and comma
    return string.substring(0, string.length() - 2);
  }

  public static void setTimeUnitValues() {
    MessageManager.setValue("second", MessageKey.TIME_UNIT_SECOND);
    MessageManager.setValue("seconds", MessageKey.TIME_UNIT_SECONDS);
    MessageManager.setValue("minute", MessageKey.TIME_UNIT_MINUTE);
    MessageManager.setValue("minutes", MessageKey.TIME_UNIT_MINUTES);
    MessageManager.setValue("hour", MessageKey.TIME_UNIT_HOURS);
    MessageManager.setValue("hours", MessageKey.TIME_UNIT_HOURS);
    MessageManager.setValue("day", MessageKey.TIME_UNIT_DAY);
    MessageManager.setValue("days", MessageKey.TIME_UNIT_DAYS);
  }
}
