# Norbit  
Extremely fast event system.

old readme was too arrogant, i will rewrite it at some point

## Usage
```java
package io.github.racoondog.example;

public class Example {
    private static final EventBus norbit = new EventBus();

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
  modImplementation "io.github.racoondog:norbit:1.0.1"
  include "io.github.racoondog:norbit:1.0.1"
}
```

## Orbit Comparison

### ConsumerListener Post

| Benchmark            | Mode | Cnt |   Score |   Error | Units | Rel Error |
|----------------------|------|-----|--------:|--------:|-------|----------:|
| Norbit Thread-unsafe | avgt |   4 | 529.552 | 184.746 | μs/op |   34.887% |
| Orbit                | avgt |   4 | 550.835 |  21.321 | μs/op |    3.870% |
| Norbit Thread-safe   | avgt |   4 | 632.368 |  40.507 | μs/op |    6.405% |

### ConsumerListener Subscribe

| Benchmark            | Mode | Cnt |    Score |   Error | Units | Rel Error |
|----------------------|------|-----|---------:|--------:|-------|----------:|
| Norbit Thread-unsafe | avgt |   4 | 1199.531 | 370.193 | μs/op |   30.861% |
| Orbit                | avgt |   4 | 2827.619 | 838.037 | μs/op |   29.637% |
| Norbit Thread-safe   | avgt |   4 | 3092.797 | 187.268 | μs/op |    6.054% |

### Instance LambdaListener Post

| Benchmark            | Mode | Cnt |   Score |   Error | Units | Rel Error |
|----------------------|------|-----|--------:|--------:|-------|----------:|
| Norbit Thread-safe   | avgt |   4 | 568.650 | 108.242 | μs/op |   19.035% |
| Norbit Thread-unsafe | avgt |   4 | 576.265 |  16.412 | μs/op |    2.848% |
| Orbit                | avgt |   4 | 597.316 |  35.095 | μs/op |    5.875% |

### Instance Multiple Identical LambdaListeners Post

| Benchmark            | Mode | Cnt |     Score |    Error | Units | Rel Error |
|----------------------|------|-----|----------:|---------:|-------|----------:|
| Norbit Thread-safe   | avgt |   4 |  4399.440 | 1011.008 | μs/op |   22.980% |
| Norbit Thread-unsafe | avgt |   4 |  4709.727 |  456.105 | μs/op |    9.684% |
| Orbit                | avgt |   4 | 11013.026 | 1572.970 | μs/op |   14.282% |

### Instance LambdaListener First Subscribe

| Benchmark            | Mode | Cnt |  Score |  Error | Units | Rel Error |
|----------------------|------|-----|-------:|-------:|-------|----------:|
| Norbit Thread-safe   | avgt |   4 | 52.841 |  8.581 | μs/op |   16.240% |
| Orbit                | avgt |   4 | 55.650 | 31.878 | μs/op |   57.283% |
| Norbit Thread-unsafe | avgt |   4 | 57.546 | 31.493 | μs/op |   54.727% |

### Instance LambdaListener Subsequent Subscribe

| Benchmark            | Mode | Cnt |     Score |     Error | Units | Rel Error |
|----------------------|------|-----|----------:|----------:|-------|----------:|
| Norbit Thread-unsafe | avgt |   4 |    36.097 |     1.535 | ms/op |    4.252% |
| Norbit Thread-safe   | avgt |   4 |    39.998 |    11.054 | ms/op |   27.637% |
| Orbit                | avgt |   4 | 80910.109 | 11462.117 | ms/op |   14.166% |

Note: Orbit performance drop is caused by a memory leak.

### Instance LambdaListener Singleton Subscribe

| Benchmark            | Mode | Cnt |     Score |    Error | Units | Rel Error |
|----------------------|------|-----|----------:|---------:|-------|----------:|
| Orbit                | avgt |   4 |  6990.913 |  627.332 | μs/op |    8.973% |
| Norbit Thread-unsafe | avgt |   4 | 41332.697 | 4210.992 | μs/op |   10.188% |
| Norbit Thread-safe   | avgt |   4 | 44654.804 | 3434.635 | μs/op |    7.691% |

Note: Performance regression is a result of patching the memory leak.

### Static LambdaListener Post

| Benchmark            | Mode | Cnt |   Score |   Error | Units | Rel Error |
|----------------------|------|-----|--------:|--------:|-------|----------:|
| Norbit Thread-unsafe | avgt |   4 | 535.835 |  87.283 | μs/op |   16.289% |
| Norbit Thread-safe   | avgt |   4 | 555.369 |  82.179 | μs/op |   14.797% |
| Orbit                | avgt |   4 | 582.571 | 250.384 | μs/op |   42.979% |

### Static LambdaListener First Subscribe

| Benchmark            | Mode | Cnt |  Score |  Error | Units | Rel Error |
|----------------------|------|-----|-------:|-------:|-------|----------:|
| Orbit                | avgt |   4 | 34.078 | 25.499 | μs/op |   74.826% |
| Norbit Thread-unsafe | avgt |   4 | 38.879 | 42.557 | μs/op |  109.457% |
| Norbit Thread-safe   | avgt |   4 | 38.936 | 40.767 | μs/op |  104.702% |

### Static LambdaListener Subsequent Subscribe

| Benchmark            | Mode | Cnt |    Score |   Error | Units | Rel Error |
|----------------------|------|-----|---------:|--------:|-------|----------:|
| Norbit Thread-unsafe | avgt |   4 | 2581.725 | 111.318 | μs/op |    4.311% |
| Orbit                | avgt |   4 | 4817.068 | 848.684 | μs/op |   17.618% |
| Norbit Thread-safe   | avgt |   4 | 5030.811 | 466.584 | μs/op |    9.274% |

### Priority Sorting

| Benchmark            | Mode | Cnt |   Score |  Error | Units | Rel Error |
|----------------------|------|-----|--------:|-------:|-------|----------:|
| Norbit Thread-unsafe | avgt |   4 | 475.295 | 19.085 | ns/op |    4.015% |
| Orbit                | avgt |   4 | 560.947 | 38.682 | ns/op |    6.895% |
| Norbit Thread-safe   | avgt |   4 | 614.964 | 52.837 | ns/op |    8.591% |

### Method Reflection

| Benchmark            | Mode | Cnt |   Score |   Error | Units | Rel Error |
|----------------------|------|-----|--------:|--------:|-------|----------:|
| Orbit                | avgt |   4 | 269.620 | 117.942 | μs/op |   43.743% |
| Norbit Thread-safe   | avgt |   4 | 272.729 | 147.855 | μs/op |   54.213% |
| Norbit Thread-unsafe | avgt |   4 | 278.266 | 177.843 | μs/op |   63.910% |