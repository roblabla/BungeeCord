package net.md_5.bungee.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Delegate;
import net.md_5.bungee.Util;

public abstract class Packet implements ByteBuf
{

    @Delegate
    private final ByteBuf buf;

    public Packet(int id)
    {
        this.buf = Unpooled.buffer();
        this.buf.writeByte(id);
    }

    public Packet(int id, ByteBuf buf)
    {
        if (buf.readUnsignedByte() != id)
        {
            throw new IllegalArgumentException("Wasn't expecting packet id " + Util.hex(id));
        }
        this.buf = buf;
    }

    public String readString()
    {
        short len = readShort();
        char[] chars = new char[len];
        for (int i = 0; i < len; i++)
        {
            chars[i] = readChar();
        }
        return new String(chars);
    }

    public void writeString(String s)
    {
        writeShort(s.length());
        for (char c : s.toCharArray())
        {
            writeChar(c);
        }
    }

    public byte[] readArray()
    {
        short len = readShort();
        byte[] ret = new byte[len];
        readBytes(ret);
        return ret;
    }

    public void writeArray(byte[] b)
    {
        writeShort(b.length);
        writeBytes(b);
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}
