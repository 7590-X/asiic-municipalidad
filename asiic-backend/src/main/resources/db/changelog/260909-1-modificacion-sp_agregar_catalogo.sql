-- liquibase formatted sql

-- changeset rmachicm:alterar-sp_agregar_catalogo splitStatements:false
create or replace procedure sp_agregar_catalogo(
    i_tabla numeric(2),
    i_valor varchar(100),
    i_seudo varchar(6) = null
)
    language plpgsql
as
$$
declare
w_nuevo_id smallint;
begin
    if exists(select 1 from as_tablas where ta_id = i_tabla) then
        select max(ca_id + 1) into w_nuevo_id from as_catalogos;
        insert into as_catalogos(ca_id, ca_tabla, ca_valor,ca_seudo)
        values (w_nuevo_id, i_tabla, i_valor,i_seudo);
    else
        raise exception 'La tabla % no existe', i_tabla;
    end if;
end;
$$;