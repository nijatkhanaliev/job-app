package com.job.util;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.InputStream;

public class CVParser {
    public static String extractText(InputStream inputStream) throws Exception {
        Tika tika = new Tika();
        try {
            return tika.parseToString(inputStream);
        } catch (TikaException e) {
            throw new RuntimeException("Failed to parse CV", e);
        }
    }
}
