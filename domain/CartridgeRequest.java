package com.nc.marketing.cartridge.domain;

import java.util.Map;

import com.nc.marketing.platform.domain.cartridge.MarketingApi;
import com.nc.marketing.platform.domain.item.MarketingItemPublic;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CartridgeRequest {
	private HttpServletRequest request;
	private long apiIdx;
	private long itemIdx;
	private MarketingItemPublic marketingItem;
	private String apiName;
	private Map<String, String> param;
	private MarketingApi marketingApi;
	private Map<String, String> passedData;
}
