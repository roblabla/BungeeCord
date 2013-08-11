package net.md_5.bungee.api.config;

import java.net.InetSocketAddress;
import java.util.Collection;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.tab.TabListHandler;

/**
 * Class used to represent a server to connect to.
 */
public interface ServerInfo
{

    /**
     * Get the name of this server.
     *
     * @return the configured name for this server address
     */
    String getName();

    /**
     * Gets the connectable host + port pair for this server. Implementations
     * expect this to be used as the unique identifier per each instance of this
     * class.
     *
     * @return the IP and port pair for this server
     */
    InetSocketAddress getAddress();

    /**
     * Get the set of all players on this server.
     *
     * @return an unmodifiable collection of all players on this server
     */
    Collection<ProxiedPlayer> getPlayers();

    /**
     * Returns the MOTD which should be used when this server is a forced host.
     *
     * @return the motd
     */
    String getMotd();

    /**
     * Returns whether the current players in motd should represent all players
     * connected to proxy or just to that server.
     * 
     * @return 
     */
    MotdCount getMotdCount();

    /**
     * Returns the max amount of slots displayed on the ping page.
     * 
     * @returns the max amount of players
     */
    int getMaxPlayers();

    /**
     * Class used to build tab lists for this player.
     */
    Class<? extends TabListHandler> getTabList();

    /**
     * Whether the player can access this server. It will only return false when
     * the player has no permission and this server is restricted.
     *
     * @param sender the player to check access for
     * @return whether access is granted to this server
     */
    boolean canAccess(CommandSender sender);

    /**
     * Send data by any available means to this server.
     *
     * @param channel the channel to send this data via
     * @param data the data to send
     */
    void sendData(String channel, byte[] data);

    /**
     * Asynchronously gets the current player count on this server.
     *
     * @param callback the callback to call when the count has been retrieved.
     */
    void ping(Callback<ServerPing> callback);

    public enum MotdCount {
        GLOBAL, SERVER
    }
}
