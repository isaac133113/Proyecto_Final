
SISTEMA DE GESTIÓN DE SALAS DE REUNIONES
Este proyecto Java permite gestionar salas de reuniones, empleados y reservas en una empresa. Incluye validación de conflictos de horario para evitar reservas solapadas.

CARACTERÍSTICAS
CRUD completo de salas de reuniones.

Gestión de empleados.

Creación de reservas con validación de conflictos de horario.

Conexión a base de datos MySQL.

Interfaz de consola interactiva.

Organización modular por paquetes Java.

ESTRUCTURA DEL PROYECTO
src/
└── com/
└── empresa/
├── salas/ → Gestión CRUD de salas
│ └── Salas.java
├── empleados/ → Gestión CRUD de empleados
│ └── Empleados.java
├── reservas/ → Gestión de reservas con control de horarios
│ └── Reservas.java
└── Main.java → Punto de entrada, menú principal

REQUISITOS
Java JDK 17 o superior.

MySQL 8 o superior.

IDE recomendado: IntelliJ IDEA o Eclipse.

Driver JDBC de MySQL añadido al classpath.

Base de datos local configurada en localhost:3306.

