# FINT events

[![Build Status](https://travis-ci.org/FINTprosjektet/fint-events.svg?branch=master)](https://travis-ci.org/FINTprosjektet/fint-events)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/17468ef5fdac4b89b1cc5e81806ad2d2)](https://www.codacy.com/app/jarle/fint-events?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=FINTprosjektet/fint-events&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/17468ef5fdac4b89b1cc5e81806ad2d2)](https://www.codacy.com/app/jarle/fint-events?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=FINTprosjektet/fint-events&amp;utm_campaign=Badge_Coverage)

Making it easy to dynamically create new queues and listeners for RabbitMQ.

* [Installation](#installation)
* [Usage](#usage)
* [Organizations](#organizations)
* [Configuration](#configuration)
 * [Rabbitmq config](#rabbitmq-config)
 * [Listener config](#listener-config)
 * [FINT events config](#fint-events-config)
* [Upload](#upload)

## Installation

build.gradle

```
repositories {
    maven {
        url  "http://dl.bintray.com/fint/maven" 
    }
}

compile('no.fint:fint-events:0.0.19')
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

Create a receiver class. The method receiving the message accepts the following arguments:  
- Message
- Map<String, Object> header, byte[] body
- byte[] body

```
@Component
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
  port:
  virtual-host:
```

## Organizations

The class `FintEvents` is created as a helper-class to use organizations.
Each organization will generate the default queues on startup (which can be configured in `application.yml`).  

A registered listener will receive messages that are sent to the configured organizations.
Then the listener class can figure out who the organization is by looking at the Message object received.  

It is possible to register listeners for: _input_, _output_ and _error_.

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
Spring Boot - Common application properties: http://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html

### Rabbitmq config
| Key | Description | Default value |
|-----|-------------|---------------|
| fint.rabbitmq.host | RabbitMQ host. | localhost |
| fint.rabbitmq.username | Login user to authenticate to the broker. | |
| fint.rabbitmq.password | Login to authenticate against the broker. | |
| fint.rabbitmq.port | RabbitMQ port. | 5672 |
| fint.rabbitmq.virtual-host | Virtual host to use when connecting to the broker. | |

### Listener config
| Key | Description | Default value |
|-----|-------------|---------------|
| fint.listener.retry.initial-interval | Interval between the first and second attempt to deliver a message. | 30000 |
| fint.listener.retry.max-attempts | Maximum number of attempts to deliver a message. | 3 |
| fint.listener.retry.max-interval | Maximum interval between attempts. | 30000 |
| fint.listener.retry.multiplier | A multiplier to apply to the previous delivery retry interval. | 1.0 |
| fint.listener.acknowledge-mode | Acknowledge mode of container. | AUTO |

### FINT events config
| Key | Description | Default value |
|-----|-------------|---------------|
| fint.events.orgs | The organizations that are included when generating the default queues. Each organization will generate the exchange (with the org name) and input, output and error queues. | |
| fint.events.default-downstream-queue | The format of the default downstream queue. | %s.input |
| fint.events.default-upstream-queue | The format of the default upstream queue. | %s.output |
| fint.events.default-error-queue | The format of the default error queue. | %s.error |
| fint.events.test-mode | Test mode will not automatically connect to rabbitmq, making it easier to test without requiring rabbitmq. | false |

---------

## Upload

Upload release to bintray

`./gradlew bintrayUpload -PbintrayUser=<username> -PbintrayKey=<apiKey>`