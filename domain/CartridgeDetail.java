package com.nc.marketing.cartridge.domain;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CartridgeDetail {
	private String key;
	private String description;
}
