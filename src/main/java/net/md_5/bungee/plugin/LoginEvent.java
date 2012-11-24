package net.md_5.bungee.plugin;

import java.net.InetAddress;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.event.Cancellable;
import net.md_5.bungee.event.Event;
import net.md_5.bungee.event.HandlerList;

/**
 * Event called to represent a player logging in.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LoginEvent extends Event implements Cancellable
{

    private static final HandlerList handlers = new HandlerList();
    /**
     * Message to use when kicking if this event is canceled.
     */
    private String cancelReason;
    /**
     * Username which the player wishes to use.
     */
    private final String username;
    /**
     * IP address of the remote connection.
     */
    private final InetAddress address;
    /**
     * Hostname which the user tried to connect to.
     */
    private final String hostname;

    @Override
    public void setCancelled(boolean cancelled)
    {
        super.setCancelled(cancelled);
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }
}
