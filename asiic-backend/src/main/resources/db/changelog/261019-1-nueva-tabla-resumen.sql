create table as_resumen (
    re_incidencia int,
    re_version smallint,
    re_body text,
    re_fec_registro timestamp,
    re_usr_registro varchar(36),
    primary key (re_incidencia, re_version),
    foreign key (re_incidencia) references as_incidencias(in_id)
);