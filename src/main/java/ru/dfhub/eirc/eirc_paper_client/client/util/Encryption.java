package ru.dfhub.eirc.eirc_paper_client.client.util;

import ru.dfhub.eirc.eirc_paper_client.Main;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Class for initializing and using encryption
 */
public class Encryption {

    /**
     * Various encryption related errors.
     * The essence of the errors is conveyed in the message
     */
    public static class EncryptionException extends Exception {
        public EncryptionException(String message) {
            super(message);
        }
    }

    private static SecretKey key;
    private static IvParameterSpec iv;
    private static Cipher encryptCipher;
    private static Cipher decryptCipher;

    /**
     * Getting the encryption key from the config
     * @throws EncryptionException Encryption key is empty
     * @throws IllegalArgumentException Key is invalid
     */
    public static void initKey() throws EncryptionException, IllegalArgumentException {
        String[] keyString = Main.getInstance().getConfig().getString("security-key").split("<->");

        if (keyString.length == 1) throw new EncryptionException("Encryption key not specified");
        if (keyString.length != 2) throw new IllegalArgumentException("Encryption key is incorrect");

        key = new SecretKeySpec(
                Base64.getDecoder().decode(keyString[0]),
                "AES"
        );
        iv = new IvParameterSpec(
                Base64.getDecoder().decode(keyString[1])
        );
    }

    /**
     * Initializing encryption and decryption methods
     * @throws InvalidKeyException Invalid key
     */
    public static void initEncryption() throws Exception {
        encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        encryptCipher.init(Cipher.ENCRYPT_MODE, key, iv);
        decryptCipher.init(Cipher.DECRYPT_MODE, key, iv);
    }

    /**
     * Generate new valid key and encode id to String(base64)
     * @return Encoded key
     */
    public static String generateNewKey() {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(256);

            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);

            String encKey = Base64.getEncoder().encodeToString(keygen.generateKey().getEncoded());
            String ivString = Base64.getEncoder().encodeToString(iv);

            return "%s<->%s".formatted(encKey, ivString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate new key and write it to config.yml
     */
    public static void generateNewKeyFile() {
        Main.getInstance().getConfig().set("security-key", generateNewKey());
        Main.getInstance().saveConfig();
    }

    /**
     * Encrypt text
     * @param text Text
     * @return Encrypted text (base64)
     * @throws Exception Wrong encryption key
     */
    public static String encrypt(String text) throws Exception {
        byte[] encryptedBytes = encryptCipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypt text
     * @param text Encrypted text (base64)
     * @return Decrypted text
     * @throws Exception Wrong encryption key
     */
    public static String decrypt(String text) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(text);
        byte[] decryptedBytes = decryptCipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
    }
}
