# Autonomiccs platform
The Autonomiccs platform is open source software that provides the vital components needed to manage a cloud computing environment autonomously.

This project is a plugin to Apache CloudStack (https://github.com/apache/cloudstack). Our mission is to take the orchestration of cloud computing infrastructures to the next level. To achieve that, we developed a distributed and autonomic plugin that can monitor the CC environment and take decisions in order to fulfill the goals of the given environment. We do not wish to remove the need for human administrators, but to improve their work. Our mission is to optimize and make CC environments more efficient and stable without needing to rely on human administrators.

## Motivation

Over the past years, cloud computing (CC) usage has increased. To meet that demand the infrastructure required to create and maintain CC environments grows constantly, which impacts on management costs. Thus, the optimization of those environments has become infeasible to humans administrators, requiring an autonomic approach.

Even so, we still do not see autonomic management solutions being used in current commercial orchestration tools such as Apache CloudStack and OpenStack. This project provides a solution capable of improving the management of virtual machines and physical servers orchestrated by Apache CloudStack. Our solution aims to optimize the cloud provider infrastructure, by ensuring the fulfillment of its goals.

## Solution

In summary, our agents can move workloads around, letting idle servers to power off; those agents also can balance the load in the whole environment and manage the service level agreements between providers and clients. The plugin as designed to be flexible and strive to achieve different goals. TOmanage cloud computing environments 

### Balancing workload

<p align="center">
	<img src="https://github.com/Autonomiccs/autonomiccs-platform/blob/master/figures/balancing.jpg" width="250">
</p>

### Powering off idle hosts

<p align="center">
	<img src="https://github.com/Autonomiccs/autonomiccs-platform/blob/master/figures/consolidating.jpg" width="250">
</p>

## Who Autonomiccs is for?

Our main focus is to get CloudStack to the next generation of cloud computing orchestration platforms. One who shares our enthusiasm may benefit from this project.
    
We foresee the following types of users of this solution:
- companies that use ACS;
- companies that provide ACS consulting and support services to other companies;
- Cloud Computing research labs;

The first user of this solution is the Network and Management Laboratory of the Federal University of Santa Catarina (<a href="https://wiki.lrg.ufsc.br/">LRG</a>).

## Getting Source Repository

Autonomiccs Platform project uses Git and a mirror is hosted on <a href="https://github.com/Autonomiccs/autonomiccs-platform">GitHub</a>.

The Github mirror allows users and developers to explore the code; however, contributions from the community can be done only via Github pull requests.

## Getting Involved and Contributing

The Autonomiccs project welcomes anybody interested to work towards the development of Cloud Computing autonomic management.

You don't need to be a developer to contribute with this project, we are pleased with any contribution. We need people that can help with documentation, promotion, design, evolve research topics etc.

Mailing lists:
- Hold on, we are working on that.

## Licence

This project is part of Autonomiccs, an open source autonomic cloud computing management platform. Copyright (C) 2016 Autonomiccs, Inc.

Licensed to the Autonomiccs, Inc under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at

   http:www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY IND, either express or implied.  See the License for the specific language governing permissions and limitations under the License.

Please see the <a href="https://github.com/Autonomiccs/autonomiccs-platform/blob/master/LICENSE">LICENSE</a> file included in the root directory of the project for more details.


