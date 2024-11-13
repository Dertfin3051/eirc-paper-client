package ru.dfhub.eirc.eirc_paper_client;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.dfhub.eirc.eirc_paper_client.client.DataParser;
import ru.dfhub.eirc.eirc_paper_client.client.ServerConnection;
import ru.dfhub.eirc.eirc_paper_client.client.util.Encryption;
import ru.dfhub.eirc.eirc_paper_client.handler.GameSessionHandler;
import ru.dfhub.eirc.eirc_paper_client.handler.GameMessageHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.bukkit.Bukkit.getPluginManager;

public final class Main extends JavaPlugin {
    private static Main INSTANCE;
    private static Logger logger;
    private static ServerConnection serverConnection;

    @Override
    public void onEnable() {
        INSTANCE = this;
        logger = getLogger();
        saveDefaultConfig();

        try {
            Encryption.initKey();
        } catch (Encryption.EncryptionException e)
        {
            getLogger().log(Level.INFO, "Security key is empty or invalid! Generating new one...");
            Encryption.generateNewKeyFile();
        }

        try {
            Encryption.initEncryption();
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Security key is invalid! Please, generate new one starting plugin with empty key...");
            getPluginManager().disablePlugin(this);
            return;
        }

        try {
            serverConnection = new ServerConnection(getConfig().getString("server-address"), getConfig().getInt("server-port"));
        } catch (Exception e)
        {
            getLogger().log(Level.WARNING, "Can't connect to the server!");
            getPluginManager().disablePlugin(this);
            return;
        }

        getPluginManager().registerEvents(new GameSessionHandler(), this);
        getPluginManager().registerEvents(new GameMessageHandler(), this);

        DataParser.handleOutputSession(
                true,
                getConfig().getString("server-name", "Paper Server")
        );
    }

    @Override
    public void onDisable() {
        DataParser.handleOutputSession(
                false,
                getConfig().getString("server-name", "Paper Server")
        );
    }

    public static Logger logger() {
        return logger;
    }

    public static ServerConnection getServerConnection() {
        return serverConnection;
    }

    public static void showInGameMessage(Component component) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(component));
    }

    public static Main getInstance() {
        return INSTANCE;
    }
}
