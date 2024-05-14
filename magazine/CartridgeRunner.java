package com.nc.marketing.cartridge.magazine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.nc.marketing.cartridge.domain.ApiResponse;
import com.nc.marketing.cartridge.domain.CartridgeComparator;
import com.nc.marketing.cartridge.domain.CartridgeInterface;
import com.nc.marketing.cartridge.domain.CartridgeInterface.CartridgeType;
import com.nc.marketing.cartridge.domain.CartridgeRequest;
import com.nc.marketing.cartridge.domain.CartridgeResponse;
import com.nc.marketing.common.domain.RestNaviImpl;
import com.nc.marketing.platform.domain.cartridge.MarketingApi;
import com.nc.marketing.platform.service.cartridge.MarketingApiService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CartridgeRunner {
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private MarketingApiService marketingApiService;

	private List<String> cartridgePreKeyList = null;
	private Map<String, CartridgeInterface> cartridgeAllMap = null;

	public ApiResponse run(CartridgeRequest cartridgeRequest) {
		ApiResponse result = ApiResponse.of();

		List<String> keys = new ArrayList<String>();
		MarketingApi marketingApi = this.marketingApiService.getByIdx(cartridgeRequest.getApiIdx());
		if (null == marketingApi) {
			// CartridgeResponse.builder().name("NOAPI").responseCode("NOT_EXIST_API").build();
			result.setName("CartridgeRunner");
			result.setResponseCode(AbstractCartridge.NOT_EXIST_API);
			return result;
		}
		keys.addAll(this.cartridgePreKeyList);
		cartridgeRequest.setItemIdx(marketingApi.getItemIdx());
		cartridgeRequest.setMarketingApi(marketingApi);
		if (null != marketingApi) {
			for (String cartridge : marketingApi.getCartridgePack().split(CartridgeInterface.SPLITTER)) {
				keys.add(cartridge);
			}
		}

		for (String key : keys) {
			CartridgeInterface cartridge = this.cartridgeAllMap.get(key.toLowerCase());
			if (null != cartridge) {
				CartridgeResponse cartridgeResponse = cartridge.run(cartridgeRequest);
				result.setName(cartridge.getName());
				result.setResponseCode(cartridgeResponse.getResponseCode());
				if (null != cartridgeResponse.getResponseObject()) {
					result.getResponseObjectMap().put(cartridge.getName(), cartridgeResponse.getResponseObject());
				}

				if (!cartridgeResponse.getResponseCode().equalsIgnoreCase(AbstractCartridge.SUCCESS)) {
					// 카트리지 결과가 SUCCESS가 아니면 이후 과정을 중지한다.
					break;
				}
			}
			else {
				log.info("pack : [{}] is not exist", key);
			}
		}

		return result;
	}

	public CartridgeResponse tester(String cartridgeName, CartridgeRequest cartridgeRequest) {
		CartridgeResponse cartridgeResponse = null;
		if (this.cartridgeAllMap.containsKey(cartridgeName.toLowerCase())) {
			CartridgeInterface cartridge = this.cartridgeAllMap.get(cartridgeName.toLowerCase());
			cartridgeResponse = cartridge.run(cartridgeRequest);
			log.info("CartridgeResponse : {}", cartridgeResponse);
		}
		else {
			log.info("not exist cartridge : {}", cartridgeName);
		}
		return cartridgeResponse;
	}

	public RestNaviImpl<CartridgeInterface> getCartridgeByType(CartridgeType cartridgeType, String[] cartridgeArray,
			String title, int page, int pageSize) {

		// cartridgeType
		List<CartridgeInterface> list = this.cartridgeAllMap.values().stream()
				.filter((p) -> p.getType() == cartridgeType).collect(Collectors.toList());

		List<CartridgeInterface> nameList = null;

		if (null != cartridgeArray && 0 < cartridgeArray.length) {
			nameList = new ArrayList<CartridgeInterface>();
			for (String name : cartridgeArray) {
				CartridgeInterface c = list.stream().filter((p) -> p.getName().equalsIgnoreCase(name)).findFirst()
						.orElse(null);
				if (null != c) {
					nameList.add(c);
				}
			}
		}
		if (null != nameList) {
			list = nameList;
		}

		// title
		if (!title.isEmpty()) {
			list = list.stream().filter((p) -> p.getTitle().contains(title)).collect(Collectors.toList());
		}

		Collections.sort(list, new CartridgeComparator());

		Pageable pageable = PageRequest.of(page - 1, pageSize);
		int listSize = list.size();
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), listSize);

		list = list.subList(start, end);

		return new RestNaviImpl<>(list, page, pageSize, listSize);
	}

	@PostConstruct
	private void initialize() {
		final Map<String, CartridgeInterface> packBeans = this.applicationContext
				.getBeansOfType(CartridgeInterface.class);
		this.cartridgePreKeyList = new ArrayList<String>();
		Map<String, Integer> cartridgePreKeyMap = new HashMap<String, Integer>();
		this.cartridgeAllMap = new HashMap<String, CartridgeInterface>();
		for (String key : packBeans.keySet()) {
			CartridgeInterface pack = packBeans.get(key);
			this.cartridgeAllMap.put(key.toLowerCase(), pack);
			if (CartridgeType.PRE_CARTRIDGE == pack.getType()) {
				cartridgePreKeyMap.put(key.toLowerCase(), pack.getRunOrder());
			}
		}

		List<Map.Entry<String, Integer>> entryList = new LinkedList<>(cartridgePreKeyMap.entrySet());

		// pre cartridge의 순서 제어는 order 값으로 진행한다.
		entryList.sort(((o1, o2) -> cartridgePreKeyMap.get(o1.getKey()) - cartridgePreKeyMap.get(o2.getKey())));
		entryList.forEach((i) -> this.cartridgePreKeyList.add(i.getKey()));
	}
}
