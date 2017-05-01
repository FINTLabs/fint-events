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

compile('no.fint:fint-events:0.1.3')
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

Create listener bean. This needs to be a bean registered in the Spring container.  
The method that will receive the message is annotated with `@FintEventListener`:
```java
@Component
public class TestListener {

    @FintEventListener
    public void receive(TestDto testDto) {
        ...
    }
}
```

Custom queue name:
```java
fintEvents.registerListener("queue-name", MyListener)
```

Downstream/upstream queue:
```java
fintEvents.registerDownstreamListener("orgId", MyListener)
fintEvents.registerUpstreamListener("orgId", MyListener)
```

Get registered listeners:
```java
Map<String, Long> listeners = fintEvents.getListeners();
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

```java
@Component
public class TestHealth implements HealthCheck<TestDto> {
    @Override
    public TestDto check(TestDto value) {
        ...
        return value;
    }
}

```

Register listener in redisson:
```java
@Autowired
private FintEventsHealth fintEventsHealth;

fintEventsHealth.registerServer(TestHealth);
```

## Remote Service

We recommend publishing messages instead of using the remote service feature. This is a blocking call, where the client will wait for a response or a timeout happens.


**Client:**
```java
@Autowired
private FintEventsRemote fintEventsRemote;

RemoteEvent<TestDto> remoteEvent = fintEventsRemote.registerClient();
```

**Server:**
```java
@Component
public class TestListener {

    @FintEventListener
    public void receive(TestDto testDto) {
        ...
    }
}
```

Register listener in redisson:
```java
@Autowired
private FintEventsRemote fintEventsRemote;

fintEventsRemote.registerServer(TestListener);
```

### Run RemoteService integration tests

Add the system property: `remoteServiceTestsEnabled=true`

## Fint Events endpoints

Makes it possible to query the content of the queues.  
Enabled with the property `int.events.queue-endpoint-enabled`.  

If use with [springfox-loader](https://github.com/jarlehansen/springfox-loader), add the `FintEventsController`:
```java
@EnableSpringfox(includeControllers = FintEventsController.class)
```

**GET all queue names**

`GET /fint-events/queues`

Response:
```json
[
  "mock.no.upstream",
  "mock.no.downstream"
]
```

**GET content of queue**

This will use a `peek()` method on the actual queue, meaning it will not be removed.  
The endpoint is available on `/fint-events/queues/{queue}`.  

The response contains size of the queue and the next value. The length of the shown next value in the queue will be max 200 characters.
```json
{
  "size": "3",
  "nextValue": "Event{corrId='43ab45e1-ed06-404d-a093-3f92cf37fc3d', ...}"
}
```

## Configuration

| Key | Description | Default value |
|-----|-------------|---------------|
| fint.events.redis-configuration | The [configuration](https://github.com/redisson/redisson/wiki/2.-Configuration) used when redisson is connecting to redis. For example clustered or single server. | single |
| fint.events.redis-address | Redis server address, includes port. | localhost:6379 |
| fint.events.default-downstream-queue | The format of the default downstream queue. | %s.downstream |
| fint.events.default-upstream-queue | The format of the default upstream queue. | %s.upstream |
| fint.events.test-mode | When test mode is enable, an embedded redis instance is initialized on startup | false |
| fint.events.queue-endpoint-enabled | Enable the rest endpoints `/fint-events/*` that make it possible to query the content of the queues. If the endpoint is disable a 404 response code is returned. | false |