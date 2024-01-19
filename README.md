# Norbit  
Extremely fast event system.

Norbit is a reimagination of the [orbit event system](https://github.com/MeteorDevelopment/orbit), currently implemented as an addon to the former.

## Usage
```java
package io.github.racoondog.example;

import io.github.racoondog.norbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;

import java.lang.invoke.MethodHandles;

public class Example {
    private static final EventBus norbit = EventBus.threadSafe();

    public static void main(String[] args) {
        // Caller-sensitive, needs to be called from a class within the specified package
        norbit.registerLambdaFactory("io.github.racoondog.example", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));

        // Subscribe a listener
        norbit.susbcribe(Example.ExampleListener.class); // Static listener
        norbit.subscribe(Example.ExampleListener.INTANCE); // Instance listener

        // Post an event
        norbit.post(new Example.ExampleEvent()); // Posted event can be any non-primitive object;
    }

    private static class ExampleListener {
        public static final ExampleListener INSTANCE = new ExampleListener();

        @EventHandler(priority = EventPriority.LOW)
        private static void onEvent_staticListener(Example.ExampleEvent event) { // Static listener
            System.out.println("Found event!");
        }

        @EventHandler(priority = EventPriority.HIGH)
        private void onEvent_instanceListener(Example.ExampleEvent event) { // Instance listener
            System.out.println("Found event, but before the other one!");
        }
    }

    private static class ExampleEvent {}
}
```

```groovy
repositories {
    maven {
        name = "meteor-maven"
        url = "https://maven.meteordev.org/releases"
    }
}

dependencies {
    implementation "meteordevelopment:orbit:0.2.4"
    implementation "io.github.racoondog:norbit:1.1.0"
}
```