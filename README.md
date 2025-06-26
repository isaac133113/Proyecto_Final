SISTEMA DE GESTIÓN DE SALAS DE REUNIONES
========================================

Este proyecto en Java permite gestionar salas de reuniones, empleados y reservas dentro de una empresa. Incluye validación automática de conflictos de horario para evitar reservas solapadas.

----------------------------------------
CARACTERÍSTICAS PRINCIPALES
----------------------------------------

- CRUD completo para salas de reuniones  
- CRUD completo para empleados  
- CRUD de reservas con control de disponibilidad  
- Validación de solapamiento de horarios  
- Interfaz de consola interactiva  
- Organización modular con paquetes Java  
- Conexión a base de datos MySQL  

----------------------------------------
ESTRUCTURA DEL PROYECTO
----------------------------------------

src/  
└── com/  
  └── empresa/  
    ├── salas/          → Gestión CRUD de salas  
    │  └── Salas.java  
    ├── empleados/      → Gestión CRUD de empleados  
    │  └── Empleados.java  
    ├── reservas/       → Gestión de reservas con validación de conflictos  
    │  └── Reservas.java  
    └── Main.java       → Punto de entrada con menú principal  

----------------------------------------
REQUISITOS
----------------------------------------

- Java JDK 17 o superior  
- MySQL 8 o superior  
- IntelliJ IDEA o Eclipse  
- Driver JDBC de MySQL (ej: mysql-connector-j-8.x.jar) incluido en el classpath  
- Conexión a base de datos local: localhost:3306  

----------------------------------------
INSTRUCCIONES PARA EJECUTAR EL PROYECTO
----------------------------------------

1. Abre IntelliJ IDEA o Eclipse  
2. Importa el proyecto como un proyecto Java con estructura de paquetes  
3. Asegúrate de tener el driver JDBC de MySQL agregado al classpath  
4. Configura tu conexión a la base de datos en el código si es necesario (usuario, contraseña, URL JDBC)  
5. Ejecuta `Principal.java` para iniciar el sistema desde la consola  

----------------------------------------
CREACIÓN DE LA BASE DE DATOS
----------------------------------------

Utiliza el siguiente script SQL para crear y poblar la base de datos:

Lo puedes encontrar en el repositorio `Script.sql`
1. Abre MySQL Workbench como administrador
2. Una vez dentro entra en una conexión en el menú de MySQL Connections
3. Seleccione la conexión o crea una conexión si no esta creada
4. Una vez dentro la conexión entra en "File" y selecciona "Run SQL Script"
5. Seleccione `Script.sql` y presiona "Run" o ejecutar
6. Si  los dos últimos pasos no funcionan entra en "File" y selecciona "New Query Tab"
7. Debe ir a la ubicación donde se encuentra guardado `Script.sql`
8. Abre el archivo y se le abrirá el menú de MySQL Workbench con el archivo `Script.sql`
9. Pulse encima del nombre de `Script.sql`, seleccione "Database" y pulse en "Connect to Database"
10. Se le abrirá un menú, pulse "ok" y ya tendrá la base de datos creada con las tablas, columnas y datos de prueba necesarios 
