-- liquibase formatted sql

-- changeset rmachicm:agregar-relacion-con-persona-con-muni
alter table as_munis add column mu_persona int not null;
alter table as_munis add foreign key (mu_persona) references as_personas(pe_id);
alter table as_munis add unique (mu_persona);
call sp_agregar_catalogo(3,'Jurídica','PEJU');
alter table as_personas alter column pe_estado_civil drop not null;
alter table as_personas alter column pe_genero drop not null;
alter table as_personas alter column pe_nombre type varchar(100);
alter table as_personas alter column pe_apellido drop not null;
