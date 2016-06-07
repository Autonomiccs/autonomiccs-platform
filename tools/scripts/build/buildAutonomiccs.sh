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

###########################################################################################################
## This script is used to build Autonomiccs platform.
## It assumes that you have the dependencies of CloudStack that it uses already installed in your maven local repository.
###########################################################################################################

CLOUDSTACK_DEPENDENCY_PATH="org/apache/cloudstack"
MAVEN_REPOSITORY_PATH=$(mvn help:evaluate -Dexpression=settings.localRepository | grep -v '\[INFO\]')

[ ! -d "$MAVEN_REPOSITORY_PATH/$CLOUDSTACK_DEPENDENCY_PATH" ] && echo 'We could not find the CloudStack jars directory ' && echo "you should first build the CloudStack, then the Autonomiccs platform" && exit 1
CURRENT_DIR=$(pwd)

if [[ "$CURRENT_DIR" == *build ]]
then
	cd ../../../
	echo "Starting the build of the Autonomiccs platform using Maven"
	mvn clean install
else
	echo "You should execute the build script within the build folder."
	exit 1
fi
