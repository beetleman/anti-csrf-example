# Build app
FROM clojure:tools-deps AS app-build
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN clojure -T:build uber

# Create a Java runtime
FROM eclipse-temurin:21 as jre-build
RUN $JAVA_HOME/bin/jlink \
    --add-modules java.base,java.logging,java.sql \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --output /javaruntime

# Define base image
FROM fedora:41
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /javaruntime $JAVA_HOME

RUN mkdir /opt/app
COPY --from=app-build /usr/src/app/target/beetleman/anti-csrf.jar /opt/app/anti-csrf.jar
CMD ["java", "-jar", "/opt/app/anti-csrf.jar"]
