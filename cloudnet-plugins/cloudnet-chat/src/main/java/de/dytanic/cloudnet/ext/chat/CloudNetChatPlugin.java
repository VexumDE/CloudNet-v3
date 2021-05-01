package de.dytanic.cloudnet.ext.chat;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CloudNetChatPlugin extends JavaPlugin implements Listener {

    private String format;

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        this.format = this.getConfig().getString("format");

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void handleChat(AsyncPlayerChatEvent event) {
        String format = ChatFormatUtil.buildFormat(
                event.getPlayer().getUniqueId(),
                event.getPlayer().getName(),
                event.getPlayer().getDisplayName(),
                this.format,
                event.getMessage(),
                event.getPlayer()::hasPermission,
                (colorChar, message) -> ChatColor.translateAlternateColorCodes(colorChar, message)
        );
        if (format == null) {
            event.setCancelled(true);
        } else {
            event.setFormat(format);
        }
    }
}
