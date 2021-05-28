package de.dytanic.cloudnet.ext.bridge.bungee;

import net.md_5.bungee.api.ProxyConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class BungeeServerRegisterHelper {

    private static final MethodHandle WATERFALL_ADD_SERVER;
    private static final MethodHandle WATERFALL_REMOVE_SERVER;
    // WaterDog needs a specific constructServerInfo method to enable raknet
    private static final MethodHandle WATER_DOG_CREATE_SERVER_INFO;

    static {
        // check if waterfall enhanced add/remove methods are available
        MethodHandle addServer = null;
        MethodHandle removeServer = null;
        try {
            addServer = MethodHandles.publicLookup().findVirtual(ProxyConfig.class, "addServer",
                    MethodType.methodType(ServerInfo.class, ServerInfo.class));
            removeServer = MethodHandles.publicLookup().findVirtual(ProxyConfig.class, "removeServerNamed",
                    MethodType.methodType(ServerInfo.class, String.class));
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
            // waterfall support is not available
        }
        // check if waterdog support is needed
        MethodHandle waterDogConstructServerInfo = null;
        try {
            waterDogConstructServerInfo = MethodHandles.publicLookup().findVirtual(ProxyServer.class,
                    "constructServerInfo",
                    MethodType.methodType(ServerInfo.class, String.class, SocketAddress.class,
                            String.class, boolean.class, boolean.class, String.class));
        } catch (NoSuchMethodException | IllegalAccessException ignored) {
            // not on water dog
        }
        // assign to class final members
        WATERFALL_ADD_SERVER = addServer;
        WATERFALL_REMOVE_SERVER = removeServer;
        WATER_DOG_CREATE_SERVER_INFO = waterDogConstructServerInfo;
    }

    private BungeeServerRegisterHelper() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull ServerInfo constructServerInfo(@NotNull String name, @NotNull InetSocketAddress address) {
        if (WATER_DOG_CREATE_SERVER_INFO != null) {
            try {
                return (ServerInfo) WATER_DOG_CREATE_SERVER_INFO.invoke(ProxyServer.getInstance(),
                        name, address, "CloudNet provided serverInfo", false, true, "default");
            } catch (Throwable throwable) {
                throw new IllegalStateException("Waterdog method present but unable to create server info", throwable);
            }
        } else {
            return ProxyServer.getInstance().constructServerInfo(name, address, "CloudNet provided serverInfo", false);
        }
    }

    public static void registerService(@NotNull String name, @NotNull InetSocketAddress address) {
        registerService(constructServerInfo(name, address));
    }

    public static void registerService(@NotNull ServerInfo serverInfo) {
        if (WATERFALL_ADD_SERVER != null) {
            try {
                WATERFALL_ADD_SERVER.invoke(ProxyServer.getInstance().getConfig(), serverInfo);
            } catch (Throwable throwable) {
                throw new IllegalStateException("WaterFall add server method present but unable to add server", throwable);
            }
        } else {
            ProxyServer.getInstance().getServers().put(serverInfo.getName(), serverInfo);
        }
    }

    public static void unregisterService(@NotNull String name) {
        if (WATERFALL_REMOVE_SERVER != null) {
            try {
                WATERFALL_REMOVE_SERVER.invoke(ProxyServer.getInstance().getConfig(), name);
            } catch (Throwable throwable) {
                throw new IllegalStateException("WaterFall remove server method present but unable to add server", throwable);
            }
        } else {
            ProxyServer.getInstance().getServers().remove(name);
        }
    }
}
