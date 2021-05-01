package de.dytanic.cloudnet.ext.chat;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;

public class NukkitCloudNetChatPlugin extends PluginBase implements Listener {

    private String format;

    @Override
    public void onEnable() {
        super.saveDefaultConfig();
        this.format = super.getConfig().getString("format", "%display%%name% &8:&f %message%");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handle(PlayerChatEvent event) {
        String format = ChatFormatUtil.buildFormat(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                event.getPlayer().getDisplayName(),
                this.format,
                event.getMessage(),
                event.getPlayer()::hasPermission,
                (colorChar, message) -> TextFormat.colorize(colorChar, message)
        );
        if (format == null) {
            event.setCancelled(true);
        } else {
            event.setFormat(format);
        }
    }
}
