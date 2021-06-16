# nmt-visualizer project

NativeMemoryTracking visualizer for visually inspecting OpenJDK native memory usage for java applications running in containers

This tool allows you to track the native memory allocations in realtime for a Java application that has been started with `-XX:NativeMemoryTracking=[summary|detail]` 


## Quickstart

Create a docker image for your application that has Native Memory Tracking enabled, e.g.;

```dockerfile
FROM registry.access.redhat.com/ubi8/openjdk-11:1.3-15

COPY --chown=1001 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=1001 target/quarkus-app/*.jar /deployments/
COPY --chown=1001 target/quarkus-app/app/ /deployments/app/
COPY --chown=1001 target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8081
USER 1001

CMD [ "java", "-XX:NativeMemoryTracking=summary", "-Dquarkus.http.port=8082", "-Xmx128m", "-jar", "/deployments/quarkus-run.jar" ]

```

Start NMT Tracker

```shell
$ docker run -it --rm --name nmt-tracker -p 8090:8081 -v /var/run/docker.sock:/var/run/docker.sock  quay.io/johara/nmt-tracker
```

Navigate to `http://localhost:8090` to visualize the Native Memory details;

![NMT-visualizer](https://github.com/johnaohara/nmt-tracker/blob/main/nmt-overview.png?raw=true)



## Building from source

The application can be packaged using:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The nmt-visualizer is now runnable using
```
java -jar  target/nmt-tracker-runner.jar `.
``` 
