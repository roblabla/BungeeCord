package net.md_5.bungee.connection;

import io.netty.channel.Channel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.Util;
import static net.md_5.bungee.Logger.$;

/**
 * Class to represent a Minecraft connection.
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public class GenericConnection
{

    public final Channel channel;
    public String username;

    /**
     * Close the socket with the specified reason.
     *
     * @param reason to disconnect
     */
    public void disconnect(String reason)
    {
        log("disconnected with " + reason);
        Util.kick(channel, reason);
    }

    /**
     * Log a message as an instance of this connection. Will include connected
     * address and username if applicable.
     *
     * @param message to log
     */
    public void log(String message)
    {
        $().info(channel.remoteAddress() + ((username == null) ? " " : " [" + username + "] ") + message);
    }
}
