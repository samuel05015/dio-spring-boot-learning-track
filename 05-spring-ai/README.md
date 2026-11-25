# DIO Spring Boot - Final Project 05: Spring AI (budgeting)

## Evolução para o desafio da DIO

Adição desenvolvida com apoio de IA sobre o projeto de referência da DIO. O endpoint `POST /transactions/ai` agora responde com HTTP 400 quando o arquivo chega vazio ou quando o serviço de transcrição não identifica fala. No primeiro caso, a aplicação nem faz a chamada de transcrição. No segundo, evita enviar um comando em branco ao modelo de linguagem ou gerar uma locução sem conteúdo.

### Testar a validação

Com JDK 25 instalado, execute os testes específicos. Eles usam substitutos para os serviços da OpenAI e dispensam uma chave de API.

```sh
./gradlew test --tests '*TransactionControllerAudioValidationTest'
```

Com o aplicativo em execução, faça um pedido com um arquivo vazio e confira o status 400.

```sh
touch audio-vazio.m4a
curl -i -F 'file=@audio-vazio.m4a;type=audio/mp4' http://localhost:8080/transactions/ai
```

Para verificar o fluxo completo com a chave configurada, envie um M4A com uma frase em português brasileiro, por exemplo “Gastei 20 reais no mercado”. O endpoint devolve o áudio de resposta. Use apenas dados de exemplo ao testar a integração.

```sh
curl -f -F 'file=@meu-teste.m4a;type=audio/mp4' \
  http://localhost:8080/transactions/ai -o resposta.mp3
```

A mudança mantém as regras de transação fora do controlador. A validação da entrada fica na camada HTTP, enquanto os casos de uso continuam responsáveis por salvar e consultar as transações.

## Introduction

This final module applies Spring AI in a budgeting API while preserving the same layered architecture used across the track.

The goal is to integrate AI capabilities without bypassing domain and use case boundaries.

## Code Context

The project processes voice commands to create and query financial transactions.

Primary flow:

1. Client uploads an audio file.
2. Audio is transcribed into text.
3. The model selects an application tool/use case.
4. The use case persists or queries transaction data.
5. The final response is converted to audio.

## Project Structure

- `src/main/java/dio/budgeting/domain`
  - Domain model and repository contract.
- `src/main/java/dio/budgeting/application`
  - Use cases used by both REST and AI tool calling.
- `src/main/java/dio/budgeting/infrastructure`
  - HTTP adapters, JPA adapters, and integration glue.

## Module-Specific Topics

### Speech-to-text

- Uses `TranscriptionModel` for audio transcription.
- Model settings are configured in `application.properties`.

### Tool calling

- `ChatClient` registers use-case tools.
- `@Tool` methods expose business capabilities to the model.

### Text-to-speech

- `TextToSpeechModel` produces MP3 output from final text.
- AI endpoint returns generated audio.

## Spring AI Documentation

- Spring AI Reference: https://docs.spring.io/spring-ai/reference/index.html
- ChatModel API: https://docs.spring.io/spring-ai/reference/api/chatmodel.html
- ChatClient API: https://docs.spring.io/spring-ai/reference/api/chatclient.html
- Tools API: https://docs.spring.io/spring-ai/reference/api/tools.html
- Audio Transcriptions API: https://docs.spring.io/spring-ai/reference/api/audio/transcriptions.html
- Audio Speech API: https://docs.spring.io/spring-ai/reference/api/audio/speech.html

## Shared Architecture References

Common architecture concepts are documented in the root README:

- [DDD layers](../README.md#ddd-layered-architecture)
- [Class vs record](../README.md#java-class-vs-java-record-in-domain-modeling)
- [Strong typed identifiers](../README.md#strong-typed-identifiers)
- [Repository pattern](../README.md#repository-pattern)
- [Use cases and Clean Architecture](../README.md#use-cases-and-clean-architecture)
- [Docker Compose support](../README.md#docker-compose-support-in-development)

## How to Run

Set your OpenAI API key:

```bash
export OPENAI_API_KEY="your_api_key_here"
```

Run the application and tests:

```bash
./gradlew bootRun
./gradlew test
```

## Notes

- Educational final project focused on AI plus architectural discipline.
- External provider integration tests may require active credentials.
