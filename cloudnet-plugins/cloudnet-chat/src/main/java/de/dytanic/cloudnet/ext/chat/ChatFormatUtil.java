package de.dytanic.cloudnet.ext.chat;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.permission.IPermissionGroup;
import de.dytanic.cloudnet.driver.permission.IPermissionUser;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ChatFormatUtil {

    private ChatFormatUtil() {
        throw new UnsupportedOperationException();
    }

    public static String buildFormat(@NotNull UUID playerUniqueId, @NotNull String playerName, @NotNull String playerDisplayName,
                                     @NotNull String format, @NotNull String message,
                                     @NotNull Function<String, Boolean> permissionChecker, @NotNull BiFunction<Character, String, String> colourReplacer) {
        IPermissionUser user = CloudNetDriver.getInstance().getPermissionManagement().getUser(playerUniqueId);
        if (user == null) {
            return null;
        }

        String finalMessage = permissionChecker.apply("cloudnet.chat.color")
                ? colourReplacer.apply('&', message.replace("%", "%%"))
                : message.replace("%", "%%");
        if (finalMessage.trim().isEmpty()) {
            return null;
        }

        IPermissionGroup group = CloudNetDriver.getInstance().getPermissionManagement().getHighestPermissionGroup(user);
        format = format
                .replace("%name%", playerName)
                .replace("%display%", playerDisplayName)
                .replace("%uniqueId%", playerUniqueId.toString())
                .replace("%group%", group == null ? "" : group.getName())
                .replace("%display%", group == null ? "" : group.getDisplay())
                .replace("%prefix%", group == null ? "" : group.getPrefix())
                .replace("%suffix%", group == null ? "" : group.getSuffix())
                .replace("%color%", group == null ? "" : group.getColor());
        return colourReplacer.apply('&', format).replace("%message%", finalMessage);
    }
}
