package com.github.hornta.race.objects;

public class DurationUnit {
  private final int amount;
  private final String singularis;
  private final String pluralis;

  public DurationUnit(int amount, String singularis, String pluralis) {
    this.amount = amount;
    this.singularis = singularis;
    this.pluralis = pluralis;
  }

  public int getAmount() {
    return amount;
  }

  public String getNumerus() {
    if(amount == 1) {
      return singularis;
    }

    return pluralis;
  }
}
