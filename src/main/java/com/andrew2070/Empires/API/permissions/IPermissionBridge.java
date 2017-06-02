package com.andrew2070.Empires.API.permissions;

import java.util.UUID;

public interface IPermissionBridge {

    boolean hasPermission(UUID uuid, String permission);

}