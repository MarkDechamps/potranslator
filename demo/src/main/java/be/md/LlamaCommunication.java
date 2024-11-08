package be.md;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;


public class LlamaCommunication {


    private static final String MODEL = "llama3.1";

    public static String sendHttpPostRequest(String url, String text) throws IOException, InterruptedException {
        // Maak een JSON payload voor de vertaling
        String jsonInput = "{ \"model\": \"" + MODEL + "\", \"text\": \"" + text.replace("\"", "\\\"") + "\" }";

        // Bouw de HTTP POST request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonInput, StandardCharsets.UTF_8))
                .build();

        // Voer de request uit en krijg de response terug
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body(); // Geeft de JSON-string terug van het antwoord
        } else {
            throw new IOException("Error: " + response.statusCode() + " - " + response.body());
        }
    }

}
