package dev.minixr9k.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.minixr9k.auth.PlayerProfile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Requests {

    private static final String PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String SKIN_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";

//    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    public static List<PlayerProfile> getSkin(String username) {
        String profile = makeRequest(PROFILE_URL + username);
        if (profile == null || profile.isEmpty()) {
            return new ArrayList<>(); // Возвращаем пустой список, чтобы избежать NullPointerException
        }
        JsonObject jsonProfile = JsonParser.parseString(profile).getAsJsonObject();
        if (!jsonProfile.has("id")) {
            return new ArrayList<>();
        }
        String uuid = jsonProfile.get("id").getAsString();

        String fetchedSkin = makeRequest(SKIN_URL.formatted(uuid));
        if (fetchedSkin == null || fetchedSkin.isEmpty()) {
            return new ArrayList<>();
        }

        List<PlayerProfile> properties = new ArrayList<>();
        try {
            JsonObject jsonSkin = JsonParser.parseString(fetchedSkin).getAsJsonObject()
                    .get("properties").getAsJsonArray().get(0).getAsJsonObject();

            String name = jsonSkin.get("name").getAsString(); // Обычно это "textures"
            String value = jsonSkin.get("value").getAsString();
            String signature = jsonSkin.get("signature").getAsString();

            properties.add(new PlayerProfile(name, value, signature));
        } catch (Exception e) {
            // Если текстур нет, возвращаем пустой список свойств
        }
        return properties;
    }

//    public static String makeRequest(String url) {
//        try {
//            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            return response.body();
//        } catch (IOException | InterruptedException e) {
//            return null;
//        }
//    }

    public static String makeRequest(String urlString) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000); // 2 секунды лимит
            conn.setReadTimeout(2000);
            conn.setUseCaches(false);

            // Если ответ не 200, ничего не читаем
            if (conn.getResponseCode() != 200) return null;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (conn != null) conn.disconnect(); // ГАРАНТИРОВАННО закрываем соединение
        }
    }

}
