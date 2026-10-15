-- Ejecutar con psql conectado inicialmente a postgres o a otra base existente.
-- Crea la base y las tablas faltantes; no elimina datos ni modifica tablas existentes.
\set ON_ERROR_STOP on
\set AUTOCOMMIT on
\encoding UTF8

SELECT 'CREATE DATABASE gestion_citas'
WHERE NOT EXISTS (
    SELECT 1 FROM pg_database WHERE datname = 'gestion_citas'
)
\gexec

\connect gestion_citas

BEGIN;

CREATE TABLE IF NOT EXISTS public.cliente (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombres VARCHAR(80) NOT NULL,
    apellidos VARCHAR(80) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(120) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_cliente_correo UNIQUE (correo)
);

CREATE TABLE IF NOT EXISTS public.cita_tecnica (
    id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id INT NOT NULL,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    tipo_servicio VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'Pendiente',

    CONSTRAINT fk_cita_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES public.cliente (id)
        ON DELETE RESTRICT,

    CONSTRAINT ck_cita_estado
        CHECK (
            estado IN (
                'Pendiente',
                'En proceso',
                'Finalizada',
                'Cancelada'
            )
        )
);

COMMIT;
