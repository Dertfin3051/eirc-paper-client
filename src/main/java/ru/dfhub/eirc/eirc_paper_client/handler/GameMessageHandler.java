package ru.dfhub.eirc.eirc_paper_client.handler;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.dfhub.eirc.eirc_paper_client.client.DataParser;

public class GameMessageHandler implements Listener {

    @EventHandler
    public void onMessage(AsyncChatEvent e) {
        DataParser.handleOutputMessage(
                ((TextComponent) e.message()).content(),
                e.getPlayer().getName()
        );
    }
}
