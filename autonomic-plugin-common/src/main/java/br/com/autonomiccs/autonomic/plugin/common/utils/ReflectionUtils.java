/*
 * This program is part of Autonomiccs "autonomic-platform",
 * an open source autonomic cloud computing management platform.
 * Copyright (C) 2016 Autonomiccs, Inc.
 *
 * Licensed to the Autonomiccs, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The Autonomiccs, Inc. licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.autonomiccs.autonomic.plugin.common.utils;

import java.lang.reflect.Field;

import org.apache.cxf.common.util.ReflectionUtil;
import org.springframework.stereotype.Component;

import com.cloud.utils.exception.CloudRuntimeException;

/**
 * This class provides support for changing fields values with reflection.
 */
@Component
public class ReflectionUtils {

    /**
     * It sets the given field with value. If it fails to set a value of declared field and the
     * {@link IllegalArgumentException} or {@link IllegalAccessException} are thrown, it throws
     * {@link CloudRuntimeException}
     *
     * @throws CloudRuntimeException
     */
    public void setFieldIntoObject(Object object, String fieldName, Object value) {
        Field declaredField = getDeclaredField(object, fieldName);
        if (declaredField == null) {
            throw new CloudRuntimeException(String.format("Field [fieldName=%s] does not exists into object [%s].", fieldName, object));
        }
        declaredField.setAccessible(true);
        try {
            declaredField.set(object, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new CloudRuntimeException(String.format("Fail to set field [fieldName=%s] into object [%s] with the value [%s].", fieldName, object, value), e);
        }
    }

    /**
     * It returns the object {@link Field} with the given field name.
     */
    protected Field getDeclaredField(Object o, String fieldName) {
        Field declaredField = ReflectionUtil.getDeclaredField(o.getClass(), fieldName);
        if (declaredField != null) {
            return declaredField;
        }
        return ReflectionUtil.getDeclaredField(o.getClass().getSuperclass(), fieldName);
    }
}
