package com.nc.marketing.cartridge.aop;

import com.nc.marketing.common.support.ZonedDateTimeBuilder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.time.ZonedDateTime;
import java.util.Optional;

public class ExpiredBulletChecker implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        try {
            return Optional.ofNullable(metadata.getAnnotationAttributes(ExpiredBulletCondition.class.getName()))
                    .map(annotation -> annotation.get("expireDate"))
                    .filter(expireDate -> ZonedDateTime.now().isBefore(ZonedDateTimeBuilder.build((String) expireDate, "yyyy-MM-dd HH:mm:ss")))
                    .isPresent();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
