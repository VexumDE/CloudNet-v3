package de.dytanic.cloudnet.wrapper.provider;

import de.dytanic.cloudnet.common.Validate;
import de.dytanic.cloudnet.common.document.gson.JsonDocument;
import de.dytanic.cloudnet.driver.network.def.packet.PacketClientServerChannelMessage;
import de.dytanic.cloudnet.driver.provider.CloudMessenger;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.driver.service.ServiceTask;
import de.dytanic.cloudnet.wrapper.Wrapper;
import org.jetbrains.annotations.NotNull;

public class WrapperMessenger implements CloudMessenger {

    private Wrapper wrapper;

    public WrapperMessenger(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public void sendChannelMessage(@NotNull String channel, @NotNull String message, @NotNull JsonDocument data) {
        Validate.checkNotNull(channel);
        Validate.checkNotNull(message);
        Validate.checkNotNull(data);

        this.wrapper.getNetworkClient().sendPacket(new PacketClientServerChannelMessage(channel, message, data));
    }

    @Override
    public void sendChannelMessage(@NotNull ServiceInfoSnapshot targetServiceInfoSnapshot, @NotNull String channel, @NotNull String message, @NotNull JsonDocument data) {
        Validate.checkNotNull(targetServiceInfoSnapshot);
        Validate.checkNotNull(channel);
        Validate.checkNotNull(message);
        Validate.checkNotNull(data);

        this.wrapper.getNetworkClient().sendPacket(new PacketClientServerChannelMessage(targetServiceInfoSnapshot.getServiceId().getUniqueId(), channel, message, data));
    }

    @Override
    public void sendChannelMessage(@NotNull ServiceTask targetServiceTask, @NotNull String channel, @NotNull String message, @NotNull JsonDocument data) {
        Validate.checkNotNull(targetServiceTask);
        Validate.checkNotNull(channel);
        Validate.checkNotNull(message);
        Validate.checkNotNull(data);

        this.wrapper.getNetworkClient().sendPacket(new PacketClientServerChannelMessage(targetServiceTask.getName(), channel, message, data));
    }
}
