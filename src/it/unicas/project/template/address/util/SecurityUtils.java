package it.unicas.project.template.address.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class SecurityUtils {

    /**
     * Calcola l'hash SHA-256 di una stringa.
     * Usato per password e risposte alle domande di sicurezza.
     */
    public static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
