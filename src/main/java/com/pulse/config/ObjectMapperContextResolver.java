package com.pulse.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * ObjectMapperContextResolver class to configure Jackson's ObjectMapper for JAX-RS.
 */
@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
    
    private final ObjectMapper mapper;
    
    /**
     * Constructor to initialize ObjectMapper with JavaTimeModule and disable timestamps
     */
    public ObjectMapperContextResolver() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    /**
     * Get the configured ObjectMapper. Overrides the method from ContextResolver.
     * @param type the class type
     * @return the ObjectMapper instance
     */
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}