package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface PacketHandler
{

    public void disconnected(Channel channel) throws Exception;

    public void handle(Channel channel, ByteBuf buf) throws Exception;
}
