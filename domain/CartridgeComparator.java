package com.nc.marketing.cartridge.domain;

import java.util.Comparator;

public class CartridgeComparator implements Comparator<CartridgeInterface> {

	@Override
	public int compare(CartridgeInterface o1, CartridgeInterface o2) {
		return o1.getName().compareTo(o2.getName());
	}

}
