
╔════════════════════════════════════════════════════════════════════╗
║     SISTEMA DE GESTIÓN DE SALAS DE REUNIONES - JAVA & MYSQL       ║
╚════════════════════════════════════════════════════════════════════╝

Este proyecto permite gestionar salas de reuniones, empleados y reservas 
dentro de una empresa, evitando solapamientos de horarios mediante validación 
automática.

───────────────────────────────
 CARACTERÍSTICAS PRINCIPALES
───────────────────────────────
✔ CRUD completo para salas de reuniones
✔ CRUD completo para empleados
✔ CRUD de reservas con control de disponibilidad
✔ Validación automática de conflictos de horario
✔ Interfaz de consola interactiva
✔ Organización modular con paquetes Java
✔ Conexión a base de datos MySQL

──────────────────────────────
ESTRUCTURA DEL PROYECTO
──────────────────────────────
Proyecto_Final/
├── .idea/
├── Documentacion/
├── logs/
│   ├── aplicacion.2025-06-26.log
│   └── aplicacion.log
├── src/
│   ├── main/java/com/empresa/
│   │   ├── empleados/Empleados.java
│   │   ├── reservas/Reservas.java
│   │   └── salas/Salas.java
│   └── resources/Principal.java
│
│   └── test/java/com/empresa/
│       ├── empleados/EmpleadosTest.java
│       ├── reservas/ReservasTest.java
│       └── salas/SalasTest.java
├── target/
├── Modelo E-R.png
├── pom.xml
├── README.md
└── ScriptSQL.sql

──────────────────────────────
REQUISITOS DEL SISTEMA
──────────────────────────────
- Java JDK 17 o superior
- MySQL 8 o superior
- IntelliJ IDEA o Eclipse
- Driver JDBC de MySQL (mysql-connector-j-8.x.jar)
- Conexión a base de datos local: localhost:3306

──────────────────────────────
INSTRUCCIONES DE EJECUCIÓN
──────────────────────────────
1. Abre IntelliJ IDEA o Eclipse
2. Importa el proyecto como Java (estructura de paquetes)
3. Asegúrate de incluir el driver JDBC de MySQL en el classpath
4. Configura la conexión JDBC (usuario, contraseña, URL) en el código
5. Ejecuta Principal.java para iniciar desde la consola

──────────────────────────────
IMPORTAR EN INTELLIJ IDEA
──────────────────────────────
1. Abre IntelliJ IDEA
2. Ve a File > Open y selecciona la carpeta del proyecto
3. Si usas Maven (pom.xml), selecciona "Import Project" y elige "Maven"
4. Asegúrate de que Java 17 esté configurado como JDK
5. Permite que IntelliJ descargue las dependencias automáticamente
6. Ejecuta Principal.java desde resources

──────────────────────────────
IMPORTAR EN ECLIPSE
──────────────────────────────
1. Abre Eclipse
2. Ve a File > Import > "Existing Projects into Workspace" o "Maven > Existing Maven Projects"
3. Selecciona el directorio del proyecto y completa el asistente
4. Configura Java 17 y el driver JDBC en el classpath
5. Ejecuta Principal.java

──────────────────────────────
CREACIÓN DE LA BASE DE DATOS
──────────────────────────────
1. Abre MySQL Workbench como administrador
2. Conéctate a tu base de datos local
3. Ve a File > Run SQL Script y selecciona ScriptSQL.sql
4. Si falla, abre una "New Query Tab" y carga ScriptSQL.sql manualmente
5. Ejecuta el script para crear tablas y datos de prueba
6. Conéctate con "Database > Connect to Database" y presiona OK
