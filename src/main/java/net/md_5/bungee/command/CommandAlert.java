package net.md_5.bungee.command;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.ChatColor;
import net.md_5.bungee.Permission;
import net.md_5.bungee.connection.UserConnection;

public class CommandAlert extends Command
{

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if (getPermission(sender) != Permission.ADMIN)
        {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
            return;
        }
        if (args.length == 0)
        {
            sender.sendMessage(ChatColor.RED + "You must supply a message.");
        } else
        {
            StringBuilder builder = new StringBuilder();
            if (args[0].startsWith("&h"))
            {
                args[0] = args[0].substring(2);
            } else
            {
                builder.append(ChatColor.DARK_PURPLE);
                builder.append("[Alert] ");
            }

            for (String s : args)
            {
                builder.append(ChatColor.translateAlternateColorCodes('&', s));
                builder.append(" ");
            }

            String message = builder.substring(0, builder.length() - 1);
            for (UserConnection con : BungeeCord.instance.connections.values())
            {
                con.sendMessage(message);
            }
        }
    }
}
