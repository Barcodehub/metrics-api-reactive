# 🧪 Ejemplos de Requests - Metrics API

## 📋 Pre-requisitos

1. **MongoDB corriendo**: `mongodb://localhost:27017`
2. **Todos los microservicios iniciados:**
   - users-api (8081)
   - bootcamp-api (8080)
   - capacity-api (8082)
   - metrics-api (8083)

---

## 🔐 Paso 1: Obtener Token de Admin

### Request
```http
POST http://localhost:8081/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```

### Response (200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "email": "admin@example.com",
  "isAdmin": true
}
```

**💡 Guarda el token para usarlo en los siguientes requests**

---

## 📊 Paso 2: Registrar Reporte de Bootcamp

### Request
```http
POST http://localhost:8083/metrics/bootcamp/report
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
X-Message-Id: test-report-001

{
  "bootcampId": 1
}
```

### Response (202 Accepted)
```json
{
  "code": "202",
  "message": "Bootcamp report registration initiated",
  "identifier": "test-report-001",
  "date": "2026-01-22T10:30:45"
}
```

**🔍 Qué sucede en background:**
1. Consulta información del bootcamp (bootcamp-api)
2. Obtiene IDs de usuarios inscritos (bootcamp-api)
3. Consulta capacidades con tecnologías (capacity-api)
4. Obtiene detalles de usuarios (users-api)
5. Calcula métricas:
   - `capacityCount`: Cantidad de capacidades
   - `technologyCount`: Suma total de tecnologías
   - `enrolledUsersCount`: Cantidad de usuarios inscritos
6. Guarda en MongoDB

### ⚠️ Errores Posibles

#### Bootcamp no existe
```json
{
  "code": "404",
  "message": "Bootcamp not found",
  "identifier": "test-report-001",
  "date": "2026-01-22T10:30:45"
}
```

#### Sin token o token inválido
```json
{
  "code": "401",
  "message": "Authentication token is required",
  "identifier": "test-report-001",
  "date": "2026-01-22T10:30:45"
}
```

#### Usuario no es admin
**Response:** `403 Forbidden` (Spring Security lo bloquea antes de llegar al handler)

---

## 🏆 Paso 3: Consultar Bootcamp Más Popular

### Request
```http
GET http://localhost:8083/metrics/bootcamp/most-popular
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
X-Message-Id: test-popular-001
```

### Response (200 OK)
```json
{
  "id": "65b9c8f0e1234567890abcde",
  "bootcampId": 1,
  "bootcampName": "Bootcamp Java Backend",
  "bootcampDescription": "Desarrollo backend con Spring Boot y arquitectura hexagonal",
  "launchDate": "2026-03-01",
  "duration": 90,
  "capacityCount": 4,
  "technologyCount": 12,
  "enrolledUsersCount": 45,
  "enrolledUsers": [
    {
      "userId": 10,
      "userName": "María García López",
      "userEmail": "maria.garcia@example.com"
    },
    {
      "userId": 11,
      "userName": "Juan Carlos Pérez",
      "userEmail": "juan.perez@example.com"
    },
    {
      "userId": 12,
      "userName": "Ana Martínez Ruiz",
      "userEmail": "ana.martinez@example.com"
    }
  ],
  "capacities": [
    {
      "capacityId": 1,
      "capacityName": "Backend Development",
      "technologies": [
        {
          "technologyId": 1,
          "technologyName": "Spring Boot"
        },
        {
          "technologyId": 2,
          "technologyName": "Spring Data JPA"
        },
        {
          "technologyId": 3,
          "technologyName": "Hibernate"
        }
      ]
    },
    {
      "capacityId": 2,
      "capacityName": "Database Management",
      "technologies": [
        {
          "technologyId": 4,
          "technologyName": "PostgreSQL"
        },
        {
          "technologyId": 5,
          "technologyName": "MongoDB"
        },
        {
          "technologyId": 6,
          "technologyName": "Redis"
        }
      ]
    },
    {
      "capacityId": 3,
      "capacityName": "API Design",
      "technologies": [
        {
          "technologyId": 7,
          "technologyName": "REST"
        },
        {
          "technologyId": 8,
          "technologyName": "GraphQL"
        },
        {
          "technologyId": 9,
          "technologyName": "gRPC"
        }
      ]
    },
    {
      "capacityId": 4,
      "capacityName": "Testing",
      "technologies": [
        {
          "technologyId": 10,
          "technologyName": "JUnit"
        },
        {
          "technologyId": 11,
          "technologyName": "Mockito"
        },
        {
          "technologyId": 12,
          "technologyName": "TestContainers"
        }
      ]
    }
  ],
  "createdAt": "2026-01-22T10:30:45",
  "updatedAt": "2026-01-22T11:15:30"
}
```

### 📊 Interpretación de los Datos

- **capacityCount**: 4 capacidades en el bootcamp
- **technologyCount**: 12 tecnologías totales (3+3+3+3)
- **enrolledUsersCount**: 45 estudiantes inscritos
- **enrolledUsers**: Lista completa con nombre y email de cada usuario
- **capacities**: Desglose de cada capacidad con sus tecnologías

### ⚠️ Errores Posibles

#### No hay reportes registrados
```json
{
  "code": "404",
  "message": "No bootcamp reports found",
  "identifier": "test-popular-001",
  "date": "2026-01-22T11:15:30"
}
```

---

## 🎯 Flujo Completo de Prueba

### Escenario: Registrar 3 bootcamps y encontrar el más popular

#### 1. Crear 3 bootcamps
```http
POST http://localhost:8082/capacity/bootcamp
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "name": "Bootcamp Java Backend",
  "description": "Backend con Spring Boot",
  "launchDate": "2026-03-01",
  "duration": 90,
  "capacityIds": [1, 2, 3, 4]
}
```

```http
POST http://localhost:8082/capacity/bootcamp
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "name": "Bootcamp React Frontend",
  "description": "Frontend moderno con React",
  "launchDate": "2026-04-01",
  "duration": 60,
  "capacityIds": [5, 6]
}
```

```http
POST http://localhost:8082/capacity/bootcamp
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "name": "Bootcamp DevOps",
  "description": "CI/CD y automatización",
  "launchDate": "2026-05-01",
  "duration": 45,
  "capacityIds": [7, 8, 9]
}
```

#### 2. Inscribir usuarios en bootcamps
```http
# 45 usuarios en Bootcamp 1 (el más popular)
POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer {user_token}
Content-Type: application/json

{
  "bootcampId": 1
}
```

```http
# 30 usuarios en Bootcamp 2
POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer {user_token}
Content-Type: application/json

{
  "bootcampId": 2
}
```

```http
# 20 usuarios en Bootcamp 3
POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer {user_token}
Content-Type: application/json

{
  "bootcampId": 3
}
```

#### 3. Registrar reportes de los 3 bootcamps
```http
POST http://localhost:8083/metrics/bootcamp/report
Authorization: Bearer {admin_token}
Content-Type: application/json

{"bootcampId": 1}
```

```http
POST http://localhost:8083/metrics/bootcamp/report
Authorization: Bearer {admin_token}
Content-Type: application/json

{"bootcampId": 2}
```

```http
POST http://localhost:8083/metrics/bootcamp/report
Authorization: Bearer {admin_token}
Content-Type: application/json

{"bootcampId": 3}
```

#### 4. Consultar el más popular
```http
GET http://localhost:8083/metrics/bootcamp/most-popular
Authorization: Bearer {admin_token}
```

**✅ Resultado esperado:** Bootcamp 1 (45 usuarios)

---

## 🧪 Validación en MongoDB

### Conectar a MongoDB
```bash
mongosh mongodb://localhost:27017/bootcamp_metrics
```

### Ver todos los reportes
```javascript
db.bootcamp_reports.find().pretty()
```

### Verificar índices
```javascript
db.bootcamp_reports.getIndexes()
```

### Consultar el más popular directamente
```javascript
db.bootcamp_reports
  .find()
  .sort({ enrolledUsersCount: -1 })
  .limit(1)
  .pretty()
```

### Contar documentos
```javascript
db.bootcamp_reports.countDocuments()
```

---

## 📈 Casos de Prueba Adicionales

### Caso 1: Reporte de bootcamp sin usuarios inscritos
```http
POST http://localhost:8083/metrics/bootcamp/report
Authorization: Bearer {admin_token}
Content-Type: application/json

{"bootcampId": 5}
```

**Resultado esperado:**
```json
{
  "enrolledUsersCount": 0,
  "enrolledUsers": [],
  ...
}
```

### Caso 2: Usuario normal intenta registrar reporte
```http
POST http://localhost:8083/metrics/bootcamp/report
Authorization: Bearer {user_token}
Content-Type: application/json

{"bootcampId": 1}
```

**Resultado esperado:** `403 Forbidden`

### Caso 3: Sin token de autenticación
```http
POST http://localhost:8083/metrics/bootcamp/report
Content-Type: application/json

{"bootcampId": 1}
```

**Resultado esperado:** `401 Unauthorized`

### Caso 4: Registrar reporte de bootcamp inexistente
```http
POST http://localhost:8083/metrics/bootcamp/report
Authorization: Bearer {admin_token}
Content-Type: application/json

{"bootcampId": 999}
```

**Resultado esperado:**
```json
{
  "code": "404",
  "message": "Bootcamp not found",
  ...
}
```

---

## 🔄 Verificar Procesamiento Asíncrono

### 1. Enviar request de registro
```http
POST http://localhost:8083/metrics/bootcamp/report
Authorization: Bearer {admin_token}
Content-Type: application/json

{"bootcampId": 1}
```

**Respuesta inmediata:** `202 Accepted` (en milisegundos)

### 2. Verificar logs del servidor
Deberías ver en los logs:
```
INFO: Starting async bootcamp report registration for bootcampId: 1
INFO: Building bootcamp report for bootcampId: 1
INFO: Calling bootcamp service to get bootcamp by id: 1
INFO: Calling capacity service to get capacities with technologies
INFO: Calling user service to get users by IDs
INFO: Bootcamp report saved successfully for bootcampId: 1
INFO: Async bootcamp report registration completed for bootcampId: 1
```

### 3. Consultar MongoDB después de unos segundos
```javascript
db.bootcamp_reports.findOne({ bootcampId: 1 })
```

---

## 📊 Colección de Postman

### Variables de Entorno
```json
{
  "admin_token": "",
  "user_token": "",
  "base_url": "http://localhost:8083",
  "users_url": "http://localhost:8081",
  "capacity_url": "http://localhost:8082"
}
```

### Script para guardar token automáticamente
En la pestaña **Tests** del request de login:
```javascript
pm.test("Save admin token", function () {
    var jsonData = pm.response.json();
    pm.environment.set("admin_token", jsonData.token);
});
```

---

## ✅ Checklist de Pruebas

- [ ] **Autenticación**
  - [ ] Login exitoso de admin
  - [ ] Token JWT válido generado

- [ ] **Registro de Reporte**
  - [ ] Admin registra reporte exitosamente
  - [ ] Respuesta 202 Accepted inmediata
  - [ ] Datos guardados en MongoDB (verificar manualmente)
  - [ ] Usuario normal recibe 403 Forbidden
  - [ ] Sin token recibe 401 Unauthorized
  - [ ] Bootcamp inexistente retorna error apropiado

- [ ] **Consulta Más Popular**
  - [ ] Admin consulta exitosamente
  - [ ] Retorna bootcamp con mayor enrolledUsersCount
  - [ ] Datos de usuarios actualizados
  - [ ] Todas las capacidades y tecnologías presentes
  - [ ] Usuario normal recibe 403 Forbidden

- [ ] **Validación MongoDB**
  - [ ] Documentos guardados correctamente
  - [ ] Índices creados
  - [ ] Formato de datos correcto

---

## 🎯 Resultado Esperado

Al finalizar todas las pruebas, deberías tener:
- ✅ Múltiples reportes guardados en MongoDB
- ✅ Capacidad de identificar el bootcamp más popular
- ✅ Métricas completas de cada bootcamp
- ✅ Seguridad funcionando correctamente
- ✅ Procesamiento asíncrono sin bloqueos

**🎉 ¡Microservicio de métricas funcionando correctamente!**
