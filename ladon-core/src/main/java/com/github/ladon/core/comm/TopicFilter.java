package com.github.ladon.core.comm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TopicFilter {

	/** ObjectMapper */
	@JsonIgnore
	private ObjectMapper objMapper = new ObjectMapper();

	@JsonProperty("topicName")
	private String topicName;

	@JsonProperty("offset")
	private int offset = -1;

	public static TopicFilter valueOf( String filter ) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		return objMapper.readValue( filter, TopicFilter.class );
	}

	public String string() throws Exception {
		return objMapper.writeValueAsString( this );
	}

}
