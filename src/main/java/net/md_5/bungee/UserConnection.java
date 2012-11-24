package net.md_5.bungee;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.md_5.bungee.command.CommandSender;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.packet.Packet;
import net.md_5.bungee.packet.Packet0KeepAlive;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.Packet3Chat;
import net.md_5.bungee.packet.PacketC9PlayerListItem;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.plugin.ServerConnectEvent;

public class UserConnection extends GenericConnection implements CommandSender
{

    public final Packet2Handshake handshake;
    public Queue<Packet> packetQueue = new ConcurrentLinkedQueue<>();
    public List<ByteBuf> loginPackets = new ArrayList<>();
    ServerConnection server;
    private UpstreamBridge upBridge;
    private DownstreamBridge downBridge;
    // reconnect stuff
    int clientEntityId;
    int serverEntityId;
    volatile boolean reconnecting;
    // ping stuff
    private int trackingPingId;
    private long pingTime;
    private int ping;

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
        BungeeCord.instance.pluginManager.onServerConnect(event);
        if (event.getMessage() != null)
        {
            this.sendMessage(event.getMessage());
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
        connect(server, addr);
    }

    private void connect(String name, InetSocketAddress serverAddr)
    {
    }

    public SocketAddress getAddress()
    {
        return channel.remoteAddress();
    }

    public int getPing()
    {
        return ping;
    }

    private void setPing(int ping)
    {
        BungeeCord.instance.tabListHandler.onPingChange(this, ping);
        this.ping = ping;
    }

    private void destroySelf(String reason)
    {
        if (BungeeCord.instance.isRunning)
        {
            BungeeCord.instance.connections.remove(username);
        }
        disconnect(reason);
        if (server != null)
        {
            server.disconnect("Quitting");
            BungeeCord.instance.config.setServer(this, server.name);
        }
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

    @Override
    public String getName()
    {
        return username;
    }

    private class UpstreamBridge implements PacketHandler
    {

        @Override
        public void disconnected(Channel channel) throws Exception
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void handle(Channel channel, ByteBuf buf) throws Exception
        {
            boolean sendPacket = true;

            int id = Util.getId(buf);
            if (id == 0x03)
            {
                Packet3Chat chat = new Packet3Chat(buf);
                String message = chat.message;
                if (message.startsWith("/"))
                {
                    sendPacket = !BungeeCord.instance.dispatchCommand(message.substring(1), UserConnection.this);
                }
            } else if (id == 0x00)
            {
                if (trackingPingId == new Packet0KeepAlive(buf).id)
                {
                    setPing((int) (System.currentTimeMillis() - pingTime));
                }
            }

            EntityMap.rewrite(buf, clientEntityId, serverEntityId);
            if (sendPacket)
            {
                server.GenericConnection.this.channel.write(buf);
            }
        }
    }

    private class DownstreamBridge implements PacketHandler
    {

        @Override
        public void disconnected(Channel channel) throws Exception
        {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void handle(Channel channel, ByteBuf buf) throws Exception
        {
            int id = Util.getId(buf);
            if (id == 0xFA)
            {
                PacketFAPluginMessage message = new PacketFAPluginMessage(buf);
                if (message.tag.equals("RubberBand"))
                {
                    String server = new String(message.data);
                    connect(server);
                    return;
                }
            } else if (id == 0x00)
            {
                trackingPingId = new Packet0KeepAlive(buf).id;
                pingTime = System.currentTimeMillis();
            } else if (id == 0xC9)
            {
                if (!BungeeCord.instance.tabListHandler.onPacketC9(UserConnection.this, new PacketC9PlayerListItem(buf)))
                {
                    return;
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
