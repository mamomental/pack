package com.nc.marketing.cartridge.domain;

import java.io.Serializable;
import java.util.Map;

import com.nc.marketing.cartridge.magazine.AbstractCartridge;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class BulletResponse implements Serializable {
    private static final long serialVersionUID = -7505652479610901778L;
    
    @Builder.Default
    private String responseCode = AbstractCartridge.SUCCESS;

    @Builder.Default
    private Map<String, Object> responseObjectMap = null;
}
