package com.andrew2070.Empires.API.permissions;

import com.andrew2070.Empires.API.permissions.PermissionLevel;

/**
 * Interface for permission management systems to implement
 */
public interface IPermissionProvider
{

    /**
     * Checks a permission
     * 
     * @param context
     *            The context where the permission is being checked in
     * @param permission
     *            The permission to check
     * @return Whether the permission is allowed
     */
    boolean checkPermission(PermissionContext context, String permission);

    /**
     * Notifies the permission manager about registered permissions
     * 
     * @param permission
     *            The name of the permission
     * @param level
     *            Default access level for the permission
     */
    void registerPermission(String permission, PermissionLevel level);

}