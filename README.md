# Conectando o Firebase ao seu Projeto Android com Jetpack Compose

Este guia detalha os passos para integrar o Firebase em um projeto Android.

## Sumário

*   [Pré-requisitos](#pré-requisitos)
*   [Passo 1: Criar um Projeto no Firebase](#passo-1-criar-um-projeto-no-firebase)
*   [Passo 2: Registrar seu App Android no Firebase](#passo-2-registrar-seu-app-android-no-firebase)
*   [Passo 3: Adicionar o Arquivo de Configuração do Firebase](#passo-3-adicionar-o-arquivo-de-configuração-do-firebase)
*   [Passo 4: Sincronizar o Projeto](#passo-5-sincronizar-o-projeto)

## Pré-requisitos

*   Uma conta do Google.
*   Android Studio instalado.
*   Um projeto Android existente ou um novo projeto criado com Jetpack Compose.

## Passo 1: Criar um Projeto no Firebase

1.  Acesse o [Console do Firebase](https://console.firebase.google.com/).
2.  Clique em "**Adicionar projeto**" (ou "**Criar um projeto**").
3.  Insira um nome para o seu projeto Firebase (ex: "MeuAppComposeFirebase").
4.  Aceite os termos e clique em "**Continuar**".
5.  Clique em "**Criar projeto**". O Firebase tera criado o seu banco.

## Passo 2: Registrar seu App Android no Firebase

Após a criação do projeto Firebase, você será direcionado para a página de visão geral do projeto.

1.  Na seção "**Comece adicionando o Firebase ao seu app**", clique no ícone do Android ( **</>** ).
2.  **Nome do pacote Android**: Insira o nome do pacote do seu aplicativo. Você pode encontrá-lo no arquivo `build.gradle` do módulo do seu app (geralmente `app/build.gradle.kts` ou `app/build.gradle`), na propriedade `applicationId`.
    *   Exemplo: `com.example.meuappcompose`
3.  **(Opcional) Apelido do app**: Forneça um apelido para identificar este app Android específico dentro do seu projeto Firebase (ex: "Meu App Compose - Android").
4.  **Certificado de assinatura de depuração SHA-1 (Opcional, mas recomendado para alguns serviços como Autenticação do Google Sign-In)**:
    *   Para obter o SHA-1, no Android Studio, abra o painel "Gradle" no lado direito.
    *   Navegue até `SeuProjeto > app > Tasks > android > signingReport`.
    *   Clique duas vezes em `signingReport`. O SHA-1 para a variante de depuração será exibido no console "Run".
    *   Copie o valor `SHA1` (geralmente para a variante `debugAndroidTest` ou `debug`).
5.  Clique em "**Registrar app**".

## Passo 3: Adicionar o Arquivo de Configuração do Firebase

1.  Após registrar o app, o Firebase fornecerá um arquivo `google-services.json`. Clique em "**Fazer o download de google-services.json**".
2.  No Android Studio, mude para a visualização de projeto "**Project**" (no painel esquerdo).
3.  Mova o arquivo `google-services.json` que você baixou para o diretório `app/` do seu módulo de aplicativo Android.
    *   O caminho completo deve ser algo como `SeuProjeto/app/google-services.json`.
4.  Clique em "**Próxima**" no console do Firebase.

## Passo 4: Sincronizar o Projeto

1.  No Android Studio, clique em "**Sync Now**" na barra que aparece na parte superior após modificar os arquivos Gradle. Isso fará o download das dependências adicionadas.
2.  Execute seu aplicativo em um emulador ou dispositivo físico. Se tudo estiver configurado corretamente, seu app se comunicará com o Firebase. O console do Firebase pode levar alguns minutos para detectar a comunicação.
3.  Clique em "**Continuar no console**" no console do Firebase.

E assim você vai ter a conexão de seu Firebase no seu projeto do Kotlin.
