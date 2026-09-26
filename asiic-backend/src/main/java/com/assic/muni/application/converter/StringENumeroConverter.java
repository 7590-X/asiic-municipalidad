package com.assic.muni.application.converter;

import com.assic.muni.application.enums.ENumero;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringENumeroConverter implements Converter<String, ENumero> {

    @Override
    public ENumero convert(String source) {
        return ENumero.fromValue(source);
    }
}
