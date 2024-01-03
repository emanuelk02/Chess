ARG SCALA_VERSION=3.2.2
ARG SBT_VERSION=1.8.2
ARG JAVA_VERSION=17.0.5_8
ARG JDK_BASE=eclipse-temurin
ARG SERVICE=chess
ARG SERVICEDIR=/opt/chess/${SERVICE}
ARG SERVICE_API_HOST=0.0.0.0
ARG SERVICE_API_PORTS=8081-8083


#======================================================================================#
# SERVICE JAR ASSEMBLER
#======================================================================================#
FROM sbtscala/scala-sbt:${JDK_BASE}-focal-${JAVA_VERSION}_${SBT_VERSION}_${SCALA_VERSION} as builder

ARG SERVICE
ARG SERVICEDIR
WORKDIR ${SERVICEDIR}

ENV SERVICE=${SERVICE}

# Copy project definition files
COPY project/dependencies.scala project/build.properties project/plugins.sbt project/
# Copy service source code dependencies
COPY ./ ./

RUN rm -rf docker \
 && rm -rf ./src \
 && mkdir chess

COPY docker/runner.build.sbt build.sbt
COPY docker/runners/main/* src/main/scala/

# Build the project
RUN sbt assembly


#======================================================================================#
# RUNNER
#======================================================================================#
FROM ${JDK_BASE}:17.0.7_7-jre-alpine as runner

ARG SERVICE
ARG SERVICEDIR
ARG SCALA_VERSION
ARG SERVICE_API_HOST
ARG SERVICE_API_PORTS
WORKDIR ${SERVICEDIR}

ENV SERVICE=${SERVICE}
ENV SERVICEDIR=${SERVICEDIR}
ENV SCALA_VERSION=${SCALA_VERSION}

ENV SERVICE_API_HOST=${SERVICE_API_HOST}

EXPOSE ${SERVICE_API_PORTS}

COPY --link --from=builder ${SERVICEDIR}/target/scala-*/${SERVICE}-*.jar lib/
RUN apk update \
 && apk add --clean-protected curl

ENTRYPOINT exec /opt/java/openjdk/bin/java -Duser.dir=${SERVICEDIR} -classpath ${SERVICEDIR}/lib/${SERVICE}-*.jar de.htwg.se.chess.Main