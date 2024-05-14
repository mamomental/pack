package com.nc.marketing.cartridge.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.nc.marketing.cartridge.magazine.AbstractCartridge;
import lombok.Data;

@Data
public class ApiResponse implements Serializable {
	private static final long serialVersionUID = -6008555428284170332L;

	private String name = "";
	private String responseCode = AbstractCartridge.SUCCESS;
	private Map<String, Object> responseObjectMap = null;

	public static ApiResponse of() {
		ApiResponse result = new ApiResponse();
		result.setResponseObjectMap(new HashMap<String, Object>());
		return result;
	}
}
