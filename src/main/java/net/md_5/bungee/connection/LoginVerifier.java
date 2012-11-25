package net.md_5.bungee.connection;

import net.md_5.bungee.connection.InitialHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.KickException;
import net.md_5.bungee.Util;
import net.md_5.bungee.packet.PacketFCEncryptionResponse;
import net.md_5.bungee.plugin.LoginEvent;

@RequiredArgsConstructor
public class LoginVerifier implements Runnable
{

    private final Channel channel;
    private final InitialHandler handler;
    private final ByteBuf packet;

    @Override
    public void run()
    {
        try
        {
            PacketFCEncryptionResponse response = new PacketFCEncryptionResponse(packet);
            SecretKey shared = EncryptionUtil.getSecret(response, handler.encryptionRequest);
            if (!EncryptionUtil.isAuthenticated(handler.handshake.username, handler.encryptionRequest.serverId, shared))
            {
                throw new KickException("Not authenticated with minecraft.net");
            }

            // fire post auth event
            LoginEvent event = new LoginEvent(handler.handshake.username, ((InetSocketAddress) channel.remoteAddress()).getAddress(), handler.handshake.host);
            BungeeCord.instance.pluginManager.callEvent(event);
            if (event.isCancelled())
            {
                throw new KickException(event.getCancelReason());
            }

            channel.write(new PacketFCEncryptionResponse());
            EncryptionUtil.addCipher(channel, shared);
            handler.state = LoginState.LOGIN;
        } catch (KickException ex)
        {
            Util.kick(channel, ex.getMessage());
        } catch (Exception ex)
        {
            Util.kick(channel, "[Auth Error] " + Util.exception(ex));
        }
    }
}
