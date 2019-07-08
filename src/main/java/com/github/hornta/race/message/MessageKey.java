package com.github.hornta.race.message;

import java.util.HashSet;
import java.util.Set;

public enum MessageKey {
  CREATE_RACE_SUCCESS("commands.create_race.success"),
  CREATE_RACE_NAME_OCCUPIED("commands.create_race.error_name_occupied"),
  DELETE_RACE_SUCCESS("commands.delete_race.success"),
  CHANGE_RACE_NAME_SUCCESS("commands.change_race_name.success"),
  RACE_ADD_CHECKPOINT_SUCCESS("commands.race_add_checkpoint.success"),
  RACE_ADD_CHECKPOINT_IS_OCCUPIED("commands.race_add_checkpoint.error_is_occupied"),
  RACE_DELETE_CHECKPOINT_SUCCESS("commands.race_delete_checkpoint.success"),
  RACE_ADD_STARTPOINT_SUCCESS("commands.race_add_startpoint.success"),
  RACE_ADD_STARTPOINT_IS_OCCUPIED("commands.race_add_startpoint.error_is_occupied"),
  RACE_DELETE_STARTPOINT_SUCCESS("commands.race_delete_startpoint.success"),
  RACE_SPAWN_NOT_ENABLED("commands.race_spawn.error_not_enabled"),
  RACE_SET_SPAWN_SUCCESS("commands.race_set_spawn.success"),
  LIST_RACES_LIST("commands.list_races.race_list"),
  LIST_RACES_ITEM("commands.list_races.race_list_item"),
  RACE_SET_TYPE_SUCCESS("commands.race_set_type.success"),
  RACE_SET_TYPE_NOCHANGE("commands.race_set_type.error_nochange"),
  RACE_SET_SONG_SUCCESS("commands.race_set_song.success"),
  RACE_SET_SONG_NOCHANGE("commands.race_set_song.error_nochange"),
  RACE_UNSET_SONG_SUCCESS("commands.race_unset_song.success"),
  RACE_UNSET_SONG_ALREADY_UNSET("commands.race_unset_song.error_already_unset"),
  START_RACE_ALREADY_STARTED("commands.start_race.error_already_started"),
  START_RACE_MISSING_STARTPOINT("commands.start_race.error_missing_startpoint"),
  START_RACE_MISSING_CHECKPOINT("commands.start_race.error_missing_checkpoint"),
  START_RACE_MISSING_CHECKPOINTS("commands.start_race.error_missing_checkpoints"),
  START_RACE_NOT_ENABLED("commands.start_race.error_not_enabled"),
  STOP_RACE_SUCCESS("commands.stop_race.success"),
  STOP_RACE_NOT_STARTED("commands.stop_race.error_not_started"),
  JOIN_RACE_SUCCESS("commands.join_race.success"),
  JOIN_RACE_NOT_OPEN("commands.join_race.error_not_open"),
  JOIN_RACE_IS_FULL("commands.join_race.error_is_full"),
  JOIN_RACE_IS_PARTICIPATING("commands.join_race.error_is_participating"),
  JOIN_RACE_IS_PARTICIPATING_OTHER("commands.join_race.error_is_participating_other"),
  RACE_SKIP_WAIT_NOT_STARTED("commands.race_skip_wait.error_not_started"),
  RELOAD_SUCCESS("commands.reload.success"),
  RELOAD_NOT_RACES("commands.reload.not_races"),
  RACE_SET_STATE_SUCCESS("commands.race_set_state.success"),
  RACE_SET_STATE_NOCHANGE("commands.race_set_state.error_nochange"),
  RACE_SET_STATE_ONGOING("commands.race_set_state.error_ongoing"),
  RACE_HELP_TITLE("commands.race_help.title"),
  RACE_HELP_ITEM("commands.race_help.item"),

  RACE_NOT_FOUND("validators.race_not_found"),
  RACE_ALREADY_EXIST("validators.race_already_exist"),
  CHECKPOINT_NOT_FOUND("validators.checkpoint_not_found"),
  CHECKPOINT_ALREADY_EXIST("validators.checkpoint_already_exist"),
  STARTPOINT_NOT_FOUND("validators.startpoint_not_found"),
  STARTPOINT_ALREADY_EXIST("validators.startpoint_already_exist"),
  TYPE_NOT_FOUND("validators.type_not_found"),
  STATE_NOT_FOUND("validators.state_not_found"),
  SONG_NOT_FOUND("validators.song_not_found"),
  VALIDATE_INTEGER_NON_INTEGER("validators.validate_integer.non_integer"),
  VALIDATE_INTEGER_MIN_EXCEED("validators.validate_integer.min_exceed"),
  VALIDATE_INTEGER_MAX_EXCEED("validators.validate_integer.max_exceed"),

  RACE_CANCELED("race_canceled"),
  NOSHOW_DISQUALIFIED("race_start_noshow_disqualified"),
  QUIT_DISQULIAFIED("race_start_quit_disqualified"),
  EDIT_NO_EDIT_MODE("edit_no_edit_mode"),
  RACE_WIN("race_win"),
  PARTICIPATE_CLICK_TEXT("race_participate_click_text"),
  PARTICIPATE_HOVER_TEXT("race_participate_hover_text"),
  PARTICIPATE_TEXT("race_participate_text"),
  PARTICIPATE_TEXT_TIMELEFT("race_participate_text_timeleft"),
  RACE_COUNTDOWN("race_countdown_subtitle"),
  RACE_NEXT_LAP("race_next_lap_actionbar"),
  RACE_FINAL_LAP("race_final_lap_actionbar"),

  NO_PERMISSION_COMMAND("no_permission_command"),
  MISSING_ARGUMENTS_COMMAND("missing_arguments_command"),
  COMMAND_NOT_FOUND("command_not_found"),

  TIME_UNIT_SECOND("timeunit.second"),
  TIME_UNIT_SECONDS("timeunit.seconds"),
  TIME_UNIT_MINUTE("timeunit.minute"),
  TIME_UNIT_MINUTES("timeunit.minutes"),
  TIME_UNIT_HOUR("timeunit.hour"),
  TIME_UNIT_HOURS("timeunit.hours"),
  TIME_UNIT_DAY("timeunit.day"),
  TIME_UNIT_DAYS("timeunit.days");

  private static final Set<String> identifiers = new HashSet<>();
  static {
    for (MessageKey key : MessageKey.values()) {
      if(key.getIdentifier() == null || key.getIdentifier().isEmpty()) {
        throw new Error("A message identifier can't be null or empty.");
      }

      for(char character : key.name().toCharArray()) {
        if(Character.getType(character) == Character.LOWERCASE_LETTER) {
          throw new Error("All characters in a message key must be uppercase");
        }
      }

      for(char character : key.getIdentifier().toCharArray()) {
        if(Character.getType(character) == Character.UPPERCASE_LETTER) {
          throw new Error("All characters in a message identifier must be lowercase");
        }
      }

      if(identifiers.contains(key.getIdentifier())) {
        throw new Error("Duplicate identifier `" + key.getIdentifier() + "` found in MessageKey");
      }

      identifiers.add(key.getIdentifier());
    }
  }

  private String identifier;

  MessageKey(String identifier) {
    this.identifier = identifier;
  }

  public static boolean hasIdentifier(String identifier) {
    return identifiers.contains(identifier);
  }

  public String getIdentifier() {
    return identifier;
  }
}
