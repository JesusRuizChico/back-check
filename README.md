# Backend HabitaCheck

## Arranque

Requiere Java 17, PostgreSQL y las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`
configuradas en el entorno de ejecución del IDE o de la terminal. Por ejemplo,
`DB_URL=jdbc:postgresql://localhost:5432/habitacheck_db`.

Desde la carpeta backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Se conserva una migración inicial `V1__base_completa.sql`. Flyway la aplica a una
base vacía y Hibernate valida las entidades. Si una base ya tiene historial de
migraciones anteriores, hay que reconciliarlo antes de usarla; no se ha modificado
ninguna base de datos.

## Cómo probar los endpoints

Usar un cliente que conserve las cookies, por ejemplo Postman. La autenticación
usa sesión HTTP con JSESSIONID, no JWT ni tablas adicionales.

## Fotos y Firebase Storage

Las fotos de perfil y de propiedades se suben desde el backend al bucket de
Firebase Storage. PostgreSQL conserva la URL de descarga, no los bytes. El
backend valida contenido real y extensión; acepta JPG/JPEG, PNG y WEBP, hasta
5 MiB por imagen. Una propiedad admite hasta cinco imágenes.

El proyecto y bucket predeterminados son `habitacheck-3f82b` y
`habitacheck-3f82b.firebasestorage.app`. Se pueden cambiar con las variables
`FIREBASE_PROJECT_ID` y `FIREBASE_STORAGE_BUCKET`.

Para autorizar al servidor, configura Application Default Credentials antes de
iniciarlo. En Windows, por ejemplo:

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS = "C:\ruta\fuera-del-repositorio\firebase-service-account.json"
```

La cuenta de servicio necesita permisos para crear y borrar objetos en el bucket.
Mantén ese archivo fuera del repositorio y no lo compartas. En despliegue, usa el
mecanismo de identidad de servicio disponible en el proveedor; no subas el JSON
de credenciales al servidor como parte del código.

Las URLs de descarga incluyen un token permanente de acceso por enlace. Quien
tenga la URL puede ver la imagen. Si faltan credenciales o Firebase no está
disponible, las operaciones de foto responden con error 503.

1. `GET /api/csrf`: guardar las cookies y leer `token` y `headerName` del JSON.
2. En cada POST enviar el token en la cabecera `X-XSRF-TOKEN` y las cookies.
3. `POST /api/auth/registro`, con `Content-Type: application/json`:

```json
{
  "nombre": "Ana Pérez",
  "correo": "ana@example.com",
  "contrasena": "MiClaveDePrueba123!",
  "telefono": "4271234567",
  "rolSolicitado": "arrendatario"
}
```

Devuelve 201 y los datos públicos; registrarse no inicia sesión. Se conservan los
roles públicos de tu servicio: arrendatario, arrendador y proveedor. El registro de
especialidades del proveedor no forma parte de este flujo todavía. Administrador
no se permite en el registro público.

La contraseña de registro exige un mínimo de 6 caracteres, acorde al frontend
actual, y un máximo de 72 bytes UTF-8 por usar BCrypt. El teléfono es opcional.

4. `POST /api/auth/login`:

```json
{"correo":"ana@example.com","contrasena":"MiClaveDePrueba123!"}
```

Devuelve 200 y el perfil; conservar la cookie JSESSIONID. El backend actualiza
ultimo_acceso y renueva el identificador de sesión cuando ya existía una sesión.

5. Volver a llamar `GET /api/csrf`: el token anterior se invalida en el login.
6. `GET /api/perfil`: devuelve el perfil de quien inició sesión, sin aceptar un ID
   de usuario enviado por el cliente. No permite editar el perfil todavía.
7. `POST /api/auth/logout`, con CSRF y cookies actuales: devuelve 204, invalida
   la sesión y elimina la cookie. El perfil vuelve a responder 401.
8. Obtener nuevamente CSRF antes de otra operación POST tras cerrar sesión.

## Conversaciones y chat

Las conversaciones requieren una sesión iniciada y conservan una sola conversación
por pareja de usuarios, aunque el arrendatario llegue desde otra propiedad del
mismo arrendador. La propiedad solo se usa para localizar al arrendador al iniciar;
no se guarda su ID en la conversación.

- `POST /api/conversaciones/propiedad/{idPropiedad}/mensajes`: abre o reutiliza el
  chat y envía el primer mensaje. Enviar `{ "contenido": "¿Sigue disponible?" }`.
  Devuelve 201 cuando crea la conversación y 200 cuando ya existía. Incluye
  `avisoRespuestaLenta` cuando el arrendador no ha iniciado sesión en los últimos
  cinco días, o nunca ha iniciado sesión.
- `GET /api/conversaciones`: lista los chats del usuario, el otro participante,
  el último mensaje y el número de mensajes no leídos.
- `GET /api/conversaciones/{id}/mensajes?limite=50`: carga los mensajes más
  recientes. Para cargar anteriores, repetir con `antesDe={siguienteAntesDe}` de
  la respuesta. El límite permitido es de 1 a 100.
- `POST /api/conversaciones/{id}/mensajes`: envía un mensaje por HTTP. También se
  puede enviar por el canal WebSocket descrito abajo. El contenido no puede estar
  vacío ni superar 4000 caracteres.
- `PUT /api/conversaciones/{id}/lectura`: marca como leídos los mensajes recibidos.

Para tiempo real, conectar por STOMP a `ws://localhost:8080/ws` conservando las
cookies `JSESSIONID` y `XSRF-TOKEN`. En el frame `CONNECT`, enviar el token CSRF
actual en el encabezado `X-XSRF-TOKEN`; se obtiene de `GET /api/csrf`. Después de
conectar:

```text
SUBSCRIBE /topic/conversaciones/{idConversacion}
SEND      /app/conversaciones/{idConversacion}/mensajes
```

El cuerpo de `SEND` es JSON, por ejemplo `{"contenido":"Hola"}`. El canal publica
eventos `MENSAJE` y `LECTURA`; los errores se envían a `/user/queue/errores`. Un
evento de mensaje tiene esta forma:

```json
{
  "tipo": "MENSAJE",
  "mensaje": {
    "idMensaje": 18,
    "idEmisor": 42,
    "nombreEmisor": "Ana Pérez",
    "contenido": "Hola",
    "fechaEnvio": "2026-09-27T12:00:00Z",
    "fechaLectura": null
  },
  "idUsuario": 42,
  "fechaEvento": "2026-09-27T12:00:00Z",
  "avisoRespuestaLenta": false
}
```

En `LECTURA`, `mensaje` es `null` e `idUsuario` indica quién leyó los mensajes.
El servidor valida la sesión, el token CSRF y que el usuario participe en la
conversación para poder leer el canal o enviar mensajes. Los orígenes WebSocket
permitidos actualmente son `localhost` y `127.0.0.1`; al desplegar el frontend
habrá que agregar su origen HTTPS.

El broker en memoria permite chat en tiempo real mientras la aplicación corre en
una sola instancia. Todavía no hay notificaciones push ni generación automática
de resúmenes a los 14 días; tampoco se eliminan mensajes antiguos.

Errores principales: 400 datos inválidos, 401 credenciales inválidas o sesión
ausente, 403 CSRF ausente/inválido, 404 conversación inexistente o ajena y 409
conflicto de datos/correo existente.

Las sesiones están en memoria: se pierden al reiniciar el servidor. El estado y
roles se cargan en la autenticación; el perfil también verifica que la cuenta esté
activa. El login reinicia los intentos fallidos al autenticarse correctamente y
bloquea la cuenta durante cinco minutos después de cinco intentos fallidos.
Antes de integrar Flutter Web se deben definir sus orígenes CORS; para desplegar
deben configurarse HTTPS y cookies adecuadas al entorno.

## Cambios realizados

- Corregidos import de RegistroRequest y tipos de autoridades.
- Entidad Rol alineada con `roles.nombre` VARCHAR(40) y `roles.descripcion` TEXT de V1.
- Validaciones de DTOs y respuestas de error coherentes con tus excepciones.
- Perfil con transacción de lectura y actualización del último acceso en login.
- UsuarioPrincipal elimina el hash de la identidad tras autenticar.
- SecurityConfig comparte la estrategia de sesión/CSRF con el login JSON.
- Nuevos AuthController y PerfilController para exponer los servicios existentes.

## Pruebas

Las pruebas de integración usan una base PostgreSQL exclusiva para tests, distinta
de desarrollo. Configurar `TEST_DB_URL`, `TEST_DB_USER` y, si corresponde,
`TEST_DB_PASSWORD`. El perfil test aplica la migración V1 mediante Flyway y
conserva `ddl-auto=validate`.

```powershell
.\mvnw.cmd test
```

AuthIntegrationTests comprueba peticiones HTTP reales, CSRF, validaciones,
duplicados, hash de contraseña, sesión persistente, rotación de sesión y cierre.
