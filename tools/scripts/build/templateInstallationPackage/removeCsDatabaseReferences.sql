-- This program is part of Autonomiccs "autonomic-platform",
-- an open source autonomic cloud computing management platform.
-- Copyright (C) 2016 Autonomiccs, Inc.
--
-- Licensed to the Autonomiccs, Inc. under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership. The Autonomiccs, Inc. licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--    http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.


-- ----------------------------------------------------------------------------------------------------------------------------------
-- Drops the administration_status column from host table
-- ----------------------------------------------------------------------------------------------------------------------------------
CREATE PROCEDURE drop_column_administrationStatus() BEGIN
IF EXISTS(
SELECT * FROM information_schema.COLUMNS
WHERE table_name = 'host' AND column_name = 'administration_status'
and table_schema ='cloud'
)
THEN
ALTER TABLE host DROP COLUMN administration_status;
END IF;
END;

CALL drop_column_administrationStatus();
DROP PROCEDURE drop_column_administrationStatus;

-- ----------------------------------------------------------------------------------------------------------------------------------
-- Drops the start_type column from host table
-- ----------------------------------------------------------------------------------------------------------------------------------
CREATE PROCEDURE drop_column_startType() BEGIN
IF EXISTS(
SELECT * FROM information_schema.COLUMNS
WHERE table_name = 'host' AND column_name = 'start_type'
and table_schema ='cloud'
)
THEN
ALTER TABLE host DROP COLUMN start_type;
END IF;
END;

CALL drop_column_startType();
DROP PROCEDURE drop_column_startType;

-- ----------------------------------------------------------------------------------------------------------------------------------
-- Drops the administration_status column from cluster table
-- ----------------------------------------------------------------------------------------------------------------------------------
CREATE PROCEDURE drop_column_cluster_administrationStatus() BEGIN
IF EXISTS(
SELECT * FROM information_schema.COLUMNS
WHERE table_name = 'cluster' AND column_name = 'administration_status'
and table_schema ='cloud'
)
THEN
ALTER TABLE cluster DROP COLUMN administration_status;
END IF;
END;
CALL drop_column_cluster_administrationStatus();
DROP PROCEDURE drop_column_cluster_administrationStatus;

-- ----------------------------------------------------------------------------------------------------------------------------------
-- Drops the last_administration column from cluster table
-- ----------------------------------------------------------------------------------------------------------------------------------
CREATE PROCEDURE drop_column_cluster_lastAdministration() BEGIN
IF EXISTS(
SELECT * FROM information_schema.COLUMNS
WHERE table_name = 'cluster' AND column_name = 'last_administration'
and table_schema ='cloud'
)
THEN
ALTER TABLE cluster DROP COLUMN last_administration;
END IF;
END;

CALL drop_column_cluster_lastAdministration();
DROP PROCEDURE drop_column_cluster_lastAdministration;

-- ----------------------------------------------------------------------------------------------------------------------------------
-- Remove configuration from configuration table
-- ----------------------------------------------------------------------------------------------------------------------------------
DELETE FROM configuration WHERE component="autonomicClusterManager";

-- ----------------------------------------------------------------------------------------------------------------------------------
-- Drops Autonomiccs system VMs table
-- ----------------------------------------------------------------------------------------------------------------------------------
DROP TABLE if exists AutonomiccsSystemVm;
