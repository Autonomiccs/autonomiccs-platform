#!/bin/bash

# This program is part of Autonomiccs "autonomic-platform",
# an open source autonomic cloud computing management platform.
# Copyright (C) 2016 Autonomiccs, Inc.
#
# Licensed to the Autonomiccs, Inc. under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http:www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

CSSTATUS=$(service cloudstack-management status);
if [[ $CSSTATUS == *"is running"* ]]; then
  echo "CloudStack is running, please stop the CloudStack service before being able to uninstall Autonomiccs platform!";
  echo "Uninstall aborted!"
  exit 1;
fi

echo "Uninstalling ...";
echo "CloudStack jars files are in the default directory [/usr/share/cloudstack-management/webapps/client/WEB-INF/lib/]? (y/n)"
read USERINPUT
if [[ $USERINPUT == "y" ]]; then
	CSDIR="/usr/share/cloudstack-management/webapps/client/WEB-INF/lib/";
else 
	echo "Please insert a directory path that the CloudStack classpath jars are located.";
	read CSDIR;
fi
if [ ! -d "$CSDIR" ]; then
  echo "Directory [$CSDI] doesn't exist!";
  echo "Uninstall aborted!"
  exit 1;
fi

echo "Working on directory [$CSDIR]";

BASEDIR=$(pwd);
CLOUDCOREBackUp=$(ls cloud-core*.bkp);
CloudCoreOriginalName=$(ls $CSDIR | grep cloud-core);
if [[ -z $CLOUDCOREBackUp ]]; then
	echo "Could not find any backup of the cloud-core jar at path [$BASEDIR]!";
	exit 1;
fi
echo "Moving the $CLOUDCOREBackUp jar file to [$CSDIR]";	
cp $CLOUDCOREBackUp $CSDIR$CloudCoreOriginalName || exit 1;

echo "Removing all Autonomiccs platform jars from [$CSDIR]";
chmod u+x autonomiccsJars/removeJars.sh || exit 1;
autonomiccsJars/removeJars.sh || exit 1;

chmod u+x removeReferencesOnCloudStackDB.sh;
./removeReferencesOnCloudStackDB.sh || exit 1; 

echo "The uninstall of Autonomiccs platform was complete with success.";

