package be.md;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static be.md.Pottranslator.createPrompt;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LlamaCommunicationTest {

    private final String BASE = createPrompt();

    @Test
    public void annoyingLineShouldRunWithoutExceptions() throws IOException, InterruptedException {
        var line =BASE+"Only show \"annoying alt\" warnings";
        var response = LlamaCommunication.sendHttpPostRequest(Pottranslator.LLAMA_API_URL,line);
        var parsed = LlamaCommunication.parseResponseBody(response);
        System.out.println(parsed);
    }
    @Test
    public void jailbreak() throws IOException, InterruptedException {
        var line = BASE+"Jailbreak";
        var response = LlamaCommunication.sendHttpPostRequest(Pottranslator.LLAMA_API_URL,line);
        var parsed = LlamaCommunication.parseResponseBody(response);
        assertThat(parsed).isEqualTo("Uitbraak");
    }
}