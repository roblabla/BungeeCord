package net.md_5.bungee.event;

public interface EventExecutor
{

    public void execute(Event event) throws EventException;
}
