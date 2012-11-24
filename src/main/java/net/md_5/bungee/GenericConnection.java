package net.md_5.bungee;

import io.netty.channel.Channel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import static net.md_5.bungee.Logger.$;

/**
 * Class to represent a Minecraft connection.
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public class GenericConnection
{

    protected final Channel channel;
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

    public void log(String message)
    {
        $().info(channel.remoteAddress() + ((username == null) ? " " : " [" + username + "] ") + message);
    }
}
