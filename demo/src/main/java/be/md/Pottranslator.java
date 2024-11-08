package be.md;

import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static be.md.LlamaCommunication.parseResponseBody;


public class Pottranslator {

    public static final String LLAMA_API_URL = "http://localhost:11434/api/chat"; // Pas aan aan jouw setup

    public static void main(String[] args) throws IOException {
        String poFilePath = "C:/mark/chesstempo/nl_NL.po";
        List<String> poLines = Files.readAllLines(Paths.get(poFilePath));
        List<String> updatedPoLines = new ArrayList<>();

        String currentMsgid = null;
        boolean needsTranslation = false;

        var prompt = createPrompt();

        for (String line : poLines) {
            line = line.trim();

            if (line.startsWith("msgid")) {
                currentMsgid = extractText(line);
                needsTranslation = false;
            } else if (line.startsWith("msgstr") && extractText(line).isEmpty()) {
                needsTranslation = true;
            }

            // Vertaal en voeg toe
            if (needsTranslation && StringUtils.hasText(currentMsgid)) {
                String translation = llamaTranslate(prompt + currentMsgid);
                if (translation.length() < 50) {
                    updatedPoLines.add("msgstr \"" + translation + "\"");
                } else {
                    log("Too long: [" + translation + "]");
                }

                needsTranslation = false;

                log(currentMsgid + "==>" + translation);
                currentMsgid = null;
            } else {
                updatedPoLines.add(line); // Voeg originele lijn toe
            }
        }

        Files.write(Paths.get(poFilePath + "_"), updatedPoLines);
        log("Vertaling voltooid en opgeslagen in " + poFilePath);
    }

    public static String createPrompt() {
        var library = createLibrary();
        library += "When KRBNKRB is passed in it should be translated to KTLPKTL. The first character of the name of the piece.";
        library += "Try to keep the same layout, also for capitals.";
        return "I will pass a line to you which you should translate in dutch." + library + " Only return the proper translation or,if it would be unclear to you then return nothing (not a word even). Don't explain, don't ask questions, don't be polite. Don't respond with anything else then a translation or an empty string as this input will be used in an automatic translation process so it needs to be right or empty. This is the line:";
    }

    private static String createLibrary() {
        var map = new HashMap<>();
        map.put("pin", "penning");
        map.put("skewer", "spies");
        map.put("blocking", "blokkeren");
        map.put("Benchmark", "Benchmark");
        map.put("Unpinning", "Ontpennen");
        map.put("Last Wrong", "Laatste fout");
        map.put("Coercion", "Dwang");
        map.put("Desperado", "Desperado");
        map.put("Counting", "Tellen");
        map.put("Engine", "Engine");
        map.put("Jailbreak", "Uitbraak");
        map.put("Loss", "Verlies");

        return "The subject is chess. Use this words to help with the translation: " + map + ".";
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
