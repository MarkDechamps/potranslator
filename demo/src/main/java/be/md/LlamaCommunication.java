package be.md;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
        //"model": "llama3.1",
        String json = """
                {
                 "model": "aya-expanse",
                 "stream": false,
                 "temperature": 1,
                 "min_p": 1,
                 "messages": [
                        { "role": "user", "content": "____PLACEHOLDER____" }
                  ]
                }
                """;


        // Bouw de HTTP POST request
        text = text.replace("\"", "\\\"");
        String question = json.replace("____PLACEHOLDER____", text).replace("\\\\","\\");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(question, StandardCharsets.UTF_8))
                .build();

        // Voer de request uit en krijg de response terug
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body(); // Geeft de JSON-string terug van het antwoord
        } else {
            throw new IOException("Error: " + response.statusCode() + " - " + response.body()+" while processing:["+question+"]");
        }
    }

    public static String parseResponseBody(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        StringBuilder contentBuilder = new StringBuilder();

        // Split the input string by newlines (assuming each response is separated by a newline)
        String[] lines = jsonString.split("\\r?\\n");

        try {
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    JsonNode jsonNode = objectMapper.readTree(line);
                    JsonNode contentNode = jsonNode.path("message").path("content");

                    // If content exists, append it to the result string
                    if (!contentNode.isMissingNode()) {
                        contentBuilder.append(contentNode.asText());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error parsing the response body", e);
        }

        return contentBuilder.toString().trim();
    }

}
