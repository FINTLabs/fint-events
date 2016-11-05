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


## Configuration

Configuration options that can be added to `application.yml`

| Key | Description | Default value |
|-----|-------------|---------------|
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