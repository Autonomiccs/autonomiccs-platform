#!/bin/bash
# This program is part of Autonomiccs "autonomic-platform",
# an open source autonomic cloud computing management platform.
# Copyright (C) 2016 Autonomiccs, Inc.
#
# Licensed to the Autonomiccs, Inc. under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The Autonomiccs, Inc. licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
######################################################################################################################
## This script is used to create the Autonomiccs platform installer.
## It assumes that you have the Autonomiccs platform already built and properly installed in your Maven local repository.
######################################################################################################################
## Dependencies to run this script:
##	You will need the command line application "zip"
##	You can install it either with apt-get, aptitude or yum, depending on your operating system.
######################################################################################################################

[[  -z  $1  ]] && echo "You must inform the version of the Autonomiccs platform you want to build." && exit 1
AUTONOMICCS_PLATFORM_VERSION=$1

MAVEN_REPOSITORY_PATH=$(mvn help:evaluate -Dexpression=settings.localRepository | grep -v '\[INFO\]')
AUTONOMICCS_DEPENDENCIES_PATH="br/com/autonomiccs/autonomic-plugin-common/$AUTONOMICCS_PLATFORM_VERSION"

[ ! -d "$MAVEN_REPOSITORY_PATH/$AUTONOMICCS_DEPENDENCIES_PATH" ] && echo "We could not find the Autonomiccs jars directory [$MAVEN_REPOSITORY_PATH/$AUTONOMICCS_DEPENDENCIES_PATH]" && echo "you should first build the Autonomiccs platform, then you can create the installation package" && exit 1

CURRENT_DIR=$(pwd)
if [[ "$CURRENT_DIR" == *build ]]
then
	AUTONOMICCS_PACKAGE_FOLDER=autonomiccsPlatformInstallationPackage
	echo "Starting the build of the Autonomiccs platform using Maven"
	mkdir -p "$AUTONOMICCS_PACKAGE_FOLDER/META-INF/cloudstack/bootstrap/"
	cp -r templateInstallationPackage/* $AUTONOMICCS_PACKAGE_FOLDER
	echo "copying the XML that holds the proxy configurations"
	AUTONOMICCS_JARS_FOLDERS=autonomiccsJars
	cp ../../../starthost-plugin/target/classes/proxyToStartHostConfigurations.xml "$AUTONOMICCS_PACKAGE_FOLDER/META-INF/cloudstack/bootstrap/spring-bootstrap-context-inheritable.xml"
	echo "copying the Autonomiccs jars"
	cp "../../../autonomic-algorithms-commons/target/autonomic-algorithms-commons-$AUTONOMICCS_PLATFORM_VERSION.jar"	$AUTONOMICCS_PACKAGE_FOLDER/$AUTONOMICCS_JARS_FOLDERS/
	cp "../../../autonomic-allocation-algorithms/target/autonomic-allocation-algorithms-$AUTONOMICCS_PLATFORM_VERSION.jar"        $AUTONOMICCS_PACKAGE_FOLDER/$AUTONOMICCS_JARS_FOLDERS/
	cp "../../../autonomic-administration-algorithms/target/autonomic-administration-algorithms-$AUTONOMICCS_PLATFORM_VERSION.jar"	$AUTONOMICCS_PACKAGE_FOLDER/$AUTONOMICCS_JARS_FOLDERS/
	cp "../../../autonomic-administration-plugin/target/autonomic-administration-plugin-$AUTONOMICCS_PLATFORM_VERSION.jar"	$AUTONOMICCS_PACKAGE_FOLDER/$AUTONOMICCS_JARS_FOLDERS/
	cp "../../../autonomic-plugin-common/target/autonomic-plugin-common-$AUTONOMICCS_PLATFORM_VERSION.jar"	$AUTONOMICCS_PACKAGE_FOLDER/$AUTONOMICCS_JARS_FOLDERS/
	cp "../../../starthost-plugin/target/starthost-plugin-$AUTONOMICCS_PLATFORM_VERSION.jar" $AUTONOMICCS_PACKAGE_FOLDER/$AUTONOMICCS_JARS_FOLDERS/
	cp "../../../wakeonlan-service/target/wakeonlan-service-$AUTONOMICCS_PLATFORM_VERSION.jar"	$AUTONOMICCS_PACKAGE_FOLDER/$AUTONOMICCS_JARS_FOLDERS/
	echo "copying the Autonomiccs dependencies"
	mkdir -p "$AUTONOMICCS_PACKAGE_FOLDER/$AUTONOMICCS_JARS_FOLDERS/dependencies/"
	cp "$MAVEN_REPOSITORY_PATH/org/apache/commons/commons-math3/3.6/commons-math3-3.6.jar"	$AUTONOMICCS_PACKAGE_FOLDER/$AUTONOMICCS_JARS_FOLDERS/dependencies/
	cp "$MAVEN_REPOSITORY_PATH/org/springframework/spring-jdbc/3.2.12.RELEASE/spring-jdbc-3.2.12.RELEASE.jar"	$AUTONOMICCS_PACKAGE_FOLDER/$AUTONOMICCS_JARS_FOLDERS/dependencies/
	cp "$MAVEN_REPOSITORY_PATH/org/springframework/integration/spring-integration-core/3.0.7.RELEASE/spring-integration-core-3.0.7.RELEASE.jar"	$AUTONOMICCS_PACKAGE_FOLDER/$AUTONOMICCS_JARS_FOLDERS/dependencies/
	cp "$MAVEN_REPOSITORY_PATH/org/springframework/spring-tx/3.2.12.RELEASE/spring-tx-3.2.12.RELEASE.jar" 	$AUTONOMICCS_PACKAGE_FOLDER/$AUTONOMICCS_JARS_FOLDERS/dependencies/
	echo "creating zip package now"
	zip -r "$AUTONOMICCS_PACKAGE_FOLDER.zip" "$AUTONOMICCS_PACKAGE_FOLDER"
else
	echo "You should execute the build script within the build folder."
	exit 1
fi
