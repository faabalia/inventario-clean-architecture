# 🚀 Guía de Ejecución del Backend

Esta guía contiene los pasos necesarios para ejecutar la aplicación de backend y sus pruebas en un entorno de desarrollo macOS.

## 1. Requisitos Previos

Asegúrate de tener el siguiente software instalado en tu Mac.

### a. Homebrew
El gestor de paquetes para macOS. Si no lo tienes, instálalo desde [brew.sh](https://brew.sh/index_es).

### b. Java 17 (JDK)
Instálalo fácilmente a través de Homebrew:
```sh
brew install openjdk@17
```
Verifica la instalación con `java -version`.

### c. Docker Desktop para Mac
Descárgalo desde el [sitio oficial de Docker](https://www.docker.com/products/docker-desktop/) e instálalo. **Es crucial que Docker Desktop esté en ejecución** para los siguientes pasos.

---

## 2. Ejecutar la Aplicación Principal

La aplicación requiere una base de datos PostgreSQL para funcionar. La levantaremos usando Docker.

### Paso 1: Iniciar la Base de Datos
Abre una terminal y ejecuta este comando para crear un contenedor Docker con la base de datos configurada según tu archivo `application.properties`.

```sh
docker run --name inventario-db \
  -e POSTGRES_USER=inventario_user \
  -e POSTGRES_PASSWORD=inventario_password \
  -e POSTGRES_DB=inventario_db \
  -p 5432:5432 \
  -d postgres:16-alpine
```
Este comando solo necesitas ejecutarlo la primera vez.

### Paso 2: Ejecutar la Aplicación
Con la base de datos en funcionamiento, navega a la raíz del proyecto en tu terminal y ejecuta:

```sh
./mvnw spring-boot:run
```
> **Nota**: Si recibes un error de `Permission denied`, ejecuta `chmod +x ./mvnw` una única vez para dar permisos al script.

La aplicación arrancará y estará disponible en `http://localhost:8080`.

---

## 3. Ejecutar las Pruebas

El proyecto utiliza **Testcontainers**, por lo que las pruebas levantan su propia base de datos temporal y no dependen de la que creaste en el paso anterior.

### Paso Único: Ejecutar el Comando de Test
Asegúrate de que Docker Desktop esté corriendo y, desde la raíz del proyecto, ejecuta:

```sh
./mvnw test
```
Este comando compilará todo el código y ejecutará tanto las pruebas unitarias como las de integración. Si todo es correcto, verás un mensaje final de `BUILD SUCCESS`.

---

## 4. Comandos Útiles de Docker

Aquí tienes una referencia rápida para gestionar el contenedor de la base de datos de desarrollo.

```sh
# Listar los contenedores que están en ejecución
docker ps

# Detener la base de datos
docker stop inventario-db

# Iniciar la base de datos (si ya fue creada y está detenida)
docker start inventario-db

# Eliminar el contenedor (debe estar detenido primero)
docker rm inventario-db

# Forzar la detención y eliminación del contenedor en un solo paso
docker rm -f inventario-db

# Listar todos los contenedores (incluso los detenidos)
docker ps -a
```
