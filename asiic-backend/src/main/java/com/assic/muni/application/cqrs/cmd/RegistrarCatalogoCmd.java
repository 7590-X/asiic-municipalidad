package com.assic.muni.application.cqrs.cmd;

import lombok.Data;

@Data
public class RegistrarCatalogoCmd {
    private Short id;
    private int idTabla;
    private String valor;
}