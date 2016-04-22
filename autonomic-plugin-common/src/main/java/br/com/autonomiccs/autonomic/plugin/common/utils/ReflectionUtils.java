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

@Component
public class ReflectionUtils {

    public void setFieldIntoObject(Object registerTemplateCmd, String fieldName, Object value) {
        Field declaredField = getDeclaredField(registerTemplateCmd, fieldName);
        declaredField.setAccessible(true);
        try {
            declaredField.set(registerTemplateCmd, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Could not register Autonomiccs System VM templates.", e);
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
