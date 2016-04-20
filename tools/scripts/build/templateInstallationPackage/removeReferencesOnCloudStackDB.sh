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

echo "Removing Autonomiccs platform data and data-structure (tables, columns and rows) of the the ACS database";

echo "Please inform the ACS database host-name";
read HOST;

echo "Please inform the ACS database port.";
read PORT;

echo "Please inform the database user";
read USER;

echo "Please inform the database password";
read PASSWORD;

IPS=$(mysql -N -h "$HOST" -P "$PORT" -u "$USER" "-p$PASSWORD" "cloud" < "selectSystemVMsIPs.sql");
echo "System VMs IPs:";
echo "$IPS";
for IP in $IPS
do
        echo "Shuting down System VM [ip=$IP]";
        ssh -oStrictHostKeyChecking=no -i id_rsa root@$IP 'halt -p'
done

mysql -h "$HOST" -P "$PORT" -u "$USER" "-p$PASSWORD" "cloud" < "removeCsDatabaseReferences.sql";
if [ "$?" -eq 0 ]; then
    echo "Autonomiccs platform references of the ACS database have been removed!";
else
    echo "Failed to execute removeCsDatabaseReferences.sql query to remove Autonomiccs platform database structures!";
	exit 1;
fi

exit 0;
