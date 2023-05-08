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
ARG PROJECT
ARG PROJECTSRC=${PROJECT}
ARG PROJECTDIR=/opt/chess/${PROJECT}
ARG PROJECT_API_HOST=0.0.0.0
ARG PROJECT_API_PORT=8080


#======================================================================================#
# PROJECT JAR ASSEMBLER
#======================================================================================#
FROM sbtscala/scala-sbt:${JDK_BASE}-${JAVA_VERSION}_${SBT_VERSION}_${SCALA_VERSION} as prebuilder

ARG PROJECT
ARG PROJECTSRC
ARG PROJECTDIR
WORKDIR ${PROJECTDIR}

ENV PROJECT=${PROJECT}

# Copy project definition files
COPY project/dependencies.scala project/
COPY project/build.properties project/ 
COPY project/plugins.sbt project/
# Copy sbt build file
COPY ${PROJECTSRC}/docker/builder.build.sbt build.sbt
# Copy dependencies
COPY utils/src/main utils/src/main
COPY legality/src/main legality/src/main
COPY persistence/src/main persistence/src/main
COPY controller/src/main controller/src/main
COPY ui/src/main ui/src/main
# Copy module source code
COPY ${PROJECTSRC}/src/main src/main

RUN sbt assembly


#======================================================================================#
# PROJECT RUNNER BUILDER
#======================================================================================#
FROM sbtscala/scala-sbt:${JDK_BASE}-${JAVA_VERSION}_${SBT_VERSION}_${SCALA_VERSION} as builder

ARG PROJECT
ARG PROJECTSRC
ARG PROJECTDIR
WORKDIR ${PROJECTDIR}

ENV PROJECT=${PROJECT}

# Copy project definition files
COPY project/dependencies.scala project/
COPY project/build.properties project/ 
COPY project/plugins.sbt project/
# Copy module runner code
COPY docker/runner.build.sbt build.sbt
COPY ${PROJECTSRC}/docker/src src/

COPY --link --from=prebuilder ${PROJECTDIR}/target/scala-*/${PROJECT}-*.jar lib/

# Build the project
RUN sbt assembly


#======================================================================================#
# RUNNER
#======================================================================================#
FROM eclipse-temurin:17.0.7_7-jre-alpine as runner

ARG PROJECT
ARG PROJECTDIR
ARG SCALA_VERSION
ARG PROJECT_API_HOST
ARG PROJECT_API_PORT
WORKDIR ${PROJECTDIR}

ENV PROJECT=${PROJECT}
ENV PROJECTDIR=${PROJECTDIR}
ENV SCALA_VERSION=${SCALA_VERSION}

ENV PROJECT_API_HOST=${PROJECT_API_HOST}
ENV PROJECT_API_PORT=${PROJECT_API_PORT}

EXPOSE ${PROJECT_API_PORT}

COPY --link --from=builder ${PROJECTDIR}/target/scala-*/${PROJECT}-*.jar lib/

ENTRYPOINT exec /opt/java/openjdk/bin/java -Duser.dir=${PROJECTDIR} -classpath ${PROJECTDIR}/lib/${PROJECT}-*.jar de.htwg.se.chess.Main
