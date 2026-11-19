# HabitaCheck — Backend (Spring Boot)

API REST para la app HabitaCheck (Flutter). Maneja cuentas de **inquilino** (`arrendatario`) y
**arrendador**, publicación de propiedades con fotos, favoritos y chat entre ambos perfiles.

## Requisitos

- Java 17 o superior
- PostgreSQL 16 (con Docker o instalado localmente)

## Opción A: Docker

Solo la base de datos (el backend se ejecuta desde el IDE o con Maven):

```bash
docker compose up -d db
```

```bash
./mvnw spring-boot:run
```

Base de datos y backend juntos en contenedores:

```bash
docker compose up -d --build
```

## Opción B: PostgreSQL instalado localmente

Crea la base de datos y ajusta las credenciales con variables de entorno si no usas las de por defecto:

```bash
createdb -U postgres habitacheck_db
```

```bash
DB_URL=jdbc:postgresql://localhost:5432/habitacheck_db DB_USER=postgres DB_PASSWORD=password ./mvnw spring-boot:run
```

En Windows: `.\mvnw.cmd spring-boot:run` (definiendo las mismas variables en el IDE o la terminal).

| Variable | Valor por defecto |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/habitacheck_db` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `password` |
| `UPLOADS_DIR` | `uploads` (carpeta donde se guardan las fotos) |
| `CORS_ORIGINS` | `http://localhost:*,http://127.0.0.1:*` |
| `PORT` | `8080` |

Flyway crea el esquema (`V1__base_completa.sql`) y aplica los ajustes de `V2__version_funcional.sql`
(características de la propiedad, biografía, favoritos, catálogo de amenidades y retiro del perfil de
prestador de servicios). La base arranca vacía: el catálogo del inquilino se llena cuando un arrendador
publica.

## Imágenes

Las fotos de perfil y de propiedades se validan (JPG, PNG o WEBP reales, máximo 5 MB, hasta 5 por
propiedad) y se guardan en `UPLOADS_DIR`. Se sirven públicamente en `GET /uploads/{carpeta}/{archivo}`;
la base de datos guarda la ruta relativa (`/uploads/propiedades/...`).

## Endpoints

La autenticación usa sesión HTTP (`JSESSIONID`) y protección CSRF: obtener el token con
`GET /api/csrf` y enviarlo en el encabezado `X-XSRF-TOKEN` en cada `POST`, `PUT`, `PATCH` o `DELETE`.

| Método | Ruta | Quién | Descripción |
| --- | --- | --- | --- |
| GET | `/api/csrf` | público | Token CSRF |
| POST | `/api/auth/registro` | público | Alta con `rolSolicitado` = `arrendatario` o `arrendador` |
| POST | `/api/auth/login` | público | Inicia sesión (bloqueo de 5 min tras 5 intentos fallidos) |
| POST | `/api/auth/logout` | sesión | Cierra sesión |
| GET / PUT | `/api/perfil` | sesión | Consultar / editar nombre, teléfono y biografía |
| PUT | `/api/perfil/contrasena` | sesión | Cambiar contraseña |
| POST / DELETE | `/api/perfil/foto` | sesión | Subir (`multipart`, campo `foto`) / quitar foto |
| GET | `/api/servicios` | sesión | Amenidades disponibles para publicar |
| GET | `/api/propiedades` | sesión | Catálogo de propiedades disponibles (con `esFavorito`) |
| GET | `/api/propiedades/{id}` | sesión | Detalle |
| POST | `/api/propiedades` | arrendador | Publicar (`multipart`: datos, `serviciosIds[n]` e `imagenes`) |
| GET | `/api/propiedades/mis-propiedades` | arrendador | Propiedades propias |
| PATCH | `/api/propiedades/{id}/estado` | arrendador | `disponible`, `pausada` o `rentada` |
| DELETE | `/api/propiedades/{id}` | arrendador | Baja lógica |
| GET | `/api/favoritos` | arrendatario | Favoritos |
| PUT / DELETE | `/api/favoritos/{idPropiedad}` | arrendatario | Agregar / quitar favorito |
| GET | `/api/conversaciones` | sesión | Bandeja de chats con no leídos |
| GET | `/api/conversaciones/propiedad/{id}` | arrendatario | Chat existente con el arrendador de la propiedad |
| POST | `/api/conversaciones/propiedad/{id}/mensajes` | arrendatario | Abre el chat enviando el primer mensaje |
| GET | `/api/conversaciones/{id}/mensajes` | participante | Mensajes (`limite`, `antesDe`) |
| POST | `/api/conversaciones/{id}/mensajes` | participante | Enviar mensaje |
| PUT | `/api/conversaciones/{id}/lectura` | participante | Marcar como leídos |

El chat también publica eventos en tiempo real por STOMP (`ws://localhost:8080/ws`,
tópico `/topic/conversaciones/{id}`).

## Arquitectura (SOLID)

```
controller/      # Entrada HTTP; solo dependen de interfaces de servicio
service/         # Interfaces (contratos) de los casos de uso
service/impl/    # Implementaciones
repository/      # Acceso a datos (Spring Data JPA)
mapper/          # Conversión entidad → DTO
dto/             # Peticiones y respuestas
entity/          # Modelo JPA
security/        # Sesión, CSRF, autenticación y canal del chat
config/          # Configuración (CORS, almacenamiento, contraseñas, WebSocket)
exception/       # Errores de negocio y manejador global
```

Ejemplos: `GestionPropiedadService` (arrendador) y `CatalogoPropiedadService` (consultas) están separados
(ISP); el almacenamiento de imágenes se abstrae en `ImageStorage`, por lo que cambiar de disco local a
otro proveedor solo requiere una nueva implementación (OCP/DIP).

## Pruebas

Usan una base PostgreSQL exclusiva para pruebas:

```bash
TEST_DB_URL=jdbc:postgresql://localhost:5432/habitacheck_test TEST_DB_USER=postgres TEST_DB_PASSWORD=password ./mvnw test
```
