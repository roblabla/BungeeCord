package net.md_5.bungee;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.packet.Packet2Handshake;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.plugin.LoginEvent;

@RequiredArgsConstructor
public class InitialHandler implements PacketHandler
{

    enum State
    {

        HANDSHAKE, RESPONSE, LOGIN;
    }
    State state = State.HANDSHAKE;
    Packet2Handshake handshake;
    PacketFDEncryptionRequest encryptionRequest;
    private List<ByteBuf> customPackets;

    @Override
    public void disconnected(Channel channel) throws Exception
    {
    }

    @Override
    public void handle(Channel channel, ByteBuf buf) throws Exception
    {

        int id = Util.getId(buf);
        switch (state)
        {
            case HANDSHAKE:
                switch (id)
                {
                    case 0x02:
                        handshake = new Packet2Handshake(buf);
                        LoginEvent event = new LoginEvent(handshake.username, ((InetSocketAddress) channel.remoteAddress()).getAddress(), handshake.host);
                        BungeeCord.instance.pluginManager.onHandshake(event);
                        if (event.isCancelled())
                        {
                            throw new KickException(event.getCancelReason());
                        }

                        encryptionRequest = EncryptionUtil.encryptRequest();
                        channel.write(encryptionRequest);
                        state = State.RESPONSE;
                        break;
                    case 0xFE:
                        Configuration conf = BungeeCord.instance.config;
                        String message = ChatColor.COLOR_CHAR + "1"
                                + "\00" + BungeeCord.PROTOCOL_VERSION
                                + "\00" + BungeeCord.GAME_VERSION
                                + "\00" + conf.motd
                                + "\00" + BungeeCord.instance.connections.size()
                                + "\00" + conf.maxPlayers;
                        throw new KickException(message);
                    default:
                        throw new KickException("Was not prepared to deal with packet " + Util.hex(id) + " at " + state);
                }
                break;
            case RESPONSE:
                BungeeCord.instance.threadPool.submit(new LoginVerifier(channel, this, buf));
                break;
            case LOGIN:
                if (buf.readUnsignedByte() != 0xCD)
                {
                    if (customPackets == null)
                    {
                        customPackets = new ArrayList<>();
                    }
                    customPackets.add(buf);
                } else
                {
                    throw new UnsupportedOperationException();
                }
                break;
        }
    }
}
