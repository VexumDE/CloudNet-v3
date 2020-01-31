package de.dytanic.cloudnet.driver.permission;

import de.dytanic.cloudnet.common.INameable;
import de.dytanic.cloudnet.common.collection.Iterables;
import de.dytanic.cloudnet.common.document.gson.IJsonDocPropertyable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface IPermissible extends INameable, IJsonDocPropertyable, Comparable<IPermissible> {

    void setName(@NotNull String name);

    int getPotency();

    void setPotency(int potency);

    boolean addPermission(@NotNull Permission permission);

    boolean addPermission(@NotNull String group, @NotNull Permission permission);

    boolean removePermission(@NotNull String permission);

    boolean removePermission(@NotNull String group, @NotNull String permission);

    Collection<Permission> getPermissions();

    Map<String, Collection<Permission>> getGroupPermissions();

    @Nullable
    default Permission getPermission(String name) {
        if (name == null) {
            return null;
        }

        return Iterables.first(getPermissions(), permission -> permission.getName().equalsIgnoreCase(name));
    }

    default boolean isPermissionSet(@NotNull String name) {
        return Iterables.first(getPermissions(), permission -> permission.getName().equalsIgnoreCase(name)) != null;
    }

    default boolean addPermission(@NotNull String permission) {
        return addPermission(permission, 0);
    }

    default boolean addPermission(@NotNull String permission, boolean value) {
        return addPermission(new Permission(permission, value ? 1 : -1));
    }

    default boolean addPermission(@NotNull String permission, int potency) {
        return addPermission(new Permission(permission, potency));
    }

    default boolean addPermission(@NotNull String group, @NotNull String permission) {
        return addPermission(group, permission, 0);
    }

    default boolean addPermission(@NotNull String group, @NotNull String permission, int potency) {
        return addPermission(group, new Permission(permission, potency));
    }

    default boolean addPermission(@NotNull String group, @NotNull String permission, int potency, long time, TimeUnit millis) {
        return addPermission(group, new Permission(permission, potency, (System.currentTimeMillis() + millis.toMillis(time))));
    }

    default Collection<String> getPermissionNames() {
        return Iterables.map(getPermissions(), Permission::getName);
    }

    default PermissionCheckResult hasPermission(@NotNull Collection<Permission> permissions, @NotNull Permission permission) {

        Permission targetPerms = Iterables.first(permissions, perm -> perm.getName().equalsIgnoreCase(permission.getName()));

        if (targetPerms != null && permission.getName().equalsIgnoreCase(targetPerms.getName()) && targetPerms.getPotency() < 0) {
            return PermissionCheckResult.FORBIDDEN;
        }

        for (Permission permissionEntry : permissions) {

            if (permissionEntry.getName().equals("*") && (permissionEntry.getPotency() >= permission.getPotency() || getPotency() >= permission.getPotency())) {
                return PermissionCheckResult.ALLOWED;
            }

            if (permissionEntry.getName().endsWith("*") && permission.getName().contains(permissionEntry.getName().replace("*", ""))
                    && (permissionEntry.getPotency() >= permission.getPotency() || getPotency() >= permission.getPotency())) {
                return PermissionCheckResult.ALLOWED;
            }

            if (permission.getName().equalsIgnoreCase(permissionEntry.getName()) &&
                    (permissionEntry.getPotency() >= permission.getPotency() || getPotency() >= permission.getPotency())) {
                return PermissionCheckResult.ALLOWED;
            }
        }

        return PermissionCheckResult.DENIED;
    }

    default PermissionCheckResult hasPermission(@NotNull String group, @NotNull Permission permission) {
        return getGroupPermissions().containsKey(group) ? hasPermission(getGroupPermissions().get(group), permission) : PermissionCheckResult.DENIED;
    }

    default PermissionCheckResult hasPermission(@NotNull Permission permission) {
        return hasPermission(getPermissions(), permission);
    }

    default PermissionCheckResult hasPermission(@NotNull String permission) {
        return hasPermission(new Permission(permission, 0));
    }

    @Override
    default int compareTo(IPermissible o) {
        return getPotency() + o.getPotency();
    }
}