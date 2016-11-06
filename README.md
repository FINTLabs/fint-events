# FINT events

[![Build Status](https://travis-ci.org/FINTprosjektet/fint-events.svg?branch=master)](https://travis-ci.org/FINTprosjektet/fint-events)

Making it easy to dynamically create new queues and listeners for RabbitMQ.

## Installation

build.gradle

```
repositories {
    maven {
        url  "http://dl.bintray.com/fint/maven" 
    }
}

compile('no.fint:fint-events:0.0.2')
```

## Usage

Add `@EnableFintEvents` to the main class

```
@EnableFintEvents
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

`@Autowire` in the `Events` class, and connect the exchange, queue and receiver

```
@Service
public class MyService {

    @Autowired
    private Events events;

    @PostConstruct
    public void init() {
        events.registerListener("my-exchange", "my-queue", Receiver.class);
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

## Organizations

The class `FintEvents` is created as a helper-class to use organizations.
Each organization will generate the default queues on startup (which can be configured in `application.yml`).  

A registered listener will receive messages that are sent to the configured organizations.
Then the listener class can figure out who the organization is by looking at the Message object received.  

It is possible to register listeners for: input, output and error.

```

@Autowired
private FintEvents events;

@PostConstruct
public void init() {
    events.registerInputListener(MessageSubscriber.class);
}

```

## Configuration

Configuration options that can be added to `application.yml`

| Key | Description | Default value |
|-----|-------------|---------------|
| fint.events.orgs | The organizations that are included when generating the default queues. Each organization in the list will generate the exchange (with the org name) and input, output and error queues.  | |
| fint.events.default-input-queue | The format of the default input queue. | %s.input |
| fint.events.default-output-queue | The format of the default output queue. | %s.output |
| fint.events.default-error-queue | The format of the default error queue. | %s.error |
| spring.rabbitmq.listener.retry.initial-interval | Interval between the first and second attempt to deliver a message. | 1000 |
| spring.rabbitmq.listener.retry.max-attempts | Maximum number of attempts to deliver a message. | 3 |
| spring.rabbitmq.listener.retry.max-interval | Maximum interval between attempts. | 10000 |
| spring.rabbitmq.listener.retry.multiplier | A multiplier to apply to the previous delivery retry interval. | 1.0 |

---------

## Event model

Event model is written as a Java-class and available as XSD-file (generated from code).  
  
To generate xsd: `./gradlew schemagen`

---------

## Upload

Upload release to bintray

`./gradlew bintrayUpload -PbintrayUser=<username> -PbintrayKey=<apiKey>`