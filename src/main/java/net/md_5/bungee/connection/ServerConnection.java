package net.md_5.bungee.connection;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.KickException;
import net.md_5.bungee.Util;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.Packet9Respawn;
import net.md_5.bungee.packet.PacketCDClientStatus;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.packet.PacketFFKick;

/**
 * Class representing a connection from the proxy to the server; ie upstream.
 */
public class ServerConnection extends GenericConnection implements PacketHandler
{

    LoginState state = LoginState.HANDHSAKE;
    private static SecretKey secret = new SecretKeySpec(new byte[16], "AES");
    private final UserConnection user;

    public ServerConnection(Channel channel, UserConnection user)
    {
        super(channel);
        this.user = user;
    }

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
            case HANDHSAKE:
                PacketFDEncryptionRequest encryptRequest = new PacketFDEncryptionRequest(buf);
                PublicKey pub = EncryptionUtil.getPubkey(encryptRequest);
                PacketFCEncryptionResponse response = new PacketFCEncryptionResponse(EncryptionUtil.getShared(secret, pub), EncryptionUtil.encrypt(pub, encryptRequest.verifyToken));
                channel.write(response);
                break;
            case RESPONSE:
                if (id != 0xFC)
                {
                    throw new RuntimeException("Server did not send encryption enable");
                }
                EncryptionUtil.addCipher(channel, secret);

                for (ByteBuf custom : user.loginPackets)
                {
                    channel.write(custom);
                }

                channel.write(new PacketCDClientStatus((byte) 0));
                break;
            case LOGIN:
                if (id == 0xFF)
                {
                    throw new KickException("[Kicked] " + new PacketFFKick(buf).message);
                }
                Packet1Login login = new Packet1Login(buf);
                channel.write(new PacketFAPluginMessage("REGISTER", "RubberBand".getBytes()));

                if (user.server != null)
                {
                    channel.write(new Packet9Respawn((byte) 1, (byte) 0, (byte) 0, (short) 256, "DEFAULT"));
                    channel.write(new Packet9Respawn((byte) -1, (byte) 0, (byte) 0, (short) 256, "DEFAULT"));
                }

                if (user.server == null)
                {
                    user.clientEntityId = login.entityId;
                    user.serverEntityId = login.entityId;
                    channel.write(login);
                } else
                {

                    user.server.disconnect("Quitting");
                    user.serverEntityId = login.entityId;
                    channel.write(new Packet9Respawn(login.dimension, login.difficulty, login.gameMode, (short) 256, login.levelType));
                }
        }
    }
}
