package com.nc.marketing.cartridge.magazine;

import java.util.HashMap;
import java.util.Map;

import com.nc.marketing.cartridge.domain.BulletInterface;
import com.nc.marketing.cartridge.domain.BulletResponse;
import com.nc.marketing.cartridge.domain.CartridgeRequest;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class BulletRunner {
    @Autowired
    private ApplicationContext applicationContext;
    
    private Map<String, BulletInterface> bulletAllMap = null;

    public BulletResponse run(AbstractCartridgeBullet cartridge, CartridgeRequest cartridgeRequest, String bulletName) {
        BulletInterface bullet = this.bulletAllMap.get(bulletName.toLowerCase());
        if (null != bullet) {
            return bullet.run(cartridge, cartridgeRequest);
        }

        return null;
    }

    @PostConstruct
    private void initialize() {
        final Map<String, BulletInterface> bulletBeans = this.applicationContext.getBeansOfType(BulletInterface.class);
        
        this.bulletAllMap = new HashMap<String, BulletInterface>();
        for (String key : bulletBeans.keySet()) {
            BulletInterface bullet = bulletBeans.get(key);
            this.bulletAllMap.put(key.toLowerCase(), bullet);
        }
    }
}
