package com.nc.marketing.cartridge.magazine;

import com.nc.marketing.cartridge.domain.CartridgeDetail;
import com.nc.marketing.cartridge.domain.CartridgeRequest;
import com.nc.marketing.cartridge.domain.CartridgeResponse;

import org.springframework.stereotype.Component;

@Component
public class CartridgeSample extends AbstractCartridge {
	private CartridgeDetail[] PARAMS = { CartridgeDetail.builder().key("param0").description("파라미터0").build(),
			CartridgeDetail.builder().key("param1").description("파라미터1").build() };

	private CartridgeDetail[] ADDITION_KEYS = { CartridgeDetail.builder().key("config0").description("설정0").build(),
			CartridgeDetail.builder().key("config1").description("설정1").build() };

	private CartridgeDetail[] RESPONSE_CODES = {
			CartridgeDetail.builder().key("mks.msg.cartridge-common.test-result").description("test result").build() };

	@Override
	public CartridgeResponse runInternal(CartridgeRequest cartridgeRequest) {
		return CartridgeResponse.builder().name(this.getName()).responseCode(AbstractCartridge.SUCCESS)
				.responseObject(CartridgeDetail.builder().key("TEST RESPONSE").description("test response").build())
				.build();
	}

	@Override
	public String getTitle() {
		return "샘플 카트리지";
	}

	@Override
	public String getDescription() {
		return "카트리지 기능 테스트를 위한 샘플(실제 api로 사용해도 아무 동작 하지 않음)";
	}

	protected CartridgeDetail[] getParameterInfo() {
		return this.PARAMS;
	}

	protected CartridgeDetail[] getAdditionKeyInfo() {
		return this.ADDITION_KEYS;
	}

	protected CartridgeDetail[] getResponseCodeInfo() {
		return this.RESPONSE_CODES;
	}

	@Override
	public CartridgeStatus getStatus() {
		return CartridgeStatus.RELEASE;
	}
	
	@Override
	public String getCategory() {
		return "etc";
	}
}
