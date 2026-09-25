package com.example.learnmaster.tccv2.security;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

// Tokens aleatorios (refresh e recuperacao): o valor vai para o usuario e so o hash fica no banco
public final class Tokens {

    private static final SecureRandom RANDOM = new SecureRandom();

    private Tokens() {
    }

    // Codigo numerico de 6 digitos (000000 a 999999)
    public static String gerarCodigo() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    /*
     * HMAC-SHA256 em hexadecimal (64 caracteres). Usado nos codigos de 6 digitos: um SHA-256 simples
     * seria revertido testando o milhao de combinacoes; sem a chave, o hash do banco nao revela o codigo.
     */
    public static String hmac(String chave, String valor) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(chave.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String gerar() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // SHA-256 em hexadecimal (64 caracteres, cabe no CHAR(64) do banco)
    public static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
