package com.nc.marketing.cartridge.domain;

import java.util.Arrays;
import java.util.Set;

public interface CartridgeInterface {
	CartridgeResponse run(CartridgeRequest packRequest);

	// 카트리지 이름은 BackPacker에서 넣어줌
	String getName();

	CartridgeType getType();

	CartridgeStatus getStatus();

	int getRunOrder();

	// 외부에서 입력받아야 하는 필수 파라미터가 있을 경우 파라미터 키를 정의(ex:리니지 본인인증 시 리턴 url)
	Set<CartridgeDetail> getParameter();

	// 카트리지별 내부 설정값이 필요할 때 키 정의(ex:쿠폰 카트리지에 쿠폰 번호 설정)
	Set<CartridgeDetail> getAdditionKey();

	// 카트리지별 실행 결과 코드 정보
	Set<CartridgeDetail> getResponseCode();
	
	// 카트리지별 실행 결과 정보
	Set<CartridgeDetail> getResponseValue();

	// MARK 에서 API 순서도 편집시 사용
	String getTitle();

	// 카트리지에 대한 상세한 설명이 필요할 경우 사용
	String getDescription();
	
	boolean useMarketingKey();
	
	// 성격이 유사한 것들끼리 묶기
	String getCategory();

	enum CartridgeType {
		PRE_CARTRIDGE, // 관리툴에 노출되지 않는 선처리 기능
		NORMAL_CARTRIDGE; // 관리툴에서 순서 관리

		public static CartridgeType getCartridgeType(String name) {
			for (CartridgeType ct : CartridgeType.values()) {
				System.out.println(ct.name());
			}

			return Arrays.stream(CartridgeType.values()).filter((p) -> p.name().equalsIgnoreCase(name)).findFirst()
					.get();
		}
	}

	enum CartridgeStatus {
		DEVELOP,	// 신규 개발된 cartridge, 특정 프로모션에 특화된 cartridge
		RELEASE,	// 추후 다른 프로모션에 재활용 가능한 것으로 판단한 cartridge
		DEPRECATED	// 사용중지된 cartridge
		;
	}

	enum CartridgeOrder {
		PRE_ORDER_1,
		PRE_ORDER_2,
		PRE_ORDER_3,
		PRE_ORDER_4,
		PRE_ORDER_5,
		PRE_ORDER_6,
		PRE_ORDER_7,
		PRE_ORDER_8,
		PRE_ORDER_9,
		NORMAL_ORDER_0;
	}

	String SPLITTER = ",";
}
