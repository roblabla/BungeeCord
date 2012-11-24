package net.md_5.bungee.plugin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.md_5.bungee.connection.UserConnection;
import net.md_5.bungee.event.Event;
import net.md_5.bungee.event.HandlerList;

/**
 * Event called when the decision is made to decide which server to connect to.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ServerConnectEvent extends Event
{

    private static final HandlerList handlers = new HandlerList();
    /**
     * If the player currently has no server, this is true
     */
    private final boolean firstTime;
    /**
     * Message to send just before the change. null for no message
     */
    private String message;
    /**
     * User in question.
     */
    private final UserConnection connection;
    /**
     * Name of the server they are connecting to.
     */
    private final String server;
    /**
     * Name of the server which they will be forwarded to instead.
     */
    private String newServer;

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }
}
