# FINT events

[![Build Status](https://travis-ci.org/FINTprosjektet/fint-events.svg?branch=master)](https://travis-ci.org/FINTprosjektet/fint-events)

## Event model

Event model is written as a Java-class and available as XSD-file (generated from code).

---------

## Dynamic RabbitMQ Spring Boot

Making it easy to dynamically create new queues and listeners for RabbitMQ.

## Installation

build.gradle

```
repositories {
    maven {
        url  "http://dl.bintray.com/fint/maven" 
    }
}

compile('no.fint:dynamic-rabbitmq-spring-boot:0.0.3')
```

## Usage

Add `@EnableDynamicRabbit` to the main class

```
@EnableDynamicRabbit
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

Create a receiver class
```
public class Receiver {
    public void receive(Message message) {
        ...
    }
}

```

`@Autowire` in the DynamicQueues class, and connect the exchange, queue and receiver

```
@Service
public class MyService {

    @Autowired
    private DynamicQueues dynamicQueues;

    @PostConstruct
    public void init() {
        dynamicQueues.registerListener("my-exchange", "my-queue", Receiver.class);
    }
}
```

Add rabbitmq configuration to `application.yml`

```
spring:
 rabbitmq:
  host:
  username:
  password:
  virtual-host:
```


## Configuration

Configuration options that can be added to `application.yml`

| Key | Description | Default value |
|-----|-------------|---------------|
| spring.rabbitmq.listener.retry.initial-interval | Interval between the first and second attempt to deliver a message. | 1000 |
| spring.rabbitmq.listener.retry.max-attempts | Maximum number of attempts to deliver a message. | 3 |
| spring.rabbitmq.listener.retry.max-interval | Maximum interval between attempts. | 10000 |
| spring.rabbitmq.listener.retry.multiplier | A multiplier to apply to the previous delivery retry interval. | 1.0 |


## Upload

Upload release to bintray

`./gradlew bintrayUpload -PbintrayUser=<username> -PbintrayKey=<apiKey>`