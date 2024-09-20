package com.api.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@SuppressWarnings("deprecation")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Data
public class StatusDescription {

	private Integer statusCode;
	private String statusMessage;
	private String statusdescription;
	public StatusDescription(Integer statusCode, String statusdescription) {
		super();
		this.statusCode = statusCode;
		this.statusdescription = statusdescription;
	}
	public StatusDescription() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}
