package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import lombok.RequiredArgsConstructor;
import static net.md_5.bungee.Logger.$;

@RequiredArgsConstructor
public class GenericHandler extends ChannelInboundMessageHandlerAdapter<ByteBuf>
{

    private final PacketHandler handler;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        $().info(ctx.channel() + " has connected");

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        handler.disconnected(ctx.channel());
        $().info(ctx.channel() + " disconnected");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        if (ctx.channel().isActive())
        {
            ctx.channel().close();
            cause.printStackTrace();
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, ByteBuf msg) throws Exception
    {
        handler.handle(ctx.channel(), msg);
    }
}
