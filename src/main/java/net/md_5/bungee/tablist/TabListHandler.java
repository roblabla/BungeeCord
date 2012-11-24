package net.md_5.bungee.tablist;

import net.md_5.bungee.connection.UserConnection;
import net.md_5.bungee.packet.PacketC9PlayerListItem;

public interface TabListHandler
{

    public void onJoin(UserConnection con);

    public void onServerChange(UserConnection con);

    public void onPingChange(UserConnection con, int ping);

    public void onDisconnect(UserConnection con);

    /**
     *
     * @param con
     * @param packet
     * @return if we handled it
     */
    public boolean onPacketC9(UserConnection con, PacketC9PlayerListItem packet);
}
