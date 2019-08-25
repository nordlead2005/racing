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
  START_RACE_NO_ENABLED("commands.start_race.error_no_enabled"),
  STOP_RACE_SUCCESS("commands.stop_race.success"),
  STOP_RACE_NOT_STARTED("commands.stop_race.error_not_started"),
  JOIN_RACE_SUCCESS("commands.join_race.success"),
  JOIN_RACE_CHARGED("commands.join_race.charged"),
  JOIN_RACE_NOT_OPEN("commands.join_race.error_not_open"),
  JOIN_RACE_IS_FULL("commands.join_race.error_is_full"),
  JOIN_RACE_IS_PARTICIPATING("commands.join_race.error_is_participating"),
  JOIN_RACE_IS_PARTICIPATING_OTHER("commands.join_race.error_is_participating_other"),
  JOIN_RACE_NOT_AFFORD("commands.join_race.error_not_afford"),
  RACE_SKIP_WAIT_NOT_STARTED("commands.race_skip_wait.error_not_started"),
  RELOAD_SUCCESS("commands.reload.success"),
  RELOAD_NOT_RACES("commands.reload.not_races"),
  RELOAD_NOT_LANGUAGE("commands.reload.not_language"),
  RACE_SET_STATE_SUCCESS("commands.race_set_state.success"),
  RACE_SET_STATE_NOCHANGE("commands.race_set_state.error_nochange"),
  RACE_SET_STATE_ONGOING("commands.race_set_state.error_ongoing"),
  RACE_HELP_TITLE("commands.race_help.title"),
  RACE_HELP_ITEM("commands.race_help.item"),
  RACE_SET_ENTRYFEE("commands.race_set_entryfee.success"),
  RACE_SET_WALKSPEED("commands.race_set_walkspeed.success"),
  RACE_ADD_POTION_EFFECT("commands.race_add_potion_effect.success"),
  RACE_REMOVE_POTION_EFFECT("commands.race_remove_potion_effect.success"),
  RACE_CLEAR_POTION_EFFECTS("commands.race_clear_potion_effects.success"),
  RACE_LEAVE_NOT_PARTICIPATING("commands.race_leave.error_not_participating"),
  RACE_LEAVE_SUCCESS("commands.race_leave.success"),
  RACE_LEAVE_BROADCAST("commands.race_leave.leave_broadcast"),
  RACE_LEAVE_PAYBACK("commands.race_leave.leave_payback"),
  RACE_INFO_SUCCESS("commands.race_info.success"),
  RACE_INFO_NO_POTION_EFFECTS("commands.race_info.no_potion_effects"),
  RACE_INFO_POTION_EFFECT("commands.race_info.potion_effect_item"),
  RACE_INFO_ENTRY_FEE_LINE("commands.race_info.entry_fee_line"),
  RACE_TOP_TYPE_FASTEST("commands.race_top.types.fastest"),
  RACE_TOP_TYPE_MOST_RUNS("commands.race_top.types.most_runs"),
  RACE_TOP_TYPE_MOST_WINS("commands.race_top.types.most_wins"),
  RACE_TOP_TYPE_WIN_RATIO("commands.race_top.types.win_ratio"),
  RACE_TOP_HEADER("commands.race_top.header"),
  RACE_TOP_ITEM("commands.race_top.item"),
  RACE_TOP_ITEM_NONE("commands.race_top.item_none"),
  RACE_RESET_TOP("commands.race_reset_top.success"),

  RACE_NOT_FOUND("validators.race_not_found"),
  RACE_ALREADY_EXIST("validators.race_already_exist"),
  CHECKPOINT_NOT_FOUND("validators.checkpoint_not_found"),
  CHECKPOINT_ALREADY_EXIST("validators.checkpoint_already_exist"),
  STARTPOINT_NOT_FOUND("validators.startpoint_not_found"),
  STARTPOINT_ALREADY_EXIST("validators.startpoint_already_exist"),
  TYPE_NOT_FOUND("validators.type_not_found"),
  STATE_NOT_FOUND("validators.state_not_found"),
  SONG_NOT_FOUND("validators.song_not_found"),
  VALIDATE_NON_INTEGER("validators.validate_non_integer"),
  VALIDATE_NON_NUMBER("validators.validate_non_number"),
  VALIDATE_MIN_EXCEED("validators.min_exceed"),
  VALIDATE_MAX_EXCEED("validators.max_exceed"),
  RACE_POTION_EFFECT_NOT_FOUND("validators.race_potion_effect_not_found"),
  POTION_EFFECT_NOT_FOUND("validators.potion_effect_not_found"),
  STAT_TYPE_NOT_FOUND("validators.stat_type_not_found"),

  RACE_CANCELED("race_canceled"),
  NOSHOW_DISQUALIFIED("race_start_noshow_disqualified"),
  QUIT_DISQUALIFIED("race_start_quit_disqualified"),
  DEATH_DISQUALIFIED("race_death_disqualified"),
  DEATH_DISQUALIFIED_TARGET("race_death_disqualified_target"),
  EDIT_NO_EDIT_MODE("edit_no_edit_mode"),
  RACE_WIN("race_win"),
  PARTICIPATE_CLICK_TEXT("race_participate_click_text"),
  PARTICIPATE_HOVER_TEXT("race_participate_hover_text"),
  PARTICIPATE_TEXT("race_participate_text"),
  PARTICIPATE_TEXT_FEE("race_participate_text_fee"),
  PARTICIPATE_DISCORD("race_participate_discord"),
  PARTICIPATE_DISCORD_FEE("race_participate_discord_fee"),
  PARTICIPATE_TEXT_TIMELEFT("race_participate_text_timeleft"),
  RACE_COUNTDOWN("race_countdown_subtitle"),
  RACE_NEXT_LAP("race_next_lap_actionbar"),
  RACE_FINAL_LAP("race_final_lap_actionbar"),
  RESPAWN_INTERACT_START("race_type_respawn_start_info"),
  RESPAWN_INTERACT_LAST("race_type_respawn_last_info"),
  SKIP_WAIT_HOVER_TEXT("race_skipwait_hover_text"),
  SKIP_WAIT_CLICK_TEXT("race_skipwait_click_text"),
  SKIP_WAIT("race_skipwait"),
  STOP_RACE_HOVER_TEXT("race_stop_hover_text"),
  STOP_RACE_CLICK_TEXT("race_stop_click_text"),
  STOP_RACE("race_stop"),
  SIGN_REGISTERED("race_sign_registered"),
  SIGN_UNREGISTERED("race_sign_unregistered"),
  RACE_SIGN_LINES("race_sign_lines"),
  SIGN_NOT_STARTED("race_sign_status_not_started"),
  SIGN_LOBBY("race_sign_status_lobby"),
  SIGN_STARTED("race_sign_status_in_game"),
  BLOCKED_CMDS("race_blocked_cmd"),

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
  TIME_UNIT_DAYS("timeunit.days"),
  TIME_UNIT_NOW("timeunit.now");

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
          throw new Error("All characters in a message identifier must be lowercase. Found " + key.getIdentifier());
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
