package com.github.hornta.race.objects;

import org.bukkit.entity.Horse;

public class HorseData {
  private Horse.Color color;
  private Horse.Style style;
  private int age;

  public HorseData(Horse horse) {
    this.color = horse.getColor();
    this.style = horse.getStyle();
    this.age = horse.getAge();
  }

  public Horse.Color getColor() {
    return color;
  }

  public int getAge() {
    return age;
  }

  public Horse.Style getStyle() {
    return style;
  }
}
