package net.md_5.bungee.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.connection.InitialHandler;

@ChannelHandler.Sharable
public class MinecraftPipeline extends ChannelInitializer
{

    public static MinecraftPipeline instance = new MinecraftPipeline();

    private MinecraftPipeline()
    {
    }

    @Override
    public void initChannel(Channel ch) throws Exception
    {
        ch.config().setOption(ChannelOption.IP_TOS, 0x18);
        ch.config().setOption(ChannelOption.TCP_NODELAY, true);
        ch.pipeline()
                .addLast("timer", new ReadTimeoutHandler(BungeeCord.instance.config.timeout))
                .addLast("decoder", new PacketReader())
                .addLast("manager", new GenericHandler(new InitialHandler()));
    }
}
