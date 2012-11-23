package net.md_5.bungee.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToByteCodec;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.crypto.BufferedBlockCipher;

@RequiredArgsConstructor
public class CipherCodec extends ByteToByteCodec
{

    private final BufferedBlockCipher encrypt;
    private final BufferedBlockCipher decrypt;

    @Override
    public void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        code(encrypt, in, out);
    }

    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception
    {
        code(decrypt, in, out);
    }

    private void code(BufferedBlockCipher cipher, ByteBuf in, ByteBuf out)
    {
        int readable = in.readableBytes();
        out.capacity(cipher.getUpdateOutputSize(readable));
        int processed = cipher.processBytes(in.array(), 0, readable, out.array(), 0);
        in.readerIndex(readable);
        out.writerIndex(processed);
    }
}
