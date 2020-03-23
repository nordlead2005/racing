package com.github.hornta.race;

import com.github.hornta.carbon.message.MessageManager;
import com.github.hornta.race.objects.DurationUnit;

import org.bukkit.Location;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Util {
  private static final double HALF_RIGHT_ANGLE = 45;
  private static final int MS_IN_ONE_DAY = 86400 * 1000;
  private static final int MS_IN_ONE_HOUR = 3600 * 1000;
  private static final int MS_IN_ONE_MINUTE = 60 * 1000;
  private static final long MS_IN_ONE_SECOND = 1000;
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

  public static int randomRangeInt(int min, int max) {
    if (min > max) {
      throw new IllegalArgumentException("min must not be greater than max");
    }

    Random r = new Random();
    return r.nextInt((max - min) + 1) + min;
  }

  public static String getTimeLeft(int duration) {
    return getTimeLeft((long) duration);
  }

  public static String getTimeLeft(long duration) {
    if(duration == 0) {
      return null;
    }

    DecimalFormat df = new DecimalFormat("#.#");
    df.setRoundingMode(RoundingMode.CEILING);

    long days = duration / MS_IN_ONE_DAY;
    long hours = (duration % MS_IN_ONE_DAY) / MS_IN_ONE_HOUR;
    long minutes = ((duration % MS_IN_ONE_DAY) % MS_IN_ONE_HOUR) / MS_IN_ONE_MINUTE;
    long seconds = (((duration % MS_IN_ONE_DAY) % MS_IN_ONE_HOUR) % MS_IN_ONE_MINUTE);

    List<DurationUnit> units = new ArrayList<>();
    units.add(new DurationUnit(days, "<day>", "<days>"));
    units.add(new DurationUnit(hours, "<hour>", "<hours>"));
    units.add(new DurationUnit(minutes, "<minute>", "<minutes>"));
    units.add(new DurationUnit(seconds, "<second>", "<seconds>"));

    StringBuilder stringBuilder = new StringBuilder();

    int count = 0;

    for(DurationUnit unit : units) {
      long amount = unit.getAmount();

      // make sure that we never get a format like 1 hour, 4 seconds, e.g. no skipping unit
      if(count != 0 && amount == 0) {
        break;
      }

      if(amount == 0) {
        continue;
      }

      if(unit == units.get(units.size() - 1)) {
        stringBuilder.append(df.format(amount / (float)MS_IN_ONE_SECOND));
      } else {
        stringBuilder.append(amount);
      }

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
