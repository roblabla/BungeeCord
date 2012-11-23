package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface PacketHandler
{

    public void disconnected(Channel channel);

    public void handle(Channel channel, ByteBuf buf);
}
