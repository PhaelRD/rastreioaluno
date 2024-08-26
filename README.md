### MainActivity

A classe `MainActivity` é a atividade principal do aplicativo. Ela gerencia o fluxo de autenticação e registro de usuários.

#### Variáveis Globais

- **`FirebaseAuth mAuth`:** Instância do Firebase Authentication usada para autenticar o usuário.
- **`DatabaseReference mDatabase`:** Referência ao Firebase Realtime Database, usada para salvar dados do usuário.
- **`EditText loginEmailEditText, loginPasswordEditText`:** Campos de entrada para o e-mail e senha no formulário de login.
- **`Button loginButton, forgotPasswordButton`:** Botões de login e recuperação de senha.
- **`EditText registerEmailEditText, registerPasswordEditText, registerConfirmPasswordEditText`:** Campos de entrada para o e-mail, senha e confirmação de senha no formulário de registro.
- **`Button registerButton`:** Botão de registro.

#### Método `onCreate`

Este é o método principal da atividade, que é chamado quando a atividade é criada. As seguintes operações são realizadas:

1. **Inicialização do Firebase:** O Firebase é inicializado com `FirebaseApp.initializeApp(this)`.
2. **Verificação de Usuário Logado:** Se o usuário já estiver logado, ele é redirecionado para a `HomeActivity`.
3. **Configuração de Ouvintes de Cliques:**
   - **Login:** Chama o método `loginUser` para autenticar o usuário.
   - **Registro:** Chama o método `registerUser` para registrar um novo usuário.
   - **Recuperação de Senha:** Envia um e-mail de redefinição de senha ao usuário.

### Métodos Auxiliares

#### `registerUser`

Este método registra um novo usuário no Firebase Authentication e salva seus dados no Firebase Realtime Database.

- **Entrada:** E-mail, senha, confirmação de senha.
- **Validação:** Verifica se as senhas coincidem antes de tentar registrar o usuário.
- **Criação de Usuário:** Usa `mAuth.createUserWithEmailAndPassword` para registrar o usuário.
- **Salvamento no Banco de Dados:** Após o registro bem-sucedido, o objeto `User` é salvo no Realtime Database.

#### `loginUser`

Este método realiza a autenticação de um usuário existente no Firebase Authentication.

- **Entrada:** E-mail, senha.
- **Autenticação:** Usa `mAuth.signInWithEmailAndPassword` para autenticar o usuário.
- **Redirecionamento:** Após o login bem-sucedido, o usuário é redirecionado para a `HomeActivity`.

#### `handleFirebaseAuthError`

Este método trata os erros retornados pelo Firebase Authentication.

- **Entrada:** Exceção do Firebase.
- **Tratamento de Erros Específicos:** Identifica o código de erro retornado e exibe uma mensagem apropriada ao usuário (por exemplo, e-mail inválido, senha fraca, etc.).

### Classe `User`

A classe `User` é um modelo que representa o usuário no Firebase Realtime Database.

- **Atributos:**
  - `fullUid`: ID completo do usuário (gerado pelo Firebase Authentication).
  - `shortUserId`: ID abreviado (primeiros 6 caracteres do UID completo).
  - `email`: Endereço de e-mail do usuário.
- **Construtores:** A classe possui um construtor padrão necessário para o Firebase e um construtor que inicializa os atributos `fullUid` e `email`.

### Fluxo de Navegação

1. **Tela Inicial (`MainActivity`):**
   - Exibe os formulários de login e registro.
   - Se o usuário já estiver logado, redireciona diretamente para a `HomeActivity`.
2. **Login Bem-Sucedido:** Redireciona para `HomeActivity`.
3. **Registro Bem-Sucedido:** Salva o usuário no Realtime Database e redireciona para `HomeActivity`.
4. **Recuperação de Senha:** Envia um e-mail para redefinir a senha do usuário.

### Tratamento de Erros

O método `handleFirebaseAuthError` trata os erros de autenticação específicos do Firebase, exibindo mensagens de erro amigáveis ao usuário. Ele trata os seguintes códigos de erro:

- `ERROR_INVALID_EMAIL`
- `ERROR_EMAIL_ALREADY_IN_USE`
- `ERROR_WEAK_PASSWORD`
- `ERROR_USER_NOT_FOUND`
- `ERROR_WRONG_PASSWORD`
- `ERROR_USER_DISABLED`
