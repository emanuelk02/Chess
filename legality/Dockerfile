#======================================================================================#
#     _________  ______________________                                                #
#    /  ___/  / /  /  ____/  ___/  ___/        2023 Emanuel Kupke & Marcel Biselli     #
#   /  /  /  /_/  /  /__  \  \  \  \           https://github.com/emanuelk02/Chess     #
#  /  /__/  __   /  /___ __\  \__\  \                                                  #
#  \    /__/ /__/______/______/\    /         Software Engineering | HTWG Constance    #
#   \__/                        \__/                                                   #
#                                                                                      #
#                                                                                      #
# LEGALITY                                                                             #
#======================================================================================#
ARG SCALA_VERSION=3.2.2
ARG SBT_VERSION=1.8.2
ARG JAVA_VERSION=17.0.5_8
ARG JDK_BASE=eclipse-temurin-focal
ARG PROJECTDIR=/opt/chess/legality


#======================================================================================#
# PROJECT JAR ASSEMBLER
#======================================================================================#
FROM sbtscala/scala-sbt:${JDK_BASE}-${JAVA_VERSION}_${SBT_VERSION}_${SCALA_VERSION} as prebuilder

ARG PROJECTDIR
WORKDIR ${PROJECTDIR}

# Copy module source code
COPY legality/src/main src/main
# Copy utils dependency
COPY utils/src/main utils/src/main
# Copy project definition files
COPY project/dependencies.scala project/
COPY project/build.properties project/ 
COPY project/plugins.sbt project/
# Copy sbt build file
COPY legality/docker/builder.build.sbt build.sbt

RUN sbt assembly


#======================================================================================#
# PROJECT RUNNER BUILDER
#======================================================================================#
FROM sbtscala/scala-sbt:${JDK_BASE}-${JAVA_VERSION}_${SBT_VERSION}_${SCALA_VERSION} as builder

ARG PROJECTDIR
WORKDIR ${PROJECTDIR}

COPY --from=prebuilder $PROJECTDIR/target/scala-*/legality-*.jar lib/

# Copy module runner code
COPY legality/docker/src src/
COPY legality/docker/runner.build.sbt build.sbt
# Copy project definition files
COPY project/dependencies.scala project/
COPY project/build.properties project/ 
COPY project/plugins.sbt project/

# Build the project
RUN sbt assembly


#======================================================================================#
# RUNNER
#======================================================================================#
FROM eclipse-temurin:17.0.7_7-jre-alpine as runner

ARG PROJECTDIR
ARG SCALA_VERSION
WORKDIR ${PROJECTDIR}

COPY --from=builder $PROJECTDIR/target/scala-*/legality-*.jar lib/
COPY legality/docker/entrypoint.sh entrypoint.sh

ENV PROJECTDIR=${PROJECTDIR}
ENV SCALA_VERSION=${SCALA_VERSION}

ENV LEGALITY_API_HOST=0.0.0.0
ENV LEGALITY_API_PORT=8082

EXPOSE ${LEGALITY_API_PORT}

RUN chmod a+x entrypoint.sh

ENTRYPOINT exec /opt/java/openjdk/bin/java -Duser.dir=${PROJECTDIR} -classpath ${PROJECTDIR}/lib/legality-*.jar de.htwg.se.chess.Legality
