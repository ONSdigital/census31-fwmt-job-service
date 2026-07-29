> **THIS REPO IS SEEDED FROM 2021 CODE AND AS SUCH CURRENTLY NEEDS MODERNISATION!** (see also [SEEDING.md](SEEDING.md).)

# census31-fwmt-job-service
This service is a gateway between Response Management and Total Mobile's COMET interface.

It takes an Field Worker Job Request Canonical (Create, Update, Cancel) message off the Gateway.Actions RabbitMQ Queue and transforms it into a JSON request which is sent to an instance of Tomtal Mobile' COMET endpoint.

![](/docs/jobservice-highlevel.png "jobservicd highlevel diagram")	

## Quick Start

Requires RabbitMQ to start:


    docker run --name rabbit -p 5671-5672:5671:5672 -p 15671-15672:15671-15672 -d rabbitmq:3.6-management

To run:

    mvn spring-boot:run

## tm-canonical-hh
 
![](docs/tm-canonical-hh.png "tm - canonical - hh mapping")

## tm-canonical-ce

![](docs/tm-canonical-ce.png "tm - canonical - ce - mapping")

## tm-canonical-ccs

![](docs/tm-canonical-ccs.png "tm - canonical - ccs - mapping")

## tm-canonical-update

![](docs/tm-canonical-update.png "tm - canonical - update - mapping")

## Transition Architecture Notes

See `docs/transition-strategy-architecture.md` for resolver/strategy execution design,
runtime sequence, and migration notes for the Transitioner refactor.

## Copyright
Copyright(C) 2020 Crown Copyright (Office for National Statistics)

Trigger on branch 3
