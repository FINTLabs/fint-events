# FINT Events

[![Build Status](https://travis-ci.org/FINTlibs/fint-events.svg?branch=master)](https://travis-ci.org/FINTlibs/fint-events)
[![Coverage Status](https://coveralls.io/repos/github/FINTlibs/fint-events/badge.svg?branch=master)](https://coveralls.io/github/FINTlibs/fint-events?branch=master)

Event library built on top of [redisson](https://redisson.org/).

* [Installation](#installation)
* [Usage](#usage)
  * [Publish message on queue](#publish-message-on-queue)
  * [Register listener](#register-listener)
  * [Health check](#health-check)
  * [Fint Events endpoints](#fint-events-endpoints)
  * [Configuration](#configuration)

---

# Installation

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/fint/maven" 
    }
}

compile('no.fint:fint-events:0.1.11')
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
fintEvents.registerDownstreamListener(MyListener, "orgId")
fintEvents.registerUpstreamListener(MyListener, "orgId")
```

Get registered listeners (Queue name + time registered):
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
Enabled with the property `fint.events.queue-endpoint-enabled`.  

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

`GET /fint-events/queues/{queue}`

This will use a `peek()` method on the actual queue, meaning it will not be removed.  
The response contains size of the queue and the next value. The length of the shown next value in the queue will be max 300 characters.
```json
{
  "size": "3",
  "value": "Event{corrId='43ab45e1-ed06-404d-a093-3f92cf37fc3d', ...}"
}
```

Get the value in the queue on the specified index.

`GET /fint-events/queues/{queue}?index=0`


## Configuration

Redisson configuration is added in a file `redisson.yml` on classpath (`src/main/resources`).  
It also supports to separate config-files for the Spring profile used, for example `redisson-test.yml` when using the test profile.
If no config-file is found the default values are used: `Single server, 127.0.0.1:6379`  
If test-model is enabled, the default config will always be used.

* **[Redisson configuration](https://github.com/redisson/redisson/wiki/2.-Configuration)**
* [Single instance mode](https://github.com/redisson/redisson/wiki/2.-Configuration#26-single-instance-mode)
* [Cluster mode](https://github.com/redisson/redisson/wiki/2.-Configuration#24-cluster-mode)

| Key | Description | Default value |
|-----|-------------|---------------|
| fint.events.default-downstream-queue | The format of the default downstream queue. | %s.downstream |
| fint.events.default-upstream-queue | The format of the default upstream queue. | %s.upstream |
| fint.events.test-mode | When test mode is enable, an embedded redis instance is initialized on startup. It will also use the default redisson config `single server, 127.0.0.1:6379`. | false |
| fint.events.queue-endpoint-enabled | Enable the rest endpoints `/fint-events/*` that make it possible to query the content of the queues. If the endpoint is disable a 404 response code is returned. | false |
| fint.events.task-scheduler-thread-pool-size | The number of threads in the task scheduler thread pool. This will be used by all event listeners and `@Scheduled` methods. | 50 |
| fint.events.healthcheck.timeout-in-seconds | The number of seconds the health check client will wait before timing out. | 120 |