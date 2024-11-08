package be.md;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Pottranslator {

    private static final String LLAMA_API_URL = "http://localhost:11434/api/chat"; // Pas aan aan jouw setup

    public static void main(String[] args) throws IOException {
        String poFilePath = "C:/mark/chesstempo/nl_NL.po";
        List<String> poLines = Files.readAllLines(Paths.get(poFilePath));
        List<String> updatedPoLines = new ArrayList<>();

        String currentMsgid = null;
        boolean needsTranslation = false;

        for (String line : poLines) {
            line = line.trim();

            // Zoek naar msgid
            if (line.startsWith("msgid")) {
                currentMsgid = extractText(line);
                needsTranslation = false;
            }
            // Zoek naar msgstr (lege strings)
            else if (line.startsWith("msgstr") && extractText(line).isEmpty()) {
                needsTranslation = true;
            }

            // Vertaal en voeg toe
            if (needsTranslation && currentMsgid != null) {
                String translation = llamaTranslate(currentMsgid);
                updatedPoLines.add("msgstr \"" + translation + "\"");
                needsTranslation = false;
                currentMsgid = null;
            } else {
                updatedPoLines.add(line); // Voeg originele lijn toe
            }
        }

        Files.write(Paths.get(poFilePath), updatedPoLines);
        System.out.println("Vertaling voltooid en opgeslagen in " + poFilePath);
    }

    private static String llamaTranslate(String text) throws IOException {
        // Aanroep naar jouw locale LLaMA API
        String jsonResponse = sendHttpPostRequest(LLAMA_API_URL, text);
        // Parsing JSON response. Hier verwacht ik een simpel JSON object {"translation": "translated_text"}
        return parseTranslation(jsonResponse);
    }

    private static String sendHttpPostRequest(String url, String text) throws IOException {
        // Implementatie voor HTTP POST met de benodigde headers en payload voor je LLaMA API
        // Dit kun je invullen afhankelijk van hoe de LLaMA API request-data ontvangt
        try {
            return LlamaCommunication.sendHttpPostRequest(url, text);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String parseTranslation(String jsonResponse) {
        // JSON parsing code om de "translation" waarde uit het JSON antwoord te halen
        return jsonResponse; // Voor nu als placeholder
    }

    private static String extractText(String poLine) {
        int startIndex = poLine.indexOf("\"") + 1;
        int endIndex = poLine.lastIndexOf("\"");
        return poLine.substring(startIndex, endIndex);
    }
}
