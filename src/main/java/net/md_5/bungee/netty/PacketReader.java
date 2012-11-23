package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.md_5.mendax.DataInputPacketReader;

public class PacketReader extends ByteToMessageDecoder<ByteBuf>
{

    @Override
    public ByteBuf decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception
    {
        DataInputPacketReader.readPacket(in);
        return in.slice(0, in.readerIndex());
    }
}
