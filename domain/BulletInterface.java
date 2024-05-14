package com.nc.marketing.cartridge.domain;

import com.nc.marketing.cartridge.magazine.AbstractCartridgeBullet;

public interface BulletInterface <T extends AbstractCartridgeBullet> {
	BulletResponse run(T cartridge, CartridgeRequest packRequest);
}
