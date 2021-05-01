package de.dytanic.cloudnet.ext.chat;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(
        id = "cloudnet_chat",
        name = "CloudNet-Chat",
        version = "1.0",
        url = "https://cloudnetservice.eu"
)
public class SpongeCloudNetChatPlugin {

    @Inject
    @DefaultConfig(sharedRoot = false)
    public Path defaultConfigPath;

    private String chatFormat;

    @Listener
    public void handle(GameInitializationEvent event) {
        ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                .setPath(this.defaultConfigPath)
                .build();
        try {
            CommentedConfigurationNode configurationNode = loader.load();
            if (Files.notExists(this.defaultConfigPath)) {
                Files.createFile(this.defaultConfigPath);
                // set defaults in config
                configurationNode.getNode("format").setValue("%display%%name% &8:&f %message%");
                loader.save(configurationNode);
            }

            CommentedConfigurationNode format = configurationNode.getNode("format");
            if (format.isVirtual()) {
                // the node is not set in the config/was removed
                format.setValue("%display%%name% &8:&f %message%");
                loader.save(configurationNode);
            }

            this.chatFormat = format.getString();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Listener
    public void handle(MessageChannelEvent.Chat event) {
        event.getCause().first(Player.class).ifPresent(player -> {
            String format = ChatFormatUtil.buildFormat(
                    player.getUniqueId(),
                    player.getName(),
                    player.getDisplayNameData().displayName().get().toPlain(),
                    this.chatFormat,
                    event.getRawMessage().toPlain(),
                    player::hasPermission,
                    (colorChar, message) -> TextSerializers.FORMATTING_CODE.replaceCodes(message, colorChar)
            );
            if (format == null) {
                event.setCancelled(true);
            } else {
                event.setMessage(Text.of(format));
            }
        });
    }
}
