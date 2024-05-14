package com.nc.marketing.cartridge.magazine;

import com.nc.marketing.cartridge.domain.BulletResponse;
import com.nc.marketing.cartridge.domain.CartridgeDetail;
import com.nc.marketing.cartridge.domain.CartridgeRequest;
import com.nc.marketing.cartridge.domain.CartridgeResponse;
import com.nc.marketing.cartridge.domain.CartridgeResponse.CartridgeResponseBuilder;
import com.nc.marketing.platform.domain.cartridge.MarketingApiCartridgeAddition;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCartridgeBullet extends AbstractCartridge {
	private static final String NOT_EXIST_CONFIG = "not-exist-config";
	private static final String NOT_WORK_BULLET = "not-work-bullet";
	public static final String BULLET_ERROR_01 = "bullet-error-01";
	public static final String BULLET_ERROR_02 = "bullet-error-02";
	public static final String BULLET_ERROR_03 = "bullet-error-03";
	public static final String BULLET_ERROR_04 = "bullet-error-04";
	public static final String BULLET_ERROR_05 = "bullet-error-05";

	protected CartridgeDetail[] ADDITION_KEYS = {
			CartridgeDetail.builder().key("bullet").description("상세 기능 bullet 지정").build() };

	public CartridgeDetail[] PARAMS = {
			CartridgeDetail.builder().key("p1").description("bullet에서 알아서 사용할 파라미터1, 사용안할 때는 아무 값이라도 넣기").build(),
			CartridgeDetail.builder().key("p2").description("bullet에서 알아서 사용할 파라미터2, 사용안할 때는 아무 값이라도 넣기").build(),
			CartridgeDetail.builder().key("p3").description("bullet에서 알아서 사용할 파라미터3, 사용안할 때는 아무 값이라도 넣기").build()};

	protected CartridgeDetail[] RESPONSE_CODES = {
			CartridgeDetail.builder().key(getErrorPrefix() + NOT_EXIST_CONFIG).description("bullet is not exist").build(),
			CartridgeDetail.builder().key(getErrorPrefix() + NOT_WORK_BULLET).description("bullet is not work").build(),
			CartridgeDetail.builder().key(getErrorPrefix() + BULLET_ERROR_01).description("bullet error 01").build(),
			CartridgeDetail.builder().key(getErrorPrefix() + BULLET_ERROR_02).description("bullet error 02").build(),
			CartridgeDetail.builder().key(getErrorPrefix() + BULLET_ERROR_03).description("bullet error 03").build(),
			CartridgeDetail.builder().key(getErrorPrefix() + BULLET_ERROR_04).description("bullet error 04").build(),
			CartridgeDetail.builder().key(getErrorPrefix() + BULLET_ERROR_05).description("bullet error 05").build()
	};

	@Autowired
	private BulletRunner bulletRunner;

	@Override
	public CartridgeResponse runInternal(CartridgeRequest cartridgeRequest) {
		int additionKeyIdx = 0;
		MarketingApiCartridgeAddition bulletInfo = getAdditionInfo(cartridgeRequest,
				this.ADDITION_KEYS[additionKeyIdx++].getKey());

		if (null == bulletInfo) {
			return createErrorResponse(NOT_EXIST_CONFIG);
		}

		BulletResponse bulletResponse = this.bulletRunner.run(this, cartridgeRequest, bulletInfo.getAdditionValue());
		CartridgeResponseBuilder builder = CartridgeResponse.builder();
		builder.name(this.getName());
		builder.responseCode(bulletResponse.getResponseCode());

		if (bulletResponse.getResponseCode().equals(AbstractCartridge.SUCCESS)) {
			builder.responseObject(bulletResponse.getResponseObjectMap());
		}

		return builder.build();
	}

	protected abstract String getErrorPrefix();

	protected CartridgeDetail[] getAdditionKeyInfo() {
		return this.ADDITION_KEYS;
	}

	protected CartridgeDetail[] getParameterInfo() {
		return this.PARAMS;
	}

	protected CartridgeDetail[] getResponseCodeInfo() {
		return this.RESPONSE_CODES;
	}

	@Override
	public boolean useMarketingKey() {
		return true;
	}
}
