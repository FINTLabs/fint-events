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