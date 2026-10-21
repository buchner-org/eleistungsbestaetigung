package de.yoshteq.dcs;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.quarkus.runtime.Startup;

@Startup
@ApplicationScoped
public class ObjectMapperHolder
{
	
	private ObjectMapper objectMapper;
	
	@PostConstruct
	void init() {
		objectMapper = JsonMapper.builder().findAndAddModules().build();	
	}
	
	public ObjectMapper getObjectMapper()
	{
		return objectMapper;
	}

}
