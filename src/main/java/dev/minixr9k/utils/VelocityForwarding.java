package dev.minixr9k.utils;

import dev.minixr9k.auth.PlayerProfile;
import io.netty.buffer.ByteBuf;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dev.minixr9k.utils.ProtocolUtils.*;

public class VelocityForwarding {

    public static class ForwardedData {
        public String ip;
        public UUID uuid;
        public String username;
        public List<PlayerProfile> properties = new ArrayList<>();
    }

    public static ForwardedData parse(ByteBuf buf, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] signature = new byte[32];
        buf.readBytes(signature);

        byte[] dataToVerify = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), dataToVerify);

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] computedSignature = mac.doFinal(dataToVerify);

        if (!MessageDigest.isEqual(signature, computedSignature)) {
            throw new SecurityException("Invalid Velocity HMAC signature! Check velocity secret key.");
        }

        int version = readVarInt(buf);
        ForwardedData data = new ForwardedData();
        data.ip = readString(buf);
        data.uuid = readUUID(buf);
        data.username = readString(buf);

        int propertyCount = ProtocolUtils.readVarInt(buf);
        for (int i = 0; i < propertyCount; i++) {
            String name = ProtocolUtils.readString(buf);
            String value = ProtocolUtils.readString(buf);
            String sig = buf.readBoolean() ? ProtocolUtils.readString(buf) : null;
            data.properties.add(new PlayerProfile(name, value, sig));
        }
        return data;
    }

}
