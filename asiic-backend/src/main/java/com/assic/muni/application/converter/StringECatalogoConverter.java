package com.assic.muni.application.converter;

import com.assic.muni.application.cqrs.enums.ECatalogo;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringECatalogoConverter implements Converter<String, ECatalogo> {

    @Override
    public ECatalogo convert(@NonNull String source) {
        return ECatalogo.fromValue(source);
    }
}
