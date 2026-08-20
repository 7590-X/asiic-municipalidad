#!/bin/bash
set -e

# ==============================================================================
# Script de inicialización para PostgreSQL
# Crea los usuarios y las bases de datos necesarias:
#  - db_asiic (Base de datos principal del sistema ASIIC)
#  - db_keycloak (Base de datos dedicada para Keycloak con su propio usuario)
#  - db_postgres (Base de datos adicional)
# ==============================================================================

echo "======================================================"
echo "Iniciando script de inicialización PostgreSQL..."
echo "======================================================"

MAIN_USER="${POSTGRES_USER}"
MAIN_DB="${POSTGRES_DB}"

KC_USER="${KC_USERNAME:-usr_keycloak}"
KC_DB="${KC_DB:-db_keycloak}"
KC_PASSWORD="${KC_PASSWORD}"

ASIIC_USER="${ASIIC_USER:-usr_asiic}"
ASIIC_DB="${ASIIC_DB:-db_asiic}"
ASIIC_DB_DEV="db_asiic_dev"
ASIIC_PASSWORD="${ASIIC_PASSWORD}"

# Función auxiliar para crear base de datos si no existe
create_database_if_not_exists() {
  local db_name="$1"
  local db_owner="$2"
  echo "Verificando/Creando base de datos '$db_name' (propietario: $db_owner)..."
  psql -v ON_ERROR_STOP=1 --username "$MAIN_USER" --dbname "$MAIN_DB" <<-EOSQL
        SELECT 'CREATE DATABASE "$db_name" WITH OWNER "$db_owner"'
        WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$db_name')\gexec
EOSQL
}

create_user_if_not_exists() {
  local usr_name="$1"
  local usr_password="$2"

  echo "Verificando/Creando usuario '$usr_name'..."
  psql -v ON_ERROR_STOP=1 --username "$MAIN_USER" --dbname "$MAIN_DB" <<-EOSQL
      DO \$\$
      BEGIN
          IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$usr_name') THEN
              CREATE USER "$usr_name" WITH ENCRYPTED PASSWORD '$usr_password';
          ELSE
              ALTER USER "$usr_name" WITH ENCRYPTED PASSWORD '$usr_password';
          END IF;
      END
      \$\$;
EOSQL
}

set_grant_to_user() {
  local user="$1"
  local database="$2"
  echo "Otorgando permisos en '$database' a '$user'..."
  psql -v ON_ERROR_STOP=1 --username "$MAIN_USER" --dbname "$database" <<-EOSQL
    GRANT ALL ON SCHEMA public TO "$user";
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "$user";
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO "$user";
    GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO "$user";

    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO "$user";
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO "$user";
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO "$user";
    ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TYPES TO "$user";
EOSQL
}

echo "[Info] Creating users"
create_user_if_not_exists "$KC_USER" "$KC_PASSWORD"
create_user_if_not_exists "$ASIIC_USER" "$ASIIC_PASSWORD"

echo "[Info] Creating databases"
create_database_if_not_exists "$KC_DB" "$KC_USER"
create_database_if_not_exists "$ASIIC_DB" "$ASIIC_USER"
create_database_if_not_exists "$ASIIC_DB_DEV" "$ASIIC_USER"

echo "[Info] Set grants to users over databases"
psql -v ON_ERROR_STOP=1 --username "$MAIN_USER" --dbname "$MAIN_DB" <<-EOSQL
    GRANT ALL PRIVILEGES ON DATABASE "$KC_DB" TO "$KC_USER";
    GRANT ALL PRIVILEGES ON DATABASE "$ASIIC_DB" TO "$ASIIC_USER";
    GRANT ALL PRIVILEGES ON DATABASE "$ASIIC_DB_DEV" TO "$ASIIC_USER"
EOSQL

echo "[Info] Setting default grants"
set_grant_to_user "$KC_USER" "$KC_DB"
set_grant_to_user "$ASIIC_USER" "$ASIIC_DB"
set_grant_to_user "$ASIIC_USER" "$ASIIC_DB_DEV"

echo "======================================================"
echo "Inicialización de bases de datos completada exitosamente!"
echo "Bases de datos configuradas:"
echo " - $MAIN_DB (Owner: $MAIN_USER)"
echo " - $KC_DB (Owner: $KC_USER)"
echo " - $ASIIC_DB (Owner: $ASIIC_USER)"
echo "======================================================"
