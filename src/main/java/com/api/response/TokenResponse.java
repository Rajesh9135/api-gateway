package com.api.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@SuppressWarnings("deprecation")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Data
public class TokenResponse {

	private StatusDescription statusDescription;
//	private UserMaster userDetail;
//	private AdminMaster adminMaster;
}
