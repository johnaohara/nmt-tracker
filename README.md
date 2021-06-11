# nmt-visualizer project

NativeMemoryTracking visualizer for visually inspecting OpenJDK native memory usage.

This tool allows you to track the native memory allocations in realtime for a Java application that has been started with `-XX:NativeMemoryTracking=[summary|detail]` 


## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

Start the application you wish to trace with `-XX:NativeMemoryTracking=summary` enabled, e.g.

```shell script
java -XX:NativeMemoryTracking=summary -jar some/other/quarkus-app/quarkus-run.jar
```

The nmt-visualizer is now runnable using 
```
java -Dnvm-tracker.process-expr=.*/quarkus-run-jar.*  -jar  target/nmt-tracker-runner.jar `.
``` 

Where `-Dnvm-tracker.process-expr=` is a regex of the running process to track.

Navigate to `http://localhost:8081` to visualize the Native Memory details;

![NMT-visualizer](https://github.com/johnaohara/nmt-tracker/blob/main/nmt-overview.png?raw=true)