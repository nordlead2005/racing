package com.github.hornta.race.message;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageManager {
  private static final Pattern placeholderPattern = Pattern.compile("<[a-z_]+>", Pattern.CASE_INSENSITIVE);
  private static LanguageTranslation languageTranslation;
  private static Map<String, String> placeholderValues = new HashMap<>();
  private static Map<String, MessageKey> placeholderKeys = new HashMap<>();

  public static void setLanguageTranslation(LanguageTranslation languageTranslation) {
    MessageManager.languageTranslation = languageTranslation;
  }

  static String transformPattern(String input) {
    return StringReplacer.replace(input, placeholderPattern, (Matcher m) -> {
      String placeholder = m.group().substring(1, m.group().length() - 1).toLowerCase(Locale.ENGLISH);
      if (placeholderValues.containsKey(placeholder)) {
        return placeholderValues.get(placeholder);
      } else if(placeholderKeys.containsKey(placeholder)) {
        return languageTranslation.getTranslation(placeholderKeys.get(placeholder));
      } else {
        return m.group();
      }
    });
  }

  public static void sendMessage(CommandSender commandSender, MessageKey key) {
    String message = languageTranslation.getTranslation(key);
    message = transformPlaceholders(message);
    commandSender.sendMessage(message);
  }

  private static String transformPlaceholders(String input) {
    String transformed = transformPattern(input);
    placeholderValues.clear();
    placeholderKeys.clear();

    return transformed;
  }

  public static void setValue(String key, MessageKey messageKey) {
    placeholderKeys.put(key, messageKey);
  }

  public static void setValue(String key, Object value) {
    if (value == null) {
      value = "";
    }

    placeholderValues.put(key.toLowerCase(Locale.ENGLISH), value.toString());
  }

  public static void broadcast(MessageKey key) {
    String message = languageTranslation.getTranslation(key);

    message = transformPlaceholders(message);

    Bukkit.broadcastMessage(message);
  }

  public static String getMessage(MessageKey key) {
    String message = languageTranslation.getTranslation(key);
    return transformPlaceholders(message);
  }
}
