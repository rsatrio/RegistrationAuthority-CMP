package com.rizky.ra.cmp.serializer;

import java.io.IOException;
import java.sql.Blob;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


public class SerializeBlobString extends JsonSerializer<Blob> {

    Logger log1=LoggerFactory.getLogger(SerializeBlobString.class);
    @Override
    public void serialize(Blob data, JsonGenerator result, SerializerProvider provider)
            throws IOException {
        try {
            int bLength = (int) data.length();  
            byte[] blob1 = data.getBytes(1, bLength);

            result.writeString(new String(blob1));
            
        }
        catch(Exception e)  {
            log1.error("Failed to serialize blob",e);
            throw new RuntimeException("Failed to serialize");
        }

    }
}
