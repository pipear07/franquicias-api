# Franquicias API

**Prueba tecnica**: API reactiva construida con Spring Boot WebFlux y MongoDB, usando arquitectura hexagonal.

**Ramas**: Existen dos ramas llamadas "main" y "master" ambas estan actualizadas entonces no hay ningun problema, pueden escoger cualquiera.

---

## Descripción

Esta aplicacion gestiona una colección de **franquicias**, cada una con multiples **sucursales**, y cada sucursal con multiples **productos** que tienen un nombre y una cantidad de stock.

**Tecnologías**:
- Java 21, Spring Boot 3.4.5, Spring WebFlux  
- MongoDB (Persistencia reactiva)  
- ModelMapper para mapeo de entidades  
- Docker y Docker Compose  
- Lombok, Reactor  
- Swagger (springdoc-openapi)

---

## Arquitectura y decisiones de diseño

1. **Hexagonal / Clean Architecture**:  
   - `com.franquicias.domain` (Modelos, casos de uso / servicios)  
   - `com.franquicias.infrastructure` (Adaptadores de MongoDB, repositorios)  
   - `com.franquicias.api` (Controladores REST, DTOs)

2. **Reactividad**:  
   - Uso de Spring WebFlux y `reactor-core` para flujos no bloqueantes.  
   - Operadores `map`, `flatMap`, `switchIfEmpty` y manejo de señales `onNext`, `onError`, `onComplete`.  
   - ReactiveMongoRepository para acceso a datos.

3. **ModelMapper**:  
   - Bean global configurado para convertir entidades Mongo a modelos de dominio y viceversa.

4. **Logging**:  
   - Uso de `slf4j` a través de Lombok `@Slf4j` en servicios, controladores y adaptadores.

5. **Validación**:  
   - Validaciones `@Valid` y `@NotBlank` en DTOs.  
   - Excepciones `ResponseStatusException` para errores HTTP.

---

## Requisitos previos

- JDK 21  
- Maven 3.8+  
- Docker & Docker Compose (opcional para contenedores)  

---

## Ejecución local (sin Docker)

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/pipear07/franquicias-api.git
   cd franquicias-api
   ```

2. Exportar la variable de entorno en PowerShell (esa variable es la que le indica al contenedor como llegar a la base de datos en MongoDB Atlas):
   ```bash
   $env:SPRING_DATA_MONGODB_URI = "mongodb+srv://pipear07:Afam2030@cluster0.abzvzny.mongodb.net/franquicias?retryWrites=true&w=majority"
   ```

2. Ejecutar con Maven:
   ```bash
   mvn clean spring-boot:run
   ```

3. Acceder a la API en `http://localhost:8080`  
   Swagger UI: `http://localhost:8080/swagger-ui.html`

> **Nota**: Deben tener MongoDB corriendo en `localhost:27017`, base de datos `franquicias`.

---

## Terraform (ECS Fargate)
(Terraform inyecta esa variable en la definición de la tarea ECS/Fargate, pero no se conecta directamente a MongoDB)

- Codigo de aplicacion: una API en Spring Boot que expone endpoints reactivos y se conecta a MongoDB Atlas via la variable de entorno SPRING_DATA_MONGODB_URI.

- Infraestructura como codigo con Terraform: Se creo un Cluster de ECS Fargate, define la Task Definition con la imagen Docker y las variables de entorno, configura un   Application Load Balancer con su Target Group y Security Groups, y despliega todo en la VPC existente usando las subnets públicas. Quedo pendiente por temas de permisos y por los costos de la infraestructura, quedo pendiente arreglar que escuchara desde afuera del contenedor con la direccion ip pública para que pueda acceder a internet.

- Complete terraform.tfvars with your values:
  region        = "us-east-2"
  vpc_id        = "vpc-036aea0e84d497cba"
  subnet_ids    = ["subnet-04e0b7808ee7d3088","subnet-06160a399b2d0d631","subnet-08c0ed5fc13597c6f"]
  image         = "pipear07/franquicias-api:latest"
  container_port = 8080
  env_vars = {
    SPRING_DATA_MONGODB_URI = "mongodb+srv://pipear07:Afam2030@cluster0.abzvzny.mongodb.net/franquicias?retryWrites=true&w=majority"
  }

- Init & apply:
  cd infra/terraform/atlas/ecs-fargate
  terraform init
  terraform apply


## Elastic Beanstalk
--------------------
cd C:/Repos2/franquicias-api
eb init franquicias-api --platform "Docker running on 64bit Amazon Linux 2" --region us-east-2
eb use franquicias-vpc-single
eb deploy


## Comandos Docker

- Construir el JAR:
   ```bash
   mvn clean package -DskipTests
   ```
- Exportar la variable de entorno en PowerShell (esa variable es la que le indica al contenedor como llegar a la base de datos en MongoDB Atlas):
   ```bash
   $env:SPRING_DATA_MONGODB_URI = "mongodb+srv://pipear07:Afam2030@cluster0.abzvzny.mongodb.net/franquicias?retryWrites=true&w=majority"
   ```
- Construir imagen:
  ```bash
  docker compose build api
  ```
- Levantar contenedores:
  ```bash
  docker compose up -d
  ```
- Ver logs:
  ```bash
  docker compose logs -f
  ```
- Ver logs:
  ```bash
  Swagger UI: `http://localhost:8080/swagger-ui.html`
  ```
  
![Docker Corriendo](src/docs/img/Docker_Run.png)

---

1. Construir el JAR:
   ```bash
   mvn clean package -DskipTests
   ```

2. Levantar Mongo y la API:
   ```bash
   docker compose up --build
   ```

3. Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Endpoints disponibles

Todos los endpoints devuelven y reciben **JSON**.

| Método | Ruta                                                   | Descripción                                 |
|--------|--------------------------------------------------------|---------------------------------------------|
| POST   | `/franchises`                                          | Crear nueva franquicia                      |
| GET    | `/franchises`                                          | Obtener todas las franquicias               |
| GET    | `/franchises/{fid}`                                    | Obtener franquicia por ID                   |
| POST   | `/franchises/{fid}/branches`                           | Agregar sucursal a franquicia               |
| POST   | `/franchises/{fid}/branches/{bid}/products`            | Agregar producto a sucursal                 |
| DELETE | `/franchises/{fid}/branches/{bid}/products/{pid}`      | Eliminar producto de sucursal               |
| PATCH  | `/franchises/{fid}/branches/{bid}/products/{pid}/stock`| Actualizar stock de producto                 |
| GET    | `/franchises/{fid}/top-stock`                         | Top stock por franquicia                    |
| PATCH  | `/franchises/{fid}`                                    | Renombrar franquicia                        |
| PATCH  | `/franchises/{fid}/branches/{bid}`                     | Renombrar sucursal                          |
| PATCH  | `/franchises/{fid}/branches/{bid}/products/{pid}`      | Renombrar producto                          |

---

## Pruebas unitarias

- Ejecutar:
  ```bash
  mvn test
  ```
- Cobertura con JaCoCo:
  ```bash
  mvn jacoco:report
  ```
- Ver informe HTML en `target/site/jacoco/index.html`.
   Comando: explorer.exe .\target\site\jacoco\index.html

![Pruebas Unitarias con Jacoco](src/docs/img/Unit_test_jacoco.png)
---

## Versionado y despliegue

- **v1.0**: Endpoints basicos.  
- **v1.1-docker**: Empaquetado Docker
- **v1.2-rename**: Endpoints PATCH rename  
- **v1.4-readme**: Documentacion completa
- **v1.5-Jacoco**: Cobertura del 80% e informe JaCoCo incluido
- **v1.6-MongoDB_Atlas**: MongoDb_Atlas
- **v7.0-ServerlessFreeWithTerraform**: ServerlessFreeWithTerraform


---

## Amazon Web Service (Lo que alcance a configurar en la Nube)

![AWS - Bad Request but I was already listening to the app](src/docs/img/Bad_request_AWS.png)

![AWS - Elastic Beanstalk](src/docs/img/AWS_Elastic_Beanstalk.png)

![AWS - Franquicias ecs-env](src/docs/img/AWS_Franquicias-ecs-env.png)

![AWS - VPC](src/docs/img/AWS_VPC.png)

![AWS - IAM](src/docs/img/AWS_IAM.png)


---

## MongoDB 

![MongoDB Atlas](src/docs/img/Mongo_DB_Atlas.png)


---

## Consideraciones de diseño y finales

1. **Reactividad y back-pressure**: con WebFlux y Reactor, el servidor maneja peticiones sin bloquear hilos, escalando eficazmente bajo carga alta.  
2. **Clean Architecture**: separa claramente dominio (modelos y servicios), infraestructura (adaptadores Mongo) y API (controladores y DTOs), facilitando pruebas y mantenibilidad.  
3. **ModelMapper**: configurado como bean unico para centralizar mapeos de entidades a DTOs, evitando código boilerplate en servicios.  
4. **Logging estructurado**: SLF4J y Logback permiten trazar el flujo Reactor (`onNext`, `onError`, `onComplete`) y diagnosticar fallos en producción.  
5. **Validación y manejo de errores**: uso de `@Valid`, `ResponseStatusException` y operadores `switchIfEmpty` para devolver HTTP 4xx claros en lugar de 500 genericos.  
6. **Contenedorizacion con Docker**: Dockerfile minimal y Docker Compose aseguran entornos reproducibles y facilitan despliegues en la nube.

7. **AWS**: Brinda una infraestructura global, escalable y altamente disponible; usar ECS Fargate elimina la gestión de servidores y simplifica el despliegue de contenedores.

8. **Terraform**: permite definir toda la infraestructura como código, facilitando versionado, reproducibilidad y automatización en entornos de desarrollo, staging y produccion.

9. **MongoDB**: Atlas ofrece una base de datos gestionada en la nube con replicas, respaldo automático y seguridad integrada, liberando de tareas operativas de mantenimiento.

10. **En conjunto**, este stack garantiza despliegues fiables, seguros y fáciles de replicar, acelerando el time-to-market y reduciendo el esfuerzo operativo.
---


**Se incluye un documento PDF sobre la ejecucion de cada uno de los endpoints en la ruta raiz llamada src/docs/DocumentacionPruebaNequi.pdf**


---

Para dudas, contactame a `pipear07@hotmail.com`
