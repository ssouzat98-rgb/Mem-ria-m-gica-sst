# Memória Mágica — Android

Projeto Android nativo feito em Kotlin + Jetpack Compose.

## Recursos
- Tela inicial roxa com botão de jogar e recordes.
- Três dificuldades: Fácil (6 pares), Médio (10 pares) e Difícil (15 pares).
- Cartas embaralhadas, animação de virar, contagem de jogadas e cronômetro.
- Recorde de menor número de jogadas salvo localmente no aparelho.
- Funciona offline depois de instalado.

## Como abrir e gerar o APK
1. Instale o Android Studio estável e o JDK 17.
2. Abra o Android Studio e escolha **Open**.
3. Selecione a pasta `MemoriaMagicaAndroid` descompactada.
4. Aguarde o Gradle sincronizar e instale os componentes de SDK solicitados (compile SDK 37).
5. Conecte um Android com depuração USB ativada ou inicie um emulador.
6. Clique em **Run** para testar.
7. Para gerar o APK: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
8. O APK de teste ficará em `app/build/outputs/apk/debug/app-debug.apk`.

Se o Android Studio solicitar a criação do Gradle Wrapper, aceite ou gere-o pelo menu de Gradle do próprio Android Studio.

## Identificação
- Nome: Memória Mágica
- Application ID: `br.com.memoriamagica`
- Versão: 1.0
- Android mínimo: Android 7.0 (API 24)

As versões de Android Gradle Plugin, Kotlin Compose Compiler e Compose BOM seguem o guia oficial de configuração do Jetpack Compose.
