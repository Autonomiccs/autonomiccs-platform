[![Autonomiccs Platform](tools/project-logo/autonomiccs.png)](http://autonomiccs.com.br)

### Project status: [![Build Status](http://jenkinsbadge.autonomiccs.com.br/buildStatus/icon?job=Autonomiccs-platform)](http://jenkins.autonomiccs.com.br/job/Autonomiccs-platform/)
 
The <a href="http://autonomiccs.com.br">Autonomiccs platform</a> is an open source distributed virtual machine scheduler (A.K.A distributed resource scheduling); developed as a plugin to <a href="https://github.com/apache/cloudstack">Apache CloudStack</a>, Autonomiccs platform is capable of managing and optimizing an IaaS cloud computing environment autonomously.

The plugin works with CloudStack 4.6 and beyond, and was designed to have a smooth installation and upgrade process. Currently, all algorithms that do not shut down idle hosts are working with every type of hypervisor supported by CloudStack; whereas, the consolidation algorithms are available only for XenServer and XCP (it is a matter of time and resources to implement our platform for all hypervisors supported by ACS).


## Solution

Our agents can migrate virtual machines and power on/off hosts in order to optimize the environment according to guidelines that are set by administrators. The plugin was designed to be flexible, tackling different management objectives.

Autonomiccs platform can move workloads around, allowing idle servers to power off; our agents can also work towards load balancing, migrating virtual machines from overloaded hosts to under loaded ones. For more details on our algorithms and their usage, please check the <a href="https://github.com/Autonomiccs/autonomiccs-platform/wiki/Heuristics">heuristics</a> page.

## Getting Started

- You can find the installation guide <a href="https://github.com/Autonomiccs/autonomiccs-platform/wiki/Installation">here</a>, and you can see all releases at this <a href="https://builds.autonomiccs.com.br/stables/">link</a>; to download the latest stable version go <a href="https://builds.autonomiccs.com.br/autonomiccsPlatformInstallationPackage-master.zip">here</a>;

- to access our master source code, click <a href="https://github.com/Autonomiccs/autonomiccs-platform">here</a>;

- once it is installed, please check the plugin <a href="https://github.com/Autonomiccs/autonomiccs-platform/wiki/Usage">usage</a>.

## Getting involved
The Autonomiccs project welcomes anybody interested in working towards the development of a Cloud Computing autonomic management solution.

You do not need to be a developer to contribute, we are pleased to receive any contribution. We need people that can help with documentation, marketing, design, research and other tasks that are essential to the project life cycle.

### Mailing lists
If you have doubts, problems, suggestions, critics or anything else that can help us improve, you are more than welcome to join us in our mailing lists. We have two distinct mailing lists; one that focuses on users of the Autonomiccs platform; and the other that is directed to the developers of the platform.

We highlight that all participants on the mailing lists are expected to treat one another professionally and politely.
#### Subscribing
If you want to subscribe to a list, send an email to <listname>-subscribe@autonomiccs.com.br:
* To join the users mailing list you should send an email to: users-subscribe@autonomiccs.com.br
* To join the developers mailing list you should send an email to: devs-subscribe@autonomiccs.com.br

#### Unsubscribing
If you have decided that you are getting too much mail and want to cancel your subscription from one of the lists you should send an email to <listname>-unsubscribe@autonomiccs.com.br from the same email you subscribed with.
* To leave the users mailing list you should send an email to: users-unsubscribe@autonomiccs.com.br
* To leave the developers mailing list you should send an email to: devs-unsubscribe@autonomiccs.com.br

## More details
Please, go to our <a href="https://github.com/Autonomiccs/autonomiccs-platform/wiki">wiki</a> page for more details; there you can understand more about the <a href="https://github.com/Autonomiccs/autonomiccs-platform/wiki/Autonomiccs-platform">Autonomiccs platform</a>, some <a href="https://github.com/Autonomiccs/autonomiccs-platform/wiki/Basic-concepts">basic concepts</a>, what are <a href="https://github.com/Autonomiccs/autonomiccs-platform/wiki/Heuristics">heuristics</a> and how we use it, how to <a href="https://github.com/Autonomiccs/autonomiccs-platform/wiki/Installation">install</a> and <a href="https://github.com/Autonomiccs/autonomiccs-platform/wiki/Usage">use</a>.

You can find out more about Autonomiccs visiting the [website](http://autonomiccs.com.br).

## Licence

This project is part of Autonomiccs, an open source autonomic cloud computing management platform. Copyright (C) 2016 Autonomiccs, Inc.

Licensed to the Autonomiccs, Inc under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.

Please see the <a href="https://github.com/Autonomiccs/autonomiccs-platform/blob/master/LICENSE">LICENSE</a> file included in the root directory of the project for more details.

<p align="center">
	<img src="https://github.com/Autonomiccs/autonomiccs-platform/blob/master/tools/project-logo/autonomiccsWhite.png" width="150">
</p>
