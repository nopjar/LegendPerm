package net.playlegend.configuration;

import java.util.Map;
import java.util.Objects;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

public class MessageConfig implements Configuration {

    private static final String PATH_UNEXPECTED_ERROR = "unexpected_error";
    private static final String PATH_ONLY_PLAYER = "only_player";
    private static final String PATH_NOT_PERMITTED = "not_permitted";
    private static final String PATH_BROADCAST_ONLINE = "broadcast_online";
    private static final String PATH_BROADCAST_OFFLINE = "broadcast_offline";

    private static final String PATH_GROUP_DOES_NOT_EXIST = "group_does_not_exist";
    private static final String PATH_GROUP_DOES_ALREADY_CONTAIN_PERMISSION = "group_does_already_contain_permission";
    private static final String PATH_GROUP_ADDED_PERMISSION = "group_added_permission";
    private static final String PATH_GROUP_ALREADY_EXISTS = "group_already_exists";
    private static final String PATH_GROUP_DELETED = "group_deleted";
    private static final String PATH_GROUP_CREATED = "group_created";
    private static final String PATH_GROUP_UPDATED = "group_updated";
    private static final String PATH_GROUP_INFO = "group_info";
    private static final String PATH_GROUP_UNKNOWN_PROPERTY = "group_unknown_property";
    private static final String PATH_GROUP_DOES_NOT_CONTAIN_PERMISSION = "group_does_not_contain_permission";
    private static final String PATH_GROUP_PERMISSION_REVOKED = "group_permission_revoked";
    private static final String PATH_GROUP_LIST = "group_list";
    private static final String PATH_GROUP_PROTECTED = "group_protected";

    private static final String PATH_UNKNOWN_USER = "unknown_user";
    private static final String PATH_USER_ALREADY_PERMANENT_IN_GROUP = "user_already_permanent_in_group";
    private static final String PATH_USER_DOES_NOT_HAVE_GROUP = "user_does_not_have_group";
    private static final String PATH_USER_ADDED_PERMANENT_TO_GROUP = "user_added_permanent_to_group";
    private static final String PATH_USER_ADDED_TEMPORARY_TO_GROUP = "user_added_temporary_to_group";
    private static final String PATH_USER_INFO = "user_info";
    private static final String PATH_USER_INFO_GROUP_LINE_PERMANENT = "user_info_group_line_permanent";
    private static final String PATH_USER_INFO_GROUP_LINE_TEMPORARY = "user_info_group_line_temporary";
    private static final String PATH_USER_REMOVED_FROM_GROUP = "user_removed_from_group";
    private static final String PATH_USER_GROUP_TIME_REDUCED = "user_group_time_reduced";
    private static final String PATH_USER_CANT_REDUCE_GROUP_AS_PERMANENT = "user_cant_reduce_group_as_permanent";

    @NotNull public final Message unexpectedError;
    @NotNull public final Message notPermitted;
    @NotNull public final Message onlyPlayer;
    @NotNull public final Message broadcast_online;
    @NotNull public final Message broadcast_offline;

    @NotNull public final Message groupDoesNotExist;
    @NotNull public final Message groupDoesAlreadyContainPermission;
    @NotNull public final Message groupAddedPermission;
    @NotNull public final Message groupAlreadyExists;
    @NotNull public final Message groupDeleted;
    @NotNull public final Message groupCreated;
    @NotNull public final Message groupUpdated;
    @NotNull public final Message groupInfo;
    @NotNull public final Message groupUnknownProperty;
    @NotNull public final Message groupDoesNotContainPermission;
    @NotNull public final Message groupPermissionRevoked;
    @NotNull public final Message groupList;
    @NotNull public final Message groupProtected;

    @NotNull public final Message unknownUser;
    @NotNull public final Message userAlreadyPermanentInGroup;
    @NotNull public final Message userDoesNotHaveGroup;
    @NotNull public final Message userAddedPermanentToGroup;
    @NotNull public final Message userAddedTemporaryToGroup;
    @NotNull public final Message userInfo;
    @NotNull public final Message userInfoGroupLinePermanent;
    @NotNull public final Message userInfoGroupLineTemporary;
    @NotNull public final Message userRemovedFromGroup;
    @NotNull public final Message userGroupTimeReduced;
    @NotNull public final Message userCantReduceGroupAsPermanent;

    public MessageConfig(@NotNull YamlConfiguration yaml) {
        this.unexpectedError = new Message(yaml.getString(PATH_UNEXPECTED_ERROR, ""));
        this.notPermitted = new Message(yaml.getString(PATH_NOT_PERMITTED, ""));
        this.onlyPlayer = new Message(yaml.getString(PATH_ONLY_PLAYER, ""));
        this.broadcast_online = new Message(yaml.getString(PATH_BROADCAST_ONLINE, ""));
        this.broadcast_offline = new Message(yaml.getString(PATH_BROADCAST_OFFLINE, ""));

        this.groupDoesNotExist = new Message(yaml.getString(PATH_GROUP_DOES_NOT_EXIST, ""));
        this.groupDoesAlreadyContainPermission = new Message(yaml.getString(PATH_GROUP_DOES_ALREADY_CONTAIN_PERMISSION, ""));
        this.groupAddedPermission = new Message(yaml.getString(PATH_GROUP_ADDED_PERMISSION, ""));
        this.groupAlreadyExists = new Message(yaml.getString(PATH_GROUP_ALREADY_EXISTS, ""));
        this.groupDeleted = new Message(yaml.getString(PATH_GROUP_DELETED, ""));
        this.groupCreated = new Message(yaml.getString(PATH_GROUP_CREATED, ""));
        this.groupUpdated = new Message(yaml.getString(PATH_GROUP_UPDATED, ""));
        this.groupInfo = new Message(yaml.getString(PATH_GROUP_INFO, ""));
        this.groupUnknownProperty = new Message(yaml.getString(PATH_GROUP_UNKNOWN_PROPERTY, ""));
        this.groupDoesNotContainPermission = new Message(yaml.getString(PATH_GROUP_DOES_NOT_CONTAIN_PERMISSION, ""));
        this.groupPermissionRevoked = new Message(yaml.getString(PATH_GROUP_PERMISSION_REVOKED, ""));
        this.groupList = new Message(yaml.getString(PATH_GROUP_LIST, ""));
        this.groupProtected = new Message(yaml.getString(PATH_GROUP_PROTECTED, ""));

        this.unknownUser = new Message(yaml.getString(PATH_UNKNOWN_USER, ""));
        this.userAlreadyPermanentInGroup = new Message(yaml.getString(PATH_USER_ALREADY_PERMANENT_IN_GROUP, ""));
        this.userDoesNotHaveGroup = new Message(yaml.getString(PATH_USER_DOES_NOT_HAVE_GROUP, ""));
        this.userAddedPermanentToGroup = new Message(yaml.getString(PATH_USER_ADDED_PERMANENT_TO_GROUP, ""));
        this.userAddedTemporaryToGroup = new Message(yaml.getString(PATH_USER_ADDED_TEMPORARY_TO_GROUP, ""));
        this.userInfo = new Message(yaml.getString(PATH_USER_INFO, ""));
        this.userInfoGroupLinePermanent = new Message(yaml.getString(PATH_USER_INFO_GROUP_LINE_PERMANENT, ""));
        this.userInfoGroupLineTemporary = new Message(yaml.getString(PATH_USER_INFO_GROUP_LINE_TEMPORARY, ""));
        this.userRemovedFromGroup = new Message(yaml.getString(PATH_USER_REMOVED_FROM_GROUP, ""));
        this.userGroupTimeReduced = new Message(yaml.getString(PATH_USER_GROUP_TIME_REDUCED, ""));
        this.userCantReduceGroupAsPermanent = new Message(yaml.getString(PATH_USER_CANT_REDUCE_GROUP_AS_PERMANENT, ""));
    }


    public class Message {

        private final String string;

        public Message(String s) {
            this.string = s;
        }

        public String parse(Map<String, Object> replacements) {
            String result = string;
            for (Map.Entry<String, Object> entry : replacements.entrySet()) {
                result = result.replaceAll("<" + entry.getKey() + ">", Objects.toString(entry.getValue(), ""));
            }
            return result;
        }

        public String get() {
            return string;
        }
    }

}
