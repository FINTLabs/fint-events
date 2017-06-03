# FINT Events

[![Build Status](https://travis-ci.org/FINTlibs/fint-events.svg?branch=master)](https://travis-ci.org/FINTlibs/fint-events)
[![Coverage Status](https://coveralls.io/repos/github/FINTlibs/fint-events/badge.svg?branch=master)](https://coveralls.io/github/FINTlibs/fint-events?branch=master)

Event library built on top of [redisson](https://redisson.org/).

* [Installation](#installation)
* [Usage](#usage)
  * [Publish message on queue](#publish-message-on-queue)
  * [Register listener](#register-listener)
  * [Queue name configuration](#queue-name-configuration)
  * [Temporary queues](#temporary-queues)
  * [Health check](#health-check)
  * [Fint Events endpoints](#fint-events-endpoints)
  * [Reconnect](#reconnect)
  * [Configuration](#configuration)

---

# Installation

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/fint/maven" 
    }
}

compile('no.fint:fint-events:0.1.31')
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
fintEvents.registerListener("queue-name", MyListener);
```

Downstream/upstream queue:
```java
fintEvents.registerDownstreamListener(MyListener.class, "orgId");
fintEvents.registerUpstreamListener(MyListener.class, "orgId");
```

Get registered listeners (Queue name + time registered):
```java
Map<String, Long> listeners = fintEvents.getListeners();
```

If orgId(s) are added to `fint.events.orgIds`, event listeners can be automatically registered.  
See [configuration](#configuration) for more details.

## Queue name configuration

If you need more control to customize the queue name than with the properties (`fint.events.env`/ `fint.events.component`)
it is possible to use the `QueueName` object.  

```java
QueueName.with(orgId);
QueueName.with(component, orgId);
QueueName.with(env, component, orgId);    
```

The object can be sent into methods that uses a queue, for example:
  
```java
fintEvents.getDownstream(queueName);
fintEvents.sendUpstream(queueName, value);
fintEvents.registerUpstreamListener(MyListener.class, queueName);
```
If the value is set in the QueueName object, it will be used instead of the configured properties.  
If a value is null in QueueName the configured values are used.

## Temporary queues

A temporary queue is a short-lived queue that will not be registered in the queue names list.  
It will also have a standard prefix (`temp-`) making it easy to find all temporary queues in redis.  

```java
fintEvents.getTempQueue("my-queue");
```

In this example the queue name in redis will be `temp-my-queue`.

All temporary queues can be deleted by calling `deleteTempQueues`:
```java
boolean deleted = fintEvents.deleteTempQueues();
```

## Health check

**Client:**
```java
@Autowired
private FintEventsHealth fintEventsHealth;

Event response = fintEventsHealth.sendHealthCheck("orgId", "id", myEvent);
```

If the response object is null, the request has timed out before a response was received.  
The id should be set to a value that is unique for the object sent (in this example it will be corrId for the event).

**Server:**  

```java
@Component
public class HealthCheckListener {

    @Autowired
    private FintEventsHealth fintEventsHealth;

    @FintEventListener
    public void receive(Event event) {
        ...
        fintEventsHealth.respondHealthCheck("id", event);
    }
}

```

### Run Listener integration tests

Add the system property: `listenerIntegrationTestsEnabled=true`

## Fint Events endpoints

Makes it possible to query the content of the queues.  
Enabled with the property `fint.events.queue-endpoint-enabled`.  

If use with [springfox-loader](https://github.com/jarlehansen/springfox-loader), add the `FintEventsController`:
```java
@EnableSpringfox(includeControllers = FintEventsController.class)
```

**GET all queue names**

`GET /fint-events/queues`

* *componentQueues*, the queues registered for the specific instance of the application
* *queues*, all queues created with fint-events that are sharing the same instances of redis

Response:
```json
{
  "componentQueues": [
    "mock.no.upstream",
    "mock.no.downstream"
  ],
  "queues": [
    "mock.no.upstream",
    "mock.no.downstream"
  ]
}
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

**Get registered listeners**

Returns the name of the listener and the timestamp when it was registered.

`GET /fint-events/listeners`

**Get redisson config**

Return the redisson configuration values. Does not display redisson.yml if that is used.

`GET /fint-events/redissonConfig`

## Reconnect

When there is a need to reconnect the redisson client (when the default reconnection strategy from redisson does not work for some reason):
```java
fintEvents.reconnect();
```
This will shutdown the redisson client and recreate it.  
An event of type `RedissonReconnectedEvent` is published after the reconnection is finished.
If there are clients that need to re-initialize, it is possible to register a listener:
```java
@EventListener(RedissonReconnectedEvent.class)
public void reconnect() {
    ...
}
```

## Configuration

| Key | Description | Default value |
|-----|-------------|---------------|
| fint.events.orgIds | The organisations that are included when generating the event listeners. The default listeners are only created if there is one event listener registered, and the listener has specified queue type `@FintEventListener(type = QueueType.DOWNSTREAM)`. Value can be a comma separated list of orgIds. | Empty array |
| fint.events.env | The environment that the system is running in, for example test / prod. Used to build the downstream/upstream queue name. | local |
| fint.events.component | The component name. Used to build the downstream/upstream queue name. | default |
| fint.events.default-downstream-queue | The format of the default downstream queue. {component}=`fint.events.component` {env}=`fint.events.env` | `downstream_{component}_{env}_{orgId}` |
| fint.events.default-upstream-queue | The format of the default upstream queue. {component}=`fint.events.component` {env}=`fint.events.env` | `upstream_{component}_{env}_{orgId}` |
| fint.events.test-mode | When test mode is enable, an embedded redis instance is initialized on startup. It will also use the default redisson config `single server, 127.0.0.1:6379`. | false |
| fint.events.queue-endpoint-enabled | Enable the rest endpoints `/fint-events/*` that make it possible to query the content of the queues. If the endpoint is disable a 404 response code is returned. | false |
| fint.events.task-scheduler-thread-pool-size | The number of threads in the task scheduler thread pool. This will be used by all event listeners and `@Scheduled` methods. | 50 |
| fint.events.healthcheck.timeout-in-seconds | The number of seconds the health check client will wait before timing out. | 90 |

### Redisson config

[Redisson documentation - configuration](https://github.com/redisson/redisson/wiki/2.-Configuration)  
There are three ways to configure redisson with fint-events:
* Redisson configuration is added in a file `redisson.yml` on classpath (`src/main/resources`). It also supports to separate config-files for the Spring profile used, for example `redisson-test.yml` when using the test profile.
* Set system properties, as described in the table below
* If no redisson-file or system properties are configured the default values are used: `Single server, 127.0.0.1:6379`  

If test-model is enabled, the default config will always be used.


| Key | Description | Default value |
|-----|-------------|---------------|
| fint.events.redisson.addresses | Address(es) to redis. If single server only one address should be configured. | redis://127.0.0.1:6379 |
| fint.events.redisson.mode | How redisson will connect to redis. Options: `CLUSTER`, `REPLICATED`, `SINGLE`, `SENTINEL` | SINGLE |
| fint.events.redisson.retry-attempts | Error will be thrown if Redis command can't be sended to Redis server after retryAttempts. | 100 |
| fint.events.redisson.retry-interval | Time interval after which another one attempt to send Redis command will be executed. | 5000 |
| fint.events.redisson.reconnection-timeout | Redis server reconnection attempt timeout. | 10000 |
| fint.events.redisson.timeout | Redis server response timeout. Starts to countdown when Redis command was succesfully sent. | 10000 |
| fint.events.redisson.dns-monitoring | If true server address will be monitored for changes in DNS. The monitoring interval is 5 seconds.  | false |
| fint.events.redisson.use-linux-native-epoll | Activates an unix socket if servers binded to loopback interface. Also used for epoll transport activation. | false |
