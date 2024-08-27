# MainActivity Documentation

## Descrição

A `MainActivity` é a tela principal do aplicativo onde os usuários podem realizar login, registro e recuperação de senha. Ela utiliza Firebase Authentication para autenticação de usuários e Firebase Realtime Database para armazenar informações de usuários registrados.

## Estrutura da Classe

A classe `MainActivity` estende `AppCompatActivity` e contém as seguintes seções principais:

### 1. Variáveis de Instância

- **FirebaseAuth mAuth**: Instância do Firebase Authentication para operações de autenticação.
- **DatabaseReference mDatabase**: Referência ao Firebase Realtime Database para operações de leitura e escrita de dados.
- **EditText loginEmailEditText**: Campo de texto para entrada de e-mail no login.
- **EditText loginPasswordEditText**: Campo de texto para entrada de senha no login.
- **Button loginButton**: Botão para acionar o login.
- **Button forgotPasswordButton**: Botão para recuperação de senha.
- **EditText registerEmailEditText**: Campo de texto para entrada de e-mail no registro.
- **EditText registerPasswordEditText**: Campo de texto para entrada de senha no registro.
- **EditText registerConfirmPasswordEditText**: Campo de texto para confirmação de senha no registro.
- **Button registerButton**: Botão para acionar o registro.

### 2. Método `onCreate`

O método `onCreate` é chamado quando a `MainActivity` é criada. Aqui, são realizadas as seguintes operações:

1. **Inicialização do Firebase**: Inicializa o Firebase App e obtém instâncias de `FirebaseAuth` e `DatabaseReference`.
2. **Configuração dos Campos e Botões**: Inicializa os campos de texto e botões com base em suas IDs definidas no layout XML (`activity_main.xml`).
3. **Verificação de Login Existente**: Verifica se o usuário já está autenticado. Se sim, redireciona para a `HomeActivity`.
4. **Configuração de Listeners**: Define ouvintes de clique para os botões de login, registro e recuperação de senha.

### 3. Métodos de Registro e Login

#### `registerUser(String email, String password, String confirmPassword)`

- Verifica se as senhas fornecidas coincidem.
- Tenta criar um novo usuário com o e-mail e senha fornecidos.
- Em caso de sucesso, salva os dados do usuário no Firebase Realtime Database e redireciona para a `HomeActivity`.
- Em caso de falha, trata o erro de autenticação.

#### `loginUser(String email, String password)`

- Tenta autenticar o usuário com o e-mail e senha fornecidos.
- Em caso de sucesso, redireciona para a `HomeActivity`.
- Em caso de falha, trata o erro de autenticação.

### 4. Tratamento de Erros

#### `handleFirebaseAuthError(Exception exception)`

- Trata erros de autenticação baseados no código de erro retornado pelo Firebase.
- Exibe mensagens específicas para erros comuns, como e-mail inválido ou senha incorreta.
- Para erros inesperados, exibe uma mensagem genérica.

### 5. Classe Interna `User`

A classe `User` é um modelo para armazenar informações do usuário no Firebase Realtime Database.

#### Variáveis de Instância

- **String fullUid**: Identificador único do usuário.
- **String shortUserId**: Uma versão abreviada do identificador único, usada para visualizações mais curtas.
- **String email**: E-mail do usuário.

#### Construtores

- **Construtor vazio**: Necessário para o Firebase.
- **Construtor com parâmetros**: Inicializa as variáveis `fullUid` e `email`, e define `shortUserId` como os primeiros 6 caracteres de `fullUid`.

# HomeActivity Documentation

## Visão Geral

A `HomeActivity` é a principal atividade onde o usuário pode gerenciar suas informações e configurações de rastreamento de localização. Ela permite ao usuário salvar informações pessoais, alternar a funcionalidade de rastreamento de localização e fazer logout.

## Estrutura do Código

### Importações

O código importa várias classes necessárias para a funcionalidade da atividade, incluindo:

- `android.Manifest`
- `android.content.Intent`
- `android.content.SharedPreferences`
- `android.content.pm.PackageManager`
- `android.location.Location`
- `android.os.Bundle`
- `android.widget.*`
- `androidx.annotation.NonNull`
- `androidx.appcompat.app.AppCompatActivity`
- `com.google.android.gms.location.*`
- `com.google.android.gms.tasks.*`
- `com.google.firebase.auth.FirebaseAuth`
- `com.google.firebase.database.*`

### Constantes e Variáveis

#### Constantes

- **LOCATION_PERMISSION_REQUEST_CODE**: Código de solicitação para permissões de localização.

#### Variáveis de Instância

- **mAuth**: Instância do Firebase Authentication.
- **mDatabase**: Referência ao banco de dados do Firebase.
- **fusedLocationClient**: Cliente para obter atualizações de localização.
- **locationCallback**: Callback para receber atualizações de localização.
- **locationSwitch**: Switch para ativar/desativar rastreamento de localização.
- **userIdTextView**: TextView para exibir o ID do usuário.
- **locationWarningTextView**: TextView para exibir um aviso de localização.
- **userTypeSpinner**: Spinner para selecionar o tipo de usuário.
- **nameEditText**: EditText para inserir o nome do usuário.
- **saveButton**: Botão para salvar as informações do usuário.
- **logoutButton**: Botão para fazer logout.
- **isLocationUpdatesStarted**: Flag para verificar se as atualizações de localização estão ativas.

### Métodos

#### `onCreate(Bundle savedInstanceState)`

Método chamado quando a atividade é criada. Configura a interface do usuário e inicializa variáveis e componentes, incluindo:

- **Configuração dos Elementos da Interface**: Inicializa `FirebaseAuth`, `DatabaseReference`, e `FusedLocationProviderClient`.
- **Configuração do Spinner**: Configura `ArrayAdapter` para o `userTypeSpinner`.
- **Definição de Comportamento**: Define o comportamento dos botões e do switch.
- **Restaurar Estado do Switch**: Restaura o estado do switch de localização a partir das preferências compartilhadas e solicita permissões de localização, se necessário.

#### `saveSwitchState(boolean isChecked)`

Salva o estado atual do switch de localização nas preferências compartilhadas.

#### `logout()`

Desconecta o usuário do Firebase e para o serviço de localização se estiver em execução. Em seguida, redireciona o usuário para `MainActivity`.

#### `checkUserInfo()`

Verifica as informações do usuário no Firebase e preenche os campos da interface do usuário com os dados existentes. Desabilita o switch de localização se as informações do usuário não estiverem disponíveis.

#### `saveUserInfo()`

Salva as informações do usuário (nome e tipo de usuário) no Firebase. Habilita o switch de localização após a conclusão com sucesso.

#### `onLocationSwitchChanged(CompoundButton buttonView, boolean isChecked)`

Manipula a mudança de estado do switch de localização. Inicia ou para o serviço de localização com base no estado do switch e salva o estado do switch nas preferências compartilhadas.

#### `startLocationUpdates()`

Solicita atualizações de localização periódicas usando o `FusedLocationProviderClient`. Define o intervalo de atualização e a prioridade da solicitação de localização.

#### `stopLocationUpdates()`

Para as atualizações de localização, se estiverem ativas.

#### `updateLocationInFirebase(Location location)`

Atualiza a localização do usuário no Firebase com a latitude e longitude fornecidas.

#### `onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)`

Lida com a resposta do usuário à solicitação de permissões de localização. Inicia atualizações de localização se a permissão for concedida e o switch estiver ativado.

### Classes Internas

#### `UserData`

Classe para armazenar as informações do usuário (`userType` e `name`).

- **Construtores**: 
  - Construtor padrão
  - Construtor com parâmetros

#### `LocationData`

Classe para armazenar dados de localização (`latitude` e `longitude`).

- **Construtores**: 
  - Construtor padrão
  - Construtor com parâmetros

# LocationService Documentation

## Descrição

A classe `LocationService` é um serviço Android que coleta atualizações de localização em segundo plano e as envia para o Firebase. O serviço funciona como uma notificação em primeiro plano para garantir que o Android não o mate quando a aplicação estiver em segundo plano. O serviço utiliza o `FusedLocationProviderClient` para obter as atualizações de localização e atualizar a base de dados do Firebase com as informações de localização do usuário.

## Componentes e Funcionalidade

### 1. Constantes

- **CHANNEL_ID**: Identificador do canal de notificação utilizado para mostrar a notificação de primeiro plano.

### 2. Campos

- **fusedLocationClient**: Instância do `FusedLocationProviderClient` usada para obter atualizações de localização.
- **locationCallback**: Instância do `LocationCallback` que processa os resultados de localização.
- **mAuth**: Instância do `FirebaseAuth` para gerenciar a autenticação do usuário.
- **mDatabase**: Referência ao banco de dados do Firebase para atualizar a localização do usuário.

### 3. Métodos

#### `onCreate()`

- Inicializa o serviço, o cliente de localização, a autenticação do Firebase e a referência ao banco de dados.
- Cria um canal de notificação para dispositivos com Android Oreo (API 26) ou superior.
- Configura e inicia atualizações de localização com o `LocationRequest` e o `LocationCallback`.

#### `startLocationUpdates(LocationRequest locationRequest)`

- Solicita atualizações de localização se as permissões necessárias estiverem concedidas.
- Se as permissões não forem concedidas, o serviço é parado.

#### `updateLocationInFirebase(Location location)`

- Atualiza a localização do usuário no banco de dados do Firebase com latitude e longitude.

#### `onStartCommand(Intent intent, int flags, int startId)`

- Configura e exibe uma notificação de primeiro plano para garantir que o serviço continue em execução.
- Se as permissões não forem concedidas, o serviço é parado e não será reiniciado.

#### `onDestroy()`

- Remove as atualizações de localização quando o serviço é destruído.

#### `onBind(Intent intent)`

- Retorna `null` porque este serviço não suporta vinculação.

#### `createNotificationChannel()`

- Cria um canal de notificação para dispositivos com Android Oreo (API 26) ou superior.
- Define o nome, descrição e importância do canal de notificação.

### 4. Classes Internas

#### `LocationData`

- Classe auxiliar para armazenar dados de localização (latitude e longitude).
- **Construtores**:
  - Construtor padrão necessário para a deserialização do Firebase.
  - Construtor com parâmetros que inicializa os campos latitude e longitude.

## Uso e Dependências

### Permissões Necessárias

- **ACCESS_FINE_LOCATION**
- **ACCESS_COARSE_LOCATION**

### Dependências

- **com.google.android.gms:play-services-location**: Para obter atualizações de localização.
- **com.google.firebase:firebase-auth**: Para autenticação do usuário.
- **com.google.firebase:firebase-database**: Para atualizar a localização no banco de dados do Firebase.
