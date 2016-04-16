package br.com.autonomiccs.autonomic.plugin.common.utils;

import java.lang.reflect.Field;

import org.apache.cxf.common.util.ReflectionUtil;
import org.springframework.stereotype.Component;

@Component
public class ReflectionUtils {

    public void setFieldIntoObject(Object registerTemplateCmd, String fieldName, Object value) {
        Field declaredField = getDeclaredField(registerTemplateCmd, fieldName);
        declaredField.setAccessible(true);
        try {
            declaredField.set(registerTemplateCmd, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Could not register Clever Clouds System VM templates.", e);
        }
    }

    private Field getDeclaredField(Object o, String fieldName) {
        Field declaredField = ReflectionUtil.getDeclaredField(o.getClass(), fieldName);
        if (declaredField != null) {
            return declaredField;
        }
        return ReflectionUtil.getDeclaredField(o.getClass().getSuperclass(), fieldName);
    }
}
