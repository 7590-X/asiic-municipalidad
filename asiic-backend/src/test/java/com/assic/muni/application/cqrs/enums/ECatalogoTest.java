package com.assic.muni.application.cqrs.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ECatalogoTest {

    @Test
    void getCatalogoItemRealName(){
        ECatalogo catalogo = ECatalogo.C_ESTADO_CIVIL;
        assertEquals("estado-civil", catalogo.getValue());
    }

}