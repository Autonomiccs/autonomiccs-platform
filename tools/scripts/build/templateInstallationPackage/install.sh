#!/bin/bash
# This program is part of Autonomiccs "autonomic-platform",
# an open source autonomic cloud computing management platform.
# Copyright (C) 2016 Autonomiccs, Inc.
#
# Licensed to the Autonomiccs, Inc. under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The The Autonomiccs, Inc. licenses this file
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
##########################################################################################################
## This script needs the program called "jar", it comes with the JDK.
## Be sure to export/install it properly before executing this script
##########################################################################################################

preffixParam1="CloudStack is running, please stop the CloudStack service before installing Autonomiccs platform!";
preffixParam2="Install aborted!";
./scriptPreffix.sh $preffixParam1 $preffixParam2 || exit 1;

echo "Installing ...";

SPRINGXML="META-INF/cloudstack/bootstrap/spring-bootstrap-context-inheritable.xml";

echo "CloudStack jars files are in the default directory [/usr/share/cloudstack-management/webapps/client/WEB-INF/lib/]? (y/n)"
read USERINPUT;
if [ $USERINPUT == "y" ]; then
	CSDIR="/usr/share/cloudstack-management/webapps/client/WEB-INF/lib/";
else 
	echo "Please insert a directory path that the CloudStack jars are located";
	read CSDIR;
fi
if [ ! -d "$CSDIR" ]; then
  echo "Directory [$CSDIR] does not exist!";
  exit 1;
fi

echo "Working on directory [$CSDIR]";

CLOUDCORE=$(ls $CSDIR | grep cloud-core);
if [ -z $CLOUDCORE ]; then
	echo "Could not find any jar named cloud-core in the given path [$CSDIR]!";
	exit 1;
fi

echo "Copying a backup file of [$CSDIR$CLOUDCORE] file to the current directory";
cp $CSDIR$CLOUDCORE $CLOUDCORE || exit 1;
cp $CLOUDCORE $CLOUDCORE.bkp || exit 1;

jar uf $CLOUDCORE $SPRINGXML || exit 1;
chmod 644 $CLOUDCORE || exit 1;

cp $CLOUDCORE $CSDIR$CLOUDCORE;

chmod u+x autonomiccsJars/moveJars.sh || exit 1;
autonomiccsJars/moveJars.sh || exit 1;

echo "Autonomiccs platform installed with success!";

