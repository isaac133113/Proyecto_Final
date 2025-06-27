CREATE DATABASE IF NOT EXISTS reservas_salas;
USE reservas_salas;
DROP TABLE IF EXISTS reservas;
DROP TABLE IF EXISTS salas;
DROP TABLE IF EXISTS empleados;
CREATE TABLE empleados (
    id INT  auto_increment PRIMARY KEY,
    nombre VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    departamento VARCHAR(100)
);

CREATE TABLE salas (
    id INT NOT NULL auto_increment PRIMARY KEY,
    nombre VARCHAR(100),
    capacidad INT,
    recursos TEXT
);
CREATE TABLE reservas (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    empleado_id INT,
    sala_id INT,
    fecha DATE,
    hora_inicio TIME,
    hora_fin TIME,
    FOREIGN KEY (empleado_id) REFERENCES empleados(id) ON DELETE CASCADE,
    FOREIGN KEY (sala_id) REFERENCES salas(id)ON DELETE CASCADE
);

INSERT INTO empleados (id, nombre, email, departamento) VALUES
(1, 'Ana López', 'ana.lopez@empresa.com', 'Recursos Humanos'),
(2, 'Carlos Pérez', 'carlos.perez@empresa.com', 'TI'),
(3, 'Lucía Gómez', 'lucia.gomez@empresa.com', 'Finanzas'),
(4, 'Miguel Torres', 'miguel.torres@empresa.com', 'Marketing'),
(5, 'Sofía Ramírez', 'sofia.ramirez@empresa.com', 'Legal');

INSERT INTO salas (id, nombre, capacidad, recursos) VALUES
(1, 'Sala A', 10, 'Proyector, Pizarra'),
(2, 'Sala B', 20, 'Videoconferencia, Pizarra'),
(3, 'Sala C', 15, 'Proyector'),
(4, 'Sala D', 25, 'Audio, Proyector, Pizarra'),
(5, 'Sala E', 8, 'Pizarra');

INSERT INTO reservas (id, empleado_id, sala_id, fecha, hora_inicio, hora_fin) VALUES
(1, 1, 2, '2025-06-26', '10:00:00', '11:00:00'),
(2, 2, 1, '2025-06-27', '09:00:00', '10:30:00'),
(3, 3, 3, '2025-06-28', '14:00:00', '15:00:00'),
(4, 4, 4, '2025-06-29', '13:00:00', '14:30:00'),
(5, 5, 5, '2025-06-30', '11:00:00', '12:00:00');