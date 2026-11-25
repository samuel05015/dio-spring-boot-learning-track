package dio.budgeting.infrastructure.http;

import dio.budgeting.application.ListTransactionsByCategoryUseCase;
import dio.budgeting.application.PersistTransactionUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.ai.audio.transcription.TranscriptionModel;
import org.springframework.ai.audio.tts.TextToSpeechModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TransactionControllerAudioValidationTest {
    private final TranscriptionModel transcription = mock(TranscriptionModel.class);
    private final ChatClient chat = mock(ChatClient.class);
    private final TextToSpeechModel speech = mock(TextToSpeechModel.class);

    @Test
    void refusesEmptyUploadBeforeCallingAi() throws IOException {
        var upload = new MockMultipartFile("file", "empty.m4a", "audio/mp4", new byte[0]);

        assertThatThrownBy(() -> controller().transcribe(upload))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        error -> assertThat(error.getStatusCode())
                                .isEqualTo(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(transcription, chat, speech);
    }

    @Test
    void refusesBlankTranscriptionBeforeChatOrSpeech() throws IOException {
        var upload = new MockMultipartFile("file", "sample.m4a", "audio/mp4", new byte[]{1});
        when(transcription.transcribe(any(Resource.class))).thenReturn("  ");

        assertThatThrownBy(() -> controller().transcribe(upload))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        error -> assertThat(error.getStatusCode())
                                .isEqualTo(HttpStatus.BAD_REQUEST));

        verifyNoInteractions(chat, speech);
    }

    private TransactionController controller() throws IOException {
        var builder = mock(ChatClient.Builder.class, RETURNS_SELF);
        when(builder.build()).thenReturn(chat);
        var systemPrompt = new ByteArrayResource("Teste".getBytes(StandardCharsets.UTF_8));
        return new TransactionController(
                mock(PersistTransactionUseCase.class),
                mock(ListTransactionsByCategoryUseCase.class),
                transcription, systemPrompt, builder, speech);
    }
}
