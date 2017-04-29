# FINT Events

[![Build Status](https://travis-ci.org/FINTlibs/fint-events.svg?branch=master)](https://travis-ci.org/FINTlibs/fint-events)
[![Coverage Status](https://coveralls.io/repos/github/FINTlibs/fint-events/badge.svg?branch=master)](https://coveralls.io/github/FINTlibs/fint-events?branch=master)

Event library built on top of [redisson](https://redisson.org/).

# Installation

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/fint/maven" 
    }
}

compile('no.fint:fint-events:0.1.0')
```

# Usage

Add `@EnableFintEvents` to the main class

```java
@EnableFintEvents
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## Publish message on queue

Custom queue name:
```java
@Autowired
private FintEvents fintEvents;

fintEvents.send("queue-name", messageObj);
```

Downstream/upstream queue:
```java
fintEvents.sendDownstream("orgId", messageObj);
fintEvents.sendUpstream("orgId", messageObj);
```

## Register listener

Custom queue name:
```java
fintEvents.registerListener("queue-name", MyListener)
```

Downstream/upstream queue:
```java
fintEvents.registerDownstreamListener("orgId", MyListener)
fintEvents.registerUpstreamListener("orgId", MyListener)
```

## Health check

**Client:**
```java
@Autowired
private FintEventsHealth fintEventsHealth;

Health<TestDto> healthClient = fintEventsHealth.registerClient();
Health<TestDto> response = client.healthCheck(new TestDto());
```

**Server:**  

Create listener bean. This needs to be a bean registered in the Spring container.  
The method that will receive the message is annotated with `@FintEventsListener`.
```java
@Component
public class TestListener {

    @FintEventsListener
    public void receive(TestDto testDto) {
        ...
    }
}
```

Register listener in redisson:
```java
@Autowired
private FintEventsHealth fintEventsHealth;

fintEventsHealth.registerServer(TestHealth);
```