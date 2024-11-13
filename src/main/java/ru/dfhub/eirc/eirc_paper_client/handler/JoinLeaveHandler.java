package ru.dfhub.eirc.eirc_paper_client.handler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.dfhub.eirc.eirc_paper_client.client.DataParser;

public class JoinLeaveHandler implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        DataParser.handleOutputSession(true, e.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        DataParser.handleOutputSession(false, e.getPlayer().getName());
    }
}
