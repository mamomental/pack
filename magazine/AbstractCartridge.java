package com.nc.marketing.cartridge.magazine;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import com.nc.brick.modules.npclient.context.NpClientSessionContext;
import com.nc.brick.modules.npclient.context.NpClientSessionContextHolder;
import com.nc.marketing.brick.support.BrickContextUtil;
import com.nc.marketing.cartridge.domain.CartridgeDetail;
import com.nc.marketing.cartridge.domain.CartridgeInterface;
import com.nc.marketing.cartridge.domain.CartridgeRequest;
import com.nc.marketing.cartridge.domain.CartridgeResponse;
import com.nc.marketing.cartridge.magazine.normal.account.AbstractCartridgeAccount;
import com.nc.marketing.common.support.MksProfileUtil;
import com.nc.marketing.common.support.MksProfileUtil.NCProfile;
import com.nc.marketing.platform.domain.cartridge.MarketingApiCartridgeAddition;
import com.nc.marketing.platform.domain.item.MarketingEntry;
import com.nc.marketing.platform.domain.item.MarketingItemPublic;
import com.nc.marketing.platform.support.MarketingItemUtil;
import com.nc.marketing.service.domain.common.MarketingBackend;
import com.nc.marketing.service.service.common.MarketingBackendService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCartridge implements CartridgeInterface {
	public static final String SUCCESS = "mks.msg.cartridge-common.success";
	public static final String INVALID_MARKETINGKEY = "mks.msg.cartridge-common.invalid-marketingkey";
	public static final String INVALID_PARAM = "mks.msg.cartridge-common.invalid-param";
	public static final String NOT_EXIST_API = "mks.msg.cartridge-common.not-exist-api";
	public static final String UNKNOWN_ERROR = "mks.msg.cartridge-common.unknown-error";
	public static final String PASSED_MARKETINGKEY = "marketing-key";
	public static final String ADDITION_MARKETINGKEY = "marketingKey";
	private static final String[] MARKETING_ITEM_ADDITION_JSON_DEFAULT_KEYS = { "pc", "code" };
	// COMMON RESPONSE CODE >= 0
	protected CartridgeDetail[] COMMON_RESPONSE_CODES = {
			CartridgeDetail.builder().key(SUCCESS).description("성공").build(),
			CartridgeDetail.builder().key(INVALID_MARKETINGKEY).description("marketingkey 를 설정하지 않았습니다.").build(),
			CartridgeDetail.builder().key(INVALID_PARAM).description("필수 파라미터가 전달되지 않았습니다.").build(),
			CartridgeDetail.builder().key(NOT_EXIST_API).description("준비된 API가 없습니다.").build(),
			CartridgeDetail.builder().key(UNKNOWN_ERROR).description("unknown error").build() };

	@Autowired
	private MarketingBackendService marketingBackendService;
	@Autowired
	private MksProfileUtil mksProfileUtil;

	@Override
	public CartridgeResponse run(CartridgeRequest cartridgeRequest) {
		if (this.useMarketingKey() && null != cartridgeRequest.getMarketingItem()) {
			if (!cartridgeRequest.getPassedData().containsKey(PASSED_MARKETINGKEY)) {
				String marketingKey = getMarketingKey(cartridgeRequest);
				if (null == marketingKey || marketingKey.isEmpty()) {
					return createErrorResponse(INVALID_MARKETINGKEY);
				}
				else {
					cartridgeRequest.getPassedData().put(PASSED_MARKETINGKEY, marketingKey);
				}
			}
		}

		CartridgeResponse packResponse = null;
		String invalidParam = checkInvalidParameter(cartridgeRequest);

		if (null == invalidParam) {
			packResponse = runInternal(cartridgeRequest);
		}
		else {
			packResponse = createErrorResponse(INVALID_PARAM, String.format("Invalid Parameter [%s]", invalidParam));
		}
		return packResponse;
	}

	protected ZonedDateTime now(CartridgeRequest cartridgeRequest) {
		if (this.mksProfileUtil.existProfile(NCProfile.LIVE)) {
			return ZonedDateTime.now();
		}

		MarketingBackend marketingBackEnd = this.marketingBackendService.getByItemIdx(cartridgeRequest.getItemIdx());
		if (null != marketingBackEnd && null != marketingBackEnd.getQaDate()) {
			return marketingBackEnd.getQaDate();
		}
		return ZonedDateTime.now();
	}

	protected MarketingApiCartridgeAddition getAdditionInfo(CartridgeRequest cartridgeRequest, String additionKey) {
		return cartridgeRequest.getMarketingApi().getMarketingApiCartridgeAdditionSet().stream()
				.filter((x) -> x.getAdditionKey().equalsIgnoreCase(additionKey)).findAny().orElse(null);
	}

	protected String findOriginModule(CartridgeRequest cartridgeRequest) {
		MarketingItemPublic marketingItem = cartridgeRequest.getMarketingItem();
		Optional<MarketingEntry> entry = marketingItem.getMarketingEntrySet().stream()
				.filter((me) -> null != BrickContextUtil.findModule(me.getEntryUrl())).findAny();
		return BrickContextUtil.findModule(entry.get().getEntryUrl());
	}

	protected CartridgeResponse createErrorResponse(String responseCode) {
		Set<CartridgeDetail> set = getResponseCode();

		CartridgeDetail cartridgeResponseCode = set.stream().filter((x) -> x.getKey().equalsIgnoreCase(responseCode))
				.findFirst().orElse(null);

		if (null == cartridgeResponseCode) {
			return createErrorResponse(UNKNOWN_ERROR);
		}

		return createErrorResponse(cartridgeResponseCode.getKey(), cartridgeResponseCode.getDescription());
	}

	protected CartridgeResponse createErrorResponse(String responseCode, String message) {
		return CartridgeResponse.builder().name(this.getName()).responseCode(responseCode).responseObject(message)
				.build();
	}

	private String checkInvalidParameter(CartridgeRequest cartridgeRequest) {
		Set<CartridgeDetail> parameters = getParameter();

		Iterator<CartridgeDetail> iter = parameters.iterator();
		while (iter.hasNext()) {
			CartridgeDetail cartridgeDetail = iter.next();
			if (!cartridgeRequest.getParam().containsKey(cartridgeDetail.getKey())) {
				return cartridgeDetail.getKey();
			}
		}

		return null;
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public CartridgeStatus getStatus() {
		return CartridgeStatus.DEVELOP;
	}

	@Override
	public CartridgeType getType() {
		return CartridgeType.NORMAL_CARTRIDGE;
	}

	@Override
	public int getRunOrder() {
		return CartridgeOrder.NORMAL_ORDER_0.ordinal();
	}

	@Override
	public Set<CartridgeDetail> getParameter() {
		Set<CartridgeDetail> parameters = new HashSet<CartridgeDetail>();

		for (CartridgeDetail param : getParameterInfo()) {
			parameters.add(param);
		}
		return parameters;
	}

	@Override
	public Set<CartridgeDetail> getAdditionKey() {
		Set<CartridgeDetail> additionKeys = new HashSet<CartridgeDetail>();

		for (CartridgeDetail key : getAdditionKeyInfo()) {
			additionKeys.add(key);
		}
		return additionKeys;
	}

	@Override
	public Set<CartridgeDetail> getResponseCode() {
		Set<CartridgeDetail> result = new HashSet<CartridgeDetail>();

		for (CartridgeDetail code : this.COMMON_RESPONSE_CODES) {
			result.add(code);
		}

		for (CartridgeDetail code : getResponseCodeInfo()) {
			result.add(code);
		}
		return result;
	}
	
	@Override
	public Set<CartridgeDetail> getResponseValue() {
		Set<CartridgeDetail> result = new HashSet<CartridgeDetail>();

		for (CartridgeDetail code : getResponseValueInfo()) {
			result.add(code);
		}
		return result;
	}

	protected CartridgeDetail[] getParameterInfo() {
		return new CartridgeDetail[] {};
	}

	protected CartridgeDetail[] getAdditionKeyInfo() {
		return new CartridgeDetail[] {};
	}

	protected CartridgeDetail[] getResponseCodeInfo() {
		return new CartridgeDetail[] {};
	}
	
	protected CartridgeDetail[] getResponseValueInfo() {
		return new CartridgeDetail[] {};
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public boolean useMarketingKey() {
		return true;
	}

	public String getAccountValue(CartridgeRequest cartridgeRequest) {
		String accountValue = cartridgeRequest.getPassedData().get(AbstractCartridgeAccount.PASSED_DATA_KEY);
		// account 값을 지정하는 카트리지를 깜빡했다면 기본으로 plaync account 사용
		if (null == accountValue) {
			NpClientSessionContext npClientSessionContext = NpClientSessionContextHolder.getContext();
			accountValue = npClientSessionContext.getCheckedSession().getUserId();
			cartridgeRequest.getPassedData().put(AbstractCartridgeAccount.PASSED_DATA_KEY, accountValue);
		}
		return accountValue;
	}

	public String getMarketingKey(CartridgeRequest cartridgeRequest) {
		if (!cartridgeRequest.getPassedData().containsKey(PASSED_MARKETINGKEY)) {
			// 개발 패키지 상단에 addition 에 저장한 경우
			String mk = MarketingItemUtil.findAdditionData(cartridgeRequest.getMarketingItem(), ADDITION_MARKETINGKEY);

			if (null != mk && !mk.isEmpty()) {
				return mk;
			}

			// json 영역에 저장한 경우
			String[] keys = { "cartridge", PASSED_MARKETINGKEY };
			String jsonString = MarketingItemUtil.findAdditionData(cartridgeRequest.getMarketingItem(), "json");
			JSONObject marketingKey = parseMarketingItemAdditionJsonPcCode(jsonString, JSONObject.class,
					MARKETING_ITEM_ADDITION_JSON_DEFAULT_KEYS);
			if (null == marketingKey) {
				return null;
			}
			return parseMarketingItemAdditionJsonPcCode(marketingKey.toJSONString(), String.class, keys);
		}
		return cartridgeRequest.getPassedData().get(PASSED_MARKETINGKEY);
	}

	private <T> T parseMarketingItemAdditionJsonPcCode(String jsonString, Class<T> returnType, String... keys) {
		JSONParser jsonParser = new JSONParser();
		JSONObject codeJsonObj = null;
		try {
			codeJsonObj = (JSONObject) jsonParser.parse(jsonString);

			for (String key : keys) {
				if (codeJsonObj.containsKey(key)) {
					Object obj = codeJsonObj.get(key);
					if (obj instanceof JSONObject) {
						codeJsonObj = (JSONObject) obj;
					}
					else {
						if (returnType == JSONObject.class) {
							codeJsonObj = (JSONObject) jsonParser.parse(obj.toString());
						}
						else {
							return returnType.cast(obj);
						}
					}
				}
				else {
					return null;
				}
			}
		}
		catch (ParseException ex) {
			ex.printStackTrace();
		}
		return returnType.cast(codeJsonObj);
	}

	protected abstract CartridgeResponse runInternal(CartridgeRequest cartridgeRequest);
}
