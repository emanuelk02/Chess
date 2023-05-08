#======================================================================================#
#     _________  ______________________                                                #
#    /  ___/  / /  /  ____/  ___/  ___/        2023 Emanuel Kupke & Marcel Biselli     #
#   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     #
#  /  /__/  __   /  /___ __\  \__\  \                                                  #
#  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    #
#   \__/                        \__/                                                   #
#                                                                                      #
#                                                                                      #
# CHESS SERVICES                                                                       #
#======================================================================================#
ARG SCALA_VERSION=3.2.2
ARG SBT_VERSION=1.8.2
ARG JAVA_VERSION=17.0.5_8
ARG JDK_BASE=eclipse-temurin-focal
# This needs to be set to the name of the service, e.g. controller, persistence, etc.
# make sure it matches the name of the folder for the service
ARG SERVICE
# This needs to be set to the services the built service depends on, e.g. utils, persistence, etc.
ARG SERVICE_DEPENDENCIES=utils
ARG SERVICESRC=${SERVICE}
ARG SERVICEDEST=${SERVICE}
ARG SERVICEDIR=/opt/chess/${SERVICE}
ARG SERVICE_API_HOST=0.0.0.0
ARG SERVICE_API_PORT=8080


#======================================================================================#
# SERVICE JAR ASSEMBLER
#======================================================================================#
FROM sbtscala/scala-sbt:${JDK_BASE}-${JAVA_VERSION}_${SBT_VERSION}_${SCALA_VERSION} as prebuilder

ARG SERVICE
ARG SERVICESRC
ARG SERVICEDEST
ARG SERVICEDIR
ARG SERVICE_DEPENDENCIES
WORKDIR ${SERVICEDIR}

ENV SERVICE=${SERVICE}
ENV SERVICE_DEPENDENCIES=${SERVICE_DEPENDENCIES}

# Copy project definition files
COPY --link project/dependencies.scala project/build.properties project/plugins.sbt project/
#COPY project/dependencies.scala project/build.properties project/plugins.sbt service/project/

# Copy sbt build file
#COPY ${SERVICESRC}/docker/builder.build.sbt build.sbt
#COPY ${SERVICESRC}/docker/builder.build.sbt ${SERVICEDEST}/build.sbt
# Copy dependencies
#COPY utils/src/main utils/src/main
#COPY legality/src/main legality/src/main
#COPY persistence/src/main persistence/src/main
#COPY controller/src/main controller/src/main
#COPY ui/src/main ui/src/main
COPY --link ./ ./

RUN : \
    && rm -rf docker \
    && mkdir chess \
    && mv ./src chess/ \
    && :

# Copy module source code
#COPY ${SERVICESRC}/src/main src/main
#COPY ${SERVICESRC}/src/main ${SERVICEDEST}/src/main


#======================================================================================#
# SERVICE RUNNER BUILDER
#======================================================================================#
#FROM sbtscala/scala-sbt:${JDK_BASE}-${JAVA_VERSION}_${SBT_VERSION}_${SCALA_VERSION} as builder
#
#ARG SERVICE
#ARG SERVICESRC
#ARG SERVICEDIR
#WORKDIR ${SERVICEDIR}
#
#ENV SERVICE=${SERVICE}

# Copy project definition files
#COPY project/dependencies.scala project/
#COPY project/build.properties project/ 
#COPY project/plugins.sbt project/
# Copy module runner code
COPY --link docker/runner.build.sbt build.sbt
#COPY ${SERVICESRC}/docker/src src/
COPY --link docker/runners/${SERVICE}/* src/main/scala/

#COPY --link --from=prebuilder ${SERVICEDIR}/target/scala-*/${SERVICE}-*.jar lib/

# Build the project
#RUN sbt assembly

RUN sbt assembly


#======================================================================================#
# RUNNER
#======================================================================================#
FROM eclipse-temurin:17.0.7_7-jre-alpine as runner

ARG SERVICE
ARG SERVICEDIR
ARG SCALA_VERSION
ARG SERVICE_API_HOST
ARG SERVICE_API_PORT
WORKDIR ${SERVICEDIR}

ENV SERVICE=${SERVICE}
ENV SERVICEDIR=${SERVICEDIR}
ENV SCALA_VERSION=${SCALA_VERSION}

ENV SERVICE_API_HOST=${SERVICE_API_HOST}
ENV SERVICE_API_PORT=${SERVICE_API_PORT}

EXPOSE ${SERVICE_API_PORT}

COPY --link --from=prebuilder ${SERVICEDIR}/target/scala-*/${SERVICE}-*.jar lib/

ENTRYPOINT exec /opt/java/openjdk/bin/java -Duser.dir=${SERVICEDIR} -classpath ${SERVICEDIR}/lib/${SERVICE}-*.jar de.htwg.se.chess.Main
