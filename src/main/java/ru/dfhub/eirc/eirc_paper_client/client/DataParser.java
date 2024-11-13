package ru.dfhub.eirc.eirc_paper_client.client;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.json.JSONObject;
import ru.dfhub.eirc.eirc_paper_client.Main;
import ru.dfhub.eirc.eirc_paper_client.client.util.Encryption;
import ru.dfhub.eirc.eirc_paper_client.client.util.ResourcesReader;

import java.util.logging.Level;

/**
 * Class for working with data and processing it
 */
public class DataParser {

    /**
     * Types of incoming and outgoing messages
     */
    public enum MessageType {
        USER_MESSAGE("user_message"), // User text messages
        USER_SESSION("user_session"); // Messages about user join/leave

        private String fileName;

        MessageType(String fileName) {
            this.fileName = fileName;
        }

        private String getResourcesPath() {
            return "message_templates/%s.json".formatted(this.fileName);
        }

        public String getTemplate() {
            return new ResourcesReader(this.getResourcesPath()).readString().replace("\n", "");
        }
    }

    /**
     * Parse the incoming message and take the necessary action to work with it
     * @param data Raw data from server
     */
    public static void handleInputData(String data) {
        JSONObject dataObj;
        try {
            dataObj = new JSONObject(data);
        } catch (Exception e) { return; } // Null message from server


        switch (dataObj.getString("type")) {
            case "user-message" -> handleUserMessage(dataObj.getJSONObject("content"));
            case "user-session" -> handleUserSession(dataObj.getJSONObject("content"));
        }
    }

    /**
     * Collect a user message into a data type accepted by the client
     * @param message Message
     */
    public static void handleOutputMessage(String message, String username) {
        String template;
        try {
            template = MessageType.USER_MESSAGE.getTemplate();
        } catch (Exception e) {
            Main.logger().log(Level.WARNING, "Error occurred while handling output message! (Getting template)", e);
            return;
        }

        String encryptedMessage;
        try {
            encryptedMessage = Encryption.encrypt(message);
        } catch (Exception e) {
            Main.logger().log(Level.WARNING, "Error occurred while handling output message! (Encrypt process)", e);
            return;
        }

        Main.getServerConnection().sendToServer(template
                        .replace("%user%", Main.getInstance().getConfig().getString("player-name-format").replace("<user>", username))
                        .replace("%message%", encryptedMessage)
        );
    }

    /**
     * Process and send a message about your session (join/leave)
     * @param isJoin Is join
     */
    public static void handleOutputSession(boolean isJoin, String username) {
        String status = isJoin ? "join" : "leave";

        String template;
        try {
            template = MessageType.USER_SESSION.getTemplate();
        } catch (Exception e) {
            Main.logger().log(Level.WARNING, "Error occurred while handling output session! (Getting template)", e);
            return;
        }

        Main.getServerConnection().sendToServer(template
                        .replace("%user%", Main.getInstance().getConfig().getString("player-name-format").replace("<user>", username))
                        .replace("%status%", status)
        );
    }

    /**
     * Processing an incoming user message
     * @param data Data's "content" object
     */
    private static void handleUserMessage(JSONObject data) {
        String sender = data.getString("user");
        String encryptedMessage = data.getString("message"); // In ftr, decrypt and handle decryption errors here

        if (data.optBoolean("minecraft-ingame", false)) return; // Don't show inGame messages

        String message;
        try {
            message = Encryption.decrypt(encryptedMessage);
        } catch (Exception e) {
            Main.logger().log(Level.WARNING, "Error occurred while handling input message! (Decrypt process)", e);
            return;
        }

        String formattedMessage = Main.getInstance().getConfig().getString("message-format")
                .replace("<user>", sender)
                .replace("<message>", message);

        Main.showInGameMessage(
                MiniMessage.miniMessage().deserialize(formattedMessage)
        );
    }

    /**
     * Handle input user-session(join/leave) message and show it
     * @param data Data's "content" object
     */
    private static void handleUserSession(JSONObject data) {
        String user = data.getString("user");
        String status = data.getString("status").equals("join") ? "joined!" : "left.";

        if (data.optBoolean("minecraft-ingame", false)) return; // Don't show inGame messages

        String formattedMessage = Main.getInstance().getConfig().getString("session-format")
                .replace("<user>", user)
                .replace("<status>", status);

        Main.showInGameMessage(MiniMessage.miniMessage().deserialize(formattedMessage));
    }
}
