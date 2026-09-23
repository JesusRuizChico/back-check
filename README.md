# Backend HabitaCheck

## Arranque

Requiere Java 17, PostgreSQL y las variables `DB_URL`, `DB_USER` y `DB_PASSWORD`
configuradas en el entorno de ejecución del IDE o de la terminal. Por ejemplo,
`DB_URL=jdbc:postgresql://localhost:5432/habitacheck_db`.

Desde la carpeta backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Solo se conservan las migraciones V1 y V2 actuales, sin cambios en su contenido.
Flyway las aplica a una base vacía y Hibernate valida las entidades. Si la base
conserva el historial anterior de V1/V2/V3, puede haber diferencias de checksum o
versiones. No se ha reparado ni borrado ese historial: se debe reconciliar por
separado antes de usar esa base. No desactivar la validación para ocultar el problema.

## Cómo probar los endpoints

Usar un cliente que conserve las cookies, por ejemplo Postman. La autenticación
usa sesión HTTP con JSESSIONID, no JWT ni tablas adicionales. No se usa Firebase.

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

Errores principales: 400 datos inválidos, 401 credenciales inválidas o sesión
ausente, 403 CSRF ausente/inválido y 409 conflicto de datos/correo existente.

Las sesiones están en memoria: se pierden al reiniciar el servidor. El estado y
roles se cargan en la autenticación; el perfil también verifica que la cuenta esté
activa. No se añadió bloqueo por intentos fallidos ni tablas o campos para ello.
Antes de integrar Flutter Web se deben definir sus orígenes CORS; para desplegar
deben configurarse HTTPS y cookies adecuadas al entorno.

## Cambios realizados

- Corregidos import de RegistroRequest y tipos de autoridades.
- Entidad Rol alineada con el VARCHAR(400) y TEXT de tu V2.
- Validaciones de DTOs y respuestas de error coherentes con tus excepciones.
- Perfil con transacción de lectura y actualización del último acceso en login.
- UsuarioPrincipal elimina el hash de la identidad tras autenticar.
- SecurityConfig comparte la estrategia de sesión/CSRF con el login JSON.
- Nuevos AuthController y PerfilController para exponer los servicios existentes.

## Pruebas

Las pruebas usan una base PostgreSQL exclusiva para tests, distinta de desarrollo.
Configurar `TEST_DB_URL`, `TEST_DB_USER` y, si corresponde, `TEST_DB_PASSWORD`.
El perfil test aplica V1 y V2 mediante Flyway y conserva ddl-auto=validate.
Las pruebas insertan usuarios aleatorios y los conservan solo en esa base de prueba.

```powershell
.\mvnw.cmd test
```

AuthIntegrationTests comprueba peticiones HTTP reales, CSRF, validaciones,
duplicados, hash de contraseña, sesión persistente, rotación de sesión y cierre.
