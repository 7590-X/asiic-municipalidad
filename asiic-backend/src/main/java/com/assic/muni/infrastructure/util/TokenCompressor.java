package com.assic.muni.infrastructure.util;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Component
public class TokenCompressor {

    public String compress(String rawToken){
        byte[] input = rawToken.getBytes(StandardCharsets.UTF_8);
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(input);
        deflater.finish();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            output.write(buffer, 0, count);
        }
        deflater.end();

        return Base64.getUrlEncoder().withoutPadding().encodeToString(output.toByteArray());
    }

    public String decompress(String compressedToken) throws Exception {
        byte[] input = Base64.getUrlDecoder().decode(compressedToken);
        Inflater inflater = new Inflater();
        inflater.setInput(input);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (!inflater.finished()) {
            int count = inflater.inflate(buffer);
            output.write(buffer, 0, count);
        }
        inflater.end();

        return output.toString(StandardCharsets.UTF_8);
    }
}
