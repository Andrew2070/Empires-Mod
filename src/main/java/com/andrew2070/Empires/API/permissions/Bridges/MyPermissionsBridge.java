package com.andrew2070.Empires.API.permissions.Bridges;


import com.andrew2070.Empires.API.permissions.IPermissionBridge;
import com.andrew2070.Empires.Config.GroupConfig;
import com.andrew2070.Empires.Config.UserConfig;
import com.andrew2070.Empires.Handlers.Constants;
import com.andrew2070.Empires.entities.Permissions.Group;
import com.andrew2070.Empires.entities.Permissions.User;

import java.util.UUID;

public class MyPermissionsBridge implements IPermissionBridge {

    private static final String DEFAULT_GROUP_NAME = "default";

    public final Group.Container groups = new Group.Container();
    public final User.Container users = new User.Container();

    public final GroupConfig groupConfig = new GroupConfig(Constants.CONFIG_FOLDER + "GroupConfig.json", this);
    public final UserConfig userConfig = new UserConfig(Constants.CONFIG_FOLDER + "UserConfig.json", this);

    public MyPermissionsBridge() {
    }

    public void loadConfigs() {
        groups.clear();
        users.clear();

        groupConfig.init(groups);
        userConfig.init(users);
    }

    public void saveConfigs() {
        groupConfig.write(groups);
        userConfig.write(users);
    }

    public void saveGroups() {
        groupConfig.write(groups);
    }

    public void saveUsers() {
        userConfig.write(users);
    }

    @Override
    public boolean hasPermission(UUID uuid, String permission) {
        User user = users.get(uuid);

        return user != null && user.hasPermission(permission);
    }
}