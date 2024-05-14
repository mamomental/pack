package com.nc.marketing.cartridge.domain;

import java.io.Serializable;

import com.nc.marketing.cartridge.magazine.AbstractCartridge;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class CartridgeResponse implements Serializable {
	private static final long serialVersionUID = -4595026052589286749L;

	@Builder.Default
	private String name = "";
	@Builder.Default
	private String responseCode = AbstractCartridge.SUCCESS;
	@Builder.Default
	private Object responseObject = null;
}
