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

HomeActivity é a atividade principal de uma aplicação Android para rastreamento de alunos, que permite aos usuários gerenciar suas informações de perfil e ativar/desativar o rastreamento de localização em segundo plano. A classe interage com o Firebase para salvar e recuperar dados do usuário, além de controlar o serviço de localização através de um `Switch`.

## Componentes e Funcionalidade

### Constantes:
- `LOCATION_PERMISSION_REQUEST_CODE`: Código de requisição usado ao solicitar permissões de localização.

### Campos:
- **`FirebaseAuth mAuth`**: Instância do FirebaseAuth usada para autenticação do usuário.
- **`DatabaseReference mDatabase`**: Referência ao Firebase Database para salvar e recuperar informações do usuário.
- **`Switch locationSwitch`**: Switch usado para ativar ou desativar o serviço de localização.
- **`TextView userIdTextView`**: Exibe um trecho do ID do usuário quando o serviço de localização está ativo.
- **`TextView locationWarningTextView`**: Exibe uma mensagem de aviso relacionada ao rastreamento de localização.
- **`Spinner userTypeSpinner`**: Spinner para seleção do tipo de perfil do usuário (ex.: estudante, professor).
- **`EditText nameEditText`**: Campo de entrada para o nome do usuário.
- **`Button saveButton`**: Botão para salvar as informações do usuário no Firebase.
- **`Button logoutButton`**: Botão para realizar o logout do usuário e encerrar a sessão no Firebase.

### Métodos:

#### `onCreate(Bundle savedInstanceState)`
- Inicializa a interface e os componentes, incluindo o FirebaseAuth, FirebaseDatabase e widgets da interface.
- Configura o Spinner com os tipos de usuários e verifica se as informações do usuário já estão armazenadas no Firebase.
- Configura os listeners para os botões e o switch de localização.
- Recupera o estado do switch de localização usando SharedPreferences e solicita permissões de localização se necessário.

#### `saveSwitchState(boolean isChecked)`
- Salva o estado atual do switch de localização no SharedPreferences para persistência entre as sessões.

#### `logout()`
- Desmarca o switch de localização, salva o estado, realiza o logout do Firebase e redireciona o usuário para a `MainActivity`.

#### `checkUserInfo()`
- Verifica se o usuário tem informações salvas no banco de dados do Firebase.
- Se as informações existirem, preenche os campos de nome e tipo de usuário e habilita o switch de localização. Caso contrário, o switch de localização é desabilitado.

#### `saveUserInfo()`
- Salva as informações do usuário no Firebase. Se o campo de nome estiver vazio, exibe uma mensagem de erro.
- Após salvar as informações com sucesso, o switch de localização é habilitado.

#### `onLocationSwitchChanged(CompoundButton buttonView, boolean isChecked)`
- Controla o início ou parada do serviço de localização com base no estado do switch.
- Exibe ou oculta o ID do usuário e a mensagem de aviso de localização.
- Salva o estado do switch.

#### `startLocationService()`
- Inicia o serviço de localização (`LocationService`) se o switch estiver ativado.

#### `stopLocationService()`
- Para o serviço de localização (`LocationService`) se o switch estiver desativado.

#### `onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)`
- Trata o resultado da solicitação de permissões de localização.
- Se a permissão for concedida e o switch estiver ativado, o serviço de localização é iniciado. Caso contrário, o switch é desativado.

### Classe Interna:

#### `UserData`
- Classe auxiliar para armazenar os dados do usuário, incluindo o tipo de usuário e o nome.
- Contém um construtor padrão (necessário para deserialização do Firebase) e um construtor que inicializa os campos `userType` e `name`.

## Uso e Dependências

### Permissões Necessárias:
- **`ACCESS_FINE_LOCATION`**: Necessária para ativar o rastreamento de localização do dispositivo.

### Dependências:
- **Firebase Authentication**: Para autenticar o usuário.
- **Firebase Database**: Para salvar e recuperar informações do usuário.
- **SharedPreferences**: Para salvar o estado do switch de localização entre sessões.

## Autor
Desenvolvido por [Seu Nome].

## Licença
Este projeto está licenciado sob a [Licença MIT](LICENSE).


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
