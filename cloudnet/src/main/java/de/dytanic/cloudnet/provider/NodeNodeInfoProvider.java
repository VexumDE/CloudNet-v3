package de.dytanic.cloudnet.provider;

import de.dytanic.cloudnet.CloudNet;
import de.dytanic.cloudnet.cluster.IClusterNodeServer;
import de.dytanic.cloudnet.command.Command;
import de.dytanic.cloudnet.command.DriverCommandSender;
import de.dytanic.cloudnet.common.Validate;
import de.dytanic.cloudnet.common.collection.Iterables;
import de.dytanic.cloudnet.common.command.CommandInfo;
import de.dytanic.cloudnet.common.concurrent.ITask;
import de.dytanic.cloudnet.driver.network.cluster.NetworkClusterNode;
import de.dytanic.cloudnet.driver.network.cluster.NetworkClusterNodeInfoSnapshot;
import de.dytanic.cloudnet.driver.provider.NodeInfoProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class NodeNodeInfoProvider implements NodeInfoProvider {

    private CloudNet cloudNet;

    public NodeNodeInfoProvider(CloudNet cloudNet) {
        this.cloudNet = cloudNet;
    }

    @Override
    public Collection<CommandInfo> getConsoleCommands() {
        return this.cloudNet.getCommandMap().getCommandInfos();
    }

    @Override
    public NetworkClusterNode[] getNodes() {
        return this.cloudNet.getConfig().getClusterConfig().getNodes().toArray(new NetworkClusterNode[0]);
    }

    @Nullable
    @Override
    public NetworkClusterNode getNode(@NotNull String uniqueId) {
        Validate.checkNotNull(uniqueId);

        if (uniqueId.equals(this.cloudNet.getConfig().getIdentity().getUniqueId())) {
            return this.cloudNet.getConfig().getIdentity();
        }
        return Iterables.first(this.cloudNet.getConfig().getClusterConfig().getNodes(), networkClusterNode -> networkClusterNode.getUniqueId().equals(uniqueId));
    }

    @Override
    public NetworkClusterNodeInfoSnapshot[] getNodeInfoSnapshots() {
        Collection<NetworkClusterNodeInfoSnapshot> nodeInfoSnapshots = Iterables.newArrayList();

        for (IClusterNodeServer clusterNodeServer : this.cloudNet.getClusterNodeServerProvider().getNodeServers()) {
            if (clusterNodeServer.isConnected()) {
                clusterNodeServer.getNodeInfoSnapshot();
                nodeInfoSnapshots.add(clusterNodeServer.getNodeInfoSnapshot());
            }
        }

        return nodeInfoSnapshots.toArray(new NetworkClusterNodeInfoSnapshot[0]);
    }

    @Nullable
    @Override
    public NetworkClusterNodeInfoSnapshot getNodeInfoSnapshot(@NotNull String uniqueId) {
        if (uniqueId.equals(this.cloudNet.getConfig().getIdentity().getUniqueId())) {
            return this.cloudNet.getCurrentNetworkClusterNodeInfoSnapshot();
        }

        for (IClusterNodeServer clusterNodeServer : this.cloudNet.getClusterNodeServerProvider().getNodeServers()) {
            if (clusterNodeServer.getNodeInfo().getUniqueId().equals(uniqueId) && clusterNodeServer.isConnected()) {
                clusterNodeServer.getNodeInfoSnapshot();
                return clusterNodeServer.getNodeInfoSnapshot();
            }
        }

        return null;
    }

    @Override
    public String[] sendCommandLine(@NotNull String commandLine) {
        Validate.checkNotNull(commandLine);

        Collection<String> collection = Iterables.newArrayList();

        if (this.cloudNet.isMainThread()) {
            this.sendCommandLine0(collection, commandLine);
        } else {
            try {
                this.cloudNet.runTask((Callable<Void>) () -> {
                    sendCommandLine0(collection, commandLine);
                    return null;
                }).get();
            } catch (InterruptedException | ExecutionException exception) {
                exception.printStackTrace();
            }
        }

        return collection.toArray(new String[0]);
    }

    @Override
    public String[] sendCommandLine(@NotNull String nodeUniqueId, @NotNull String commandLine) {
        Validate.checkNotNull(nodeUniqueId);
        Validate.checkNotNull(commandLine);

        if (this.cloudNet.getConfig().getIdentity().getUniqueId().equals(nodeUniqueId)) {
            return this.sendCommandLine(commandLine);
        }

        IClusterNodeServer clusterNodeServer = this.cloudNet.getClusterNodeServerProvider().getNodeServer(nodeUniqueId);

        if (clusterNodeServer != null && clusterNodeServer.isConnected() && clusterNodeServer.getChannel() != null) {
            return clusterNodeServer.sendCommandLine(commandLine);
        }

        return null;
    }

    private void sendCommandLine0(Collection<String> collection, String commandLine) {
        this.cloudNet.getCommandMap().dispatchCommand(new DriverCommandSender(collection), commandLine);
    }

    @Nullable
    @Override
    public CommandInfo getConsoleCommand(@NotNull String commandLine) {
        Command command = this.cloudNet.getCommandMap().getCommandFromLine(commandLine);
        return command != null ? command.getInfo() : null;
    }

    @Override
    public Collection<String> getConsoleTabCompleteResults(@NotNull String commandLine) {
        return this.cloudNet.getCommandMap().tabCompleteCommand(commandLine);
    }

    @Override
    public ITask<Collection<CommandInfo>> getConsoleCommandsAsync() {
        return this.cloudNet.scheduleTask(this::getConsoleCommands);
    }

    @Override
    public ITask<CommandInfo> getConsoleCommandAsync(@NotNull String commandLine) {
        return this.cloudNet.scheduleTask(() -> this.getConsoleCommand(commandLine));
    }

    @Override
    public ITask<Collection<String>> getConsoleTabCompleteResultsAsync(@NotNull String commandLine) {
        return this.cloudNet.scheduleTask(() -> this.getConsoleTabCompleteResults(commandLine));
    }

    @Override
    public ITask<String[]> sendCommandLineAsync(@NotNull String commandLine) {
        return this.cloudNet.scheduleTask(() -> this.sendCommandLine(commandLine));
    }

    @Override
    public ITask<String[]> sendCommandLineAsync(@NotNull String nodeUniqueId, @NotNull String commandLine) {
        return this.cloudNet.scheduleTask(() -> this.sendCommandLine(nodeUniqueId, commandLine));
    }

    @Override
    public ITask<NetworkClusterNode[]> getNodesAsync() {
        return this.cloudNet.scheduleTask(this::getNodes);
    }

    @Override
    public ITask<NetworkClusterNode> getNodeAsync(@NotNull String uniqueId) {
        Validate.checkNotNull(uniqueId);

        return this.cloudNet.scheduleTask(() -> this.getNode(uniqueId));
    }

    @Override
    public ITask<NetworkClusterNodeInfoSnapshot[]> getNodeInfoSnapshotsAsync() {
        return this.cloudNet.scheduleTask(this::getNodeInfoSnapshots);
    }

    @Override
    public ITask<NetworkClusterNodeInfoSnapshot> getNodeInfoSnapshotAsync(@NotNull String uniqueId) {
        Validate.checkNotNull(uniqueId);

        return this.cloudNet.scheduleTask(() -> this.getNodeInfoSnapshot(uniqueId));
    }
}
