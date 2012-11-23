package net.md_5.bungee;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.netty.PacketHandler;
import net.md_5.bungee.packet.Packet1Login;
import net.md_5.bungee.packet.PacketCDClientStatus;
import net.md_5.bungee.packet.PacketFAPluginMessage;
import net.md_5.bungee.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.packet.PacketFDEncryptionRequest;
import net.md_5.bungee.packet.PacketFFKick;

/**
 * Class representing a connection from the proxy to the server; ie upstream.
 */
@RequiredArgsConstructor
public class ServerConnection implements PacketHandler
{

     enum State
    {

        RESPONSE, ACTIVATE, LOGIN;
    }
    State state = State.RESPONSE;
    private static SecretKey secret = new SecretKeySpec(new byte[16], "AES");
    private final UserConnection user;

    @Override
    public void disconnected(Channel channel)
    {
    }

    @Override
    public void handle(Channel channel, ByteBuf buf)
    {
        try
        {
            int id = Util.getId(buf);
            switch (state)
            {
                case RESPONSE:
                    PacketFDEncryptionRequest encryptRequest = new PacketFDEncryptionRequest(buf);
                    PublicKey pub = EncryptionUtil.getPubkey(encryptRequest);
                    PacketFCEncryptionResponse response = new PacketFCEncryptionResponse(EncryptionUtil.getShared(secret, pub), EncryptionUtil.encrypt(pub, encryptRequest.verifyToken));
                    channel.write(response);
                    break;
                case ACTIVATE:
                    if (id != 0xFC)
                    {
                        throw new RuntimeException("Server did not send encryption enable");
                    }
                    Util.addCipher(channel, secret);

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
                    break;
            }
        } catch (Exception ex)
        {
        }
    }
}
