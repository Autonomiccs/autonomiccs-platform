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
package br.com.autonomiccs.autonomic.plugin.common.enums;

import java.io.File;

/**
 * This class serves as an abstraction unit for the jars that are needed by
 * Autonomiccs platform in order to deploy and configure its system VM.
 */
public enum AutonomiccsSystemVmJarsEnum {
	JADE("jade/jade-plataform-agents.jar"), WAKEONLAN("wakeonlan/wakeonlan-service.jar");

	private static final String AUTONOMICCS_JARS_BASE_FOLDER = "/var/lib/autonomiccs/jars/";
	private final String fileName;

	private AutonomiccsSystemVmJarsEnum(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return The jar file correspondent to the abstraction.
	 */
	public File getJarFile() {
		return new File(AUTONOMICCS_JARS_BASE_FOLDER + fileName);
	}
}
