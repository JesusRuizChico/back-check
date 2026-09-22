# Cómo consumir el login desde Flutter

El backend corre en el puerto `8080`.

```text
http://172.20.10.2:8080
```

Para iniciar sesión tienen que hacer dos peticiones.

## 1. Pedir el token

Primero hagan una petición `GET` a:

```text
http://172.20.10.2:8080/api/csrf
```

La respuesta les dará un token parecido a este:

```json
{
  "token": "TOKEN_RECIBIDO",
  "headerName": "X-XSRF-TOKEN"
}
```

Guarden el valor de ese token, porque lo necesitan para hacer el login.
También deben conservar las cookies que reciban en esta petición.

## 2. Hacer login

Después hagan una petición `POST` a:

```text
http://172.20.10.2:8080/api/auth/login
```

En el body manden las credenciales en formato JSON:

```json
{
  "correo": "ejemplo@ejemplo.mx",
  "contrasena": "Ejempo123"
}
```

También deben mandar este header:

```text
X-XSRF-TOKEN: TOKEN_RECIBIDO
```

Reemplacen `TOKEN_RECIBIDO` por el token que obtuvieron en el primer paso.

La petición debe conservar las cookies. La más importante es `JSESSIONID`,
porque esa cookie mantiene la sesión iniciada.

No se devuelve un JWT. La sesión se mantiene con la cookie `JSESSIONID`, por
eso Flutter debe guardarla y enviarla en las siguientes peticiones.

## Dirección del backend

Si Flutter corre en la misma computadora, pueden usar:

```text
http://172.20.10.2:8080
```
