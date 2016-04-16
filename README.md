# Autonomiccs platform
The Autonomiccs platform is open source software that provides the vital components needed to manage a cloud computing environment autonomously.

This project is a plugin to Apache CloudStack (https://github.com/apache/cloudstack). Our mission is to take the orchestration of cloud computing infrastructures to the next level. To achieve that, we developed a distributed and autonomic plugin that can monitor the CC environment and take decisions in order to fulfill the goals of the given environment. We do not wish to remove the need for human administrators, but to improve their work. Our mission is to optimize and make CC environments more efficient and stable without needing to rely upon human administrators.

## Motivation

Over the past years, cloud computing (CC) usage has increased. To meet that demand the infrastructure required to create and maintain CC environments grows constantly, which impacts on management costs. Thus, the optimization of those environments has become infeasible to humans administrators, requiring an autonomic approach.

Even so, we still do not see autonomic management solutions being used in current commercial orchestration tools such as Apache CloudStack and OpenStack. This priject provides a solution capable of improving the management of virtual machines and physical servers orchestrated by Apache CloudStack. Our solution aims to optimize the cloud provider infrastructure, by ensuring the fulfillment of its goals.

## Solution

In summary, our agents can move workloads around, letting idle servers to power off; those agents also can balance the load in the whole environment and manage the service level agreements between providers and clients.

### Balancing workload
![example of the autonomic agent configured to balance workload]()
### Powering off idle hosts

## Who uses Autonomiccs platform?

## Getting Started