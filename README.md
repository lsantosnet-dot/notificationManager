# Painel de Notificações

App Android nativo (Kotlin + Jetpack Compose) de uso pessoal que captura as notificações
recebidas no aparelho, agrupa por app remetente, e gera resumos de cada grupo com IA
totalmente on-device (Gemini Nano via ML Kit GenAI Summarization) — nada sai do celular.

Não é publicado na Play Store; o fluxo abaixo gera um APK para instalação manual.

## Stack

- Kotlin, Jetpack Compose (Material 3), tema escuro fiel ao protótipo (`ui/theme`)
- Room para persistência local (`data/local`)
- `NotificationListenerService` para captura de notificações (`notification/`)
- WorkManager para limpeza periódica por retenção (`work/`)
- ML Kit GenAI Summarization (`com.google.mlkit:genai-summarization`) para os resumos (`data/ai`)
- DataStore Preferences para os ajustes (`data/repository/SettingsRepository`)
- minSdk 26, compileSdk/targetSdk 35
- `applicationId` = `com.leo.painelnotificacoes`

Não há framework de injeção de dependência (Hilt/Koin) — o app é pequeno o bastante para um
container manual (`AppContainer.kt`) instanciado uma vez em `PainelNotificacoesApp`.

## Estrutura do projeto

```
app/src/main/java/com/leo/painelnotificacoes/
├── AppContainer.kt                # DI manual
├── MainActivity.kt                # gate de permissão + NavHost
├── PainelNotificacoesApp.kt       # Application: cria o container, agenda o worker
├── data/
│   ├── ai/SummarizationManager.kt # wrapper coroutine do ML Kit GenAI
│   ├── local/                     # NotificationEntity, GroupSummaryEntity, DAOs, Room DB
│   └── repository/                # NotificationRepository, SettingsRepository
├── navigation/PainelNavHost.kt
├── notification/
│   ├── NotificationCaptureService.kt  # onNotificationPosted/Removed + catch-up
│   ├── NotificationMapper.kt          # StatusBarNotification -> NotificationEntity
│   └── NotificationAccess.kt          # checagem da permissão especial
├── ui/
│   ├── theme/                     # cores, tipografia, tema (Compose)
│   ├── components/                # GroupAvatar, NoiseMeter
│   ├── home/                      # Home: lista de grupos
│   ├── group/                     # Detalhe do grupo: resumo IA + itens (swipe-to-dismiss)
│   ├── permission/                # Tela de solicitação de acesso a notificações
│   └── settings/                  # Retenção configurável
├── util/                          # formatação de tempo relativo, cor/iniciais de avatar
└── work/                          # RetentionCleanupWorker + WorkScheduler
```

## Como gerar o APK

Pré-requisitos: JDK 17+, Android SDK (compileSdk/targetSdk 35) instalado — mais fácil via
Android Studio (ele baixa o SDK automaticamente na primeira abertura do projeto).

```bash
# Debug (assinado com a chave de debug padrão — pronto para instalar direto)
./gradlew assembleDebug
# APK gerado em: app/build/outputs/apk/debug/app-debug.apk

# Release (sem assinatura configurada neste projeto — precisa assinar antes de instalar,
# ou abrir o projeto no Android Studio e usar Build > Generate Signed App Bundle / APK)
./gradlew assembleRelease
# APK gerado em: app/build/outputs/apk/release/app-release-unsigned.apk
```

Para uso pessoal no S24 Ultra, o build de **debug** já resolve (instala direto, sem passos
extras de assinatura).

## Instalar no aparelho

Com o S24 Ultra conectado via USB e depuração USB habilitada:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Alternativa sem cabo: transferir o APK para o aparelho (ex.: Drive, e-mail) e instalar
manualmente — é preciso permitir "instalar apps de fontes desconhecidas" para o app usado
para abrir o arquivo.

## Conceder acesso a notificações

Na primeira abertura, o app detecta que a permissão especial de notification listener não
foi concedida e mostra uma tela explicando o motivo, com um botão que abre diretamente
`Configurações > Apps com acesso a notificações`. Depois de habilitar o Painel de
Notificações nessa lista e voltar ao app, a permissão é revalidada automaticamente
(o app observa o ciclo de vida da Activity) e a tela principal aparece.

## Resumo por IA local (Gemini Nano)

O card de resumo no topo do detalhe do grupo chama a API ML Kit GenAI Summarization, que
roda inteiramente on-device via Gemini Nano. Isso só está disponível em aparelhos com
suporte de hardware/software ao AICore (nem todo Android 14+/15 tem isso) — quando o
dispositivo não suporta, o app mostra uma mensagem explicando em vez do botão, sem quebrar
o resto do app.

> **Nota sobre a API (beta):** a API ML Kit GenAI Summarization (`genai-summarization:1.0.0-beta1`)
> ainda está em beta e pode evoluir entre versões. Toda a integração está isolada em
> `data/ai/SummarizationManager.kt` — se um bump de versão renomear alguma classe/método,
> esse é o único arquivo que precisa de ajuste. Recomendo compilar o projeto localmente
> (Android Studio faz a sincronização do Gradle e sinaliza qualquer divergência de API na
> hora) antes do primeiro uso, já que este ambiente de desenvolvimento não teve acesso ao
> Android SDK nem ao repositório Maven do Google para validar a compilação.

## Retenção de dados

Notificações mais antigas que N dias (padrão: 30, ajustável em Ajustes) são excluídas
automaticamente por um worker periódico do WorkManager, que roda a cada 24h enquanto o
app está instalado.

## Privacidade

- Todo o processamento (armazenamento, agrupamento, resumo por IA) acontece no aparelho.
- A tabela de notificações (`painel_notificacoes.db`) é explicitamente excluída do Android
  Auto Backup e do device-to-device transfer via `res/xml/data_extraction_rules.xml` e
  `res/xml/backup_rules.xml`, já que o conteúdo pode ser sensível.
