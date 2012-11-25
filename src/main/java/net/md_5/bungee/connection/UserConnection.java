package net.md_5.bungee.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EntityMap;
import net.md_5.bungee.Util;
import net.md_5.bungee.command.CommandSender;
import net.md_5.bungee.netty.MinecraftPipeline;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.packet.Packet;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.Packet3Chat;
import net.md_5.bungee.packet.PacketC9PlayerListItem;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.plugin.ServerConnectEvent;

public class UserConnection extends GenericConnection implements CommandSender
{

    private static final AttributeKey<String> ATTR_SERVER = new AttributeKey("server");
    public final Packet2Handshake handshake;
    public Queue<Packet> packetQueue = new ConcurrentLinkedQueue<>();
    public List<ByteBuf> loginPackets = new ArrayList<>();
    ServerConnection server;
    private UpstreamBridge upBridge;
    private DownstreamBridge downBridge;
    // reconnect stuff
    int clientEntityId;
    int serverEntityId;
    // ping stuff
    private int trackingPingId;
    private long pingTime;
    public int ping;

    public UserConnection(Channel channel, Packet2Handshake handshake, List<ByteBuf> loginPackets)
    {
        super(channel);
        this.handshake = handshake;
        username = handshake.username;
        this.loginPackets = loginPackets;
        BungeeCord.instance.connections.put(username, this);
        BungeeCord.instance.tabListHandler.onJoin(this);
    }

    public void connect(String server)
    {
        ServerConnectEvent event = new ServerConnectEvent(this.server == null, this, server);
        event.setNewServer(server);
        BungeeCord.instance.pluginManager.callEvent(event);
        if (event.getMessage() != null)
        {
            sendMessage(event.getMessage());
        }
        if (event.getNewServer() == null)
        {
            if (event.isFirstTime())
            {
                event.setNewServer(BungeeCord.instance.config.defaultServerName);
            } else
            {
                return;
            }
        }
        InetSocketAddress addr = BungeeCord.instance.config.getServer(event.getNewServer());
        new Bootstrap().channel(NioSocketChannel.class).handler(MinecraftPipeline.instance).group(BungeeCord.instance.eventGroup).remoteAddress(addr).connect().channel().write(handshake).channel().attr(ATTR_SERVER).set(server);
    }

    @Override
    public String getName()
    {
        return username;
    }

    @Override
    public void disconnect(String reason)
    {
        BungeeCord.instance.tabListHandler.onDisconnect(this);
        super.disconnect(reason);
    }

    @Override
    public void sendMessage(String message)
    {
        packetQueue.add(new Packet3Chat(message));
    }

    private class UpstreamBridge implements PacketHandler
    {

        @Override
        public void disconnected(Channel channel) throws Exception
        {
        }

        @Override
        public void handle(Channel channel, ByteBuf buf) throws Exception
        {
            int id = Util.getId(buf);
            if (id == 0x00)
            {
                if (buf.getInt(2) == trackingPingId)
                {
                    ping = (int) (System.currentTimeMillis() - pingTime);
                    BungeeCord.instance.tabListHandler.onPingChange(UserConnection.this, ping);
                }
            } else if (id == 0x03)
            {
                Packet3Chat chat = new Packet3Chat(buf);
                if (chat.message.startsWith("/") && BungeeCord.instance.dispatchCommand(chat.message.substring(1), UserConnection.this))
                {
                    return;
                }
            }

            EntityMap.rewrite(buf, clientEntityId, serverEntityId);
            server.channel.write(buf);
        }
    }

    private class DownstreamBridge implements PacketHandler
    {

        @Override
        public void disconnected(Channel channel) throws Exception
        {
        }

        @Override
        public void handle(Channel channel, ByteBuf buf) throws Exception
        {
            int id = Util.getId(buf);
            if (id == 0x00)
            {
                trackingPingId = buf.getInt(2);
                pingTime = System.currentTimeMillis();
            } else if (id == 0xC9)
            {
                if (!BungeeCord.instance.tabListHandler.onPacketC9(UserConnection.this, new PacketC9PlayerListItem(buf)))
                {
                    return;
                }
            } else if (id == 0xFA)
            {
                PacketFAPluginMessage message = new PacketFAPluginMessage(buf);
                if (message.tag.equals("BungeeCord|Jump"))
                {
                    connect(new String(message.data));
                }
            }

            while (!packetQueue.isEmpty())
            {
                Packet p = packetQueue.poll();
                if (p != null)
                {
                    channel.write(p);
                }
            }

            EntityMap.rewrite(buf, serverEntityId, clientEntityId);
            channel.write(buf);
        }
    }
}
