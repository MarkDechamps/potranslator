package be.md;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static be.md.LlamaCommunication.parseResponseBody;
import static org.springframework.util.StringUtils.*;


public class Pottranslator {

    public static final String LLAMA_API_URL = "http://localhost:11434/api/chat"; // Adjust to your setup..

    public static void main(String[] args) throws IOException {
        String poFilePath = "C:/mark/dev/potranslator/demo/input/nl_NL.po";//this is the path to the file to translate

        List<String> poLines = Files.readAllLines(Paths.get(poFilePath));
        List<String> updatedPoLines = new ArrayList<>();

        String currentMsgid = null;
        boolean needsTranslation = false;

        var prompt = createPrompt();

        for (String line : poLines) {

            if (line.startsWith("msgid")) {//remember the id...
                currentMsgid = extractText(line);
                needsTranslation = false;
            } else if (line.startsWith("msgstr") && extractText(line).isEmpty()) {//because here we need it.
                needsTranslation = true;
            }

            // Translate and add...
            if (needsTranslation && hasText(currentMsgid)) {
                String translation = llamaTranslate(prompt + currentMsgid);
                if (translation.length() < 100) {
                    updatedPoLines.add("msgstr \"" + translation + "\"");
                } else {
                    log("Too long: [" + translation + "]");
                }

                needsTranslation = false;

                //log(currentMsgid + "==>" + translation);
                currentMsgid = null;
            } else {
                updatedPoLines.add(line); // Don't touch it, add original line to output
            }
        }

        Files.write(Paths.get(poFilePath + "_"), updatedPoLines);
        log("Translation done and saved in " + poFilePath);
    }

    public static String createPrompt() {
        var library = createDutchLibrary();
        library += "When KRBNKRB is passed in it should be translated to KTLPKTL. The first character of the name of the piece.";
        library += "Try to keep the same layout, also for capitals.";
        library += "Everything between curly braces should be taken over literally.";
        library += "If something starts with a capital letter it should be taken over.";
        return "These labels from a chess application need to be translated from English to Dutch." + library
                + " Only return the proper translation or,if it would be unclear to you then return nothing (not a word even). " +
                "Don't explain, don't ask questions, don't be polite. Give ONLY the translation! " +
                "This is the line:";
    }

    /**
     * This is necessary as chess has some specific terms and the AI can use some help..
     */
    private static String createDutchLibrary() {
        var map = new HashMap<>();
        map.put("Pin", "Penning");
        map.put("Skewer", "Spies");
        map.put("blocking", "blokkeren");
        map.put("Benchmark", "Benchmark");
        map.put("Unpinning", "Ontpennen");
        map.put("Last Wrong", "Laatste fout");
        map.put("Coercion", "Dwang");
        map.put("Desperado", "Desperado");
        map.put("Counting", "Tellen");
        map.put("Engine", "Engine");
        map.put("Jailbreak", "Uitbraak");
        map.put("Check", "Schaak");
        map.put("Loss", "Verlies");
        map.put("Control", "Controleren");
        map.put("Discovery", "Aftrekaanval");
        map.put("Mate", "Mat");
        map.put("Square", "Veld");
        map.put("Multi-Square", "Meerdere Velden");
        map.put("Pendulum", "Pendulum");
        map.put("Queen", "Dame");
        map.put("Rook", "Toren");
        map.put("Cutoff", "Afgesneden");
        map.put("Recapture", "Terug slaan");
        map.put("Capture", "Slaan");
        map.put("Seizing", "Opeisen");
        map.put("Windmill", "Windmolen");
        map.put("castle", "rokeren");
        map.put("castle king side", "korte rokade");
        map.put("castle queen side", "lange rokade");
        map.put("unseen", "ongezien");
        map.put("Counter", "Counter");
        map.put("Game", "Partij");
        map.put("My Games", "Mijn Partijen");
        map.put("Thema", "Thema");
        map.put("Hit and run", "Slaan en weg");
        map.put("Cross pin", "Kruispenning");
        map.put("All Time High", "Persoonlijk record");
        map.put("Streak", "Streak");
        map.put("Moves", "Zetten");
        map.put("Move", "Zet");
        map.put("engine", "engine");
        map.put("capture", "nemen");
        map.put("exchange", "kwaliteit");

        try {
            var mapString = new ObjectMapper().writeValueAsString(map);
            return " Use this lookup key-value json map (it is case insensitive) to help with the translation. The key is English, the value is the corresponding Dutch translation: " + mapString + "."
                    + " Use this map as a helping tool. If something does not match, use the other rules.";
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void log(String translation) {
        System.out.println(translation);
    }

    private static String llamaTranslate(String text) throws IOException {
        String result;
        try {
            result = LlamaCommunication.sendHttpPostRequest(LLAMA_API_URL, text);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        String jsonResponse = result;
        return parseResponseBody(jsonResponse);
    }


    private static String extractText(String poLine) {
        int startIndex = poLine.indexOf("\"") + 1;
        int endIndex = poLine.lastIndexOf("\"");
        return poLine.substring(startIndex, endIndex);
    }

}
