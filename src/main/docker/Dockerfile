FROM debian:stable-slim
LABEL maintainer="steve.springett@owasp.org"
LABEL vendor="OWASP"

# Arguments that can be passed at build time
# Directory names must end with / to avoid errors when ADDing and COPYing
ARG APP_DIR=/opt/owasp/dependency-track/
ARG DATA_DIR=/data/
ARG USERNAME=dtrack
ARG WAR_FILENAME=dependency-track-apiserver.jar

# Create the directory where Dependency-Track will store its data (${DATA_DIR}) and the external library directory (${EXTLIB_DIR})
# Create a user and assign home directory to a ${DATA_DIR}
# Ensure UID 1000 & GID 1000 own all the needed directories
RUN mkdir -p -m 770 ${DATA_DIR} \
    && useradd --home-dir ${DATA_DIR} --uid 1000 ${USERNAME} \
    && chown -R ${USERNAME}:${USERNAME} ${DATA_DIR} \
    # install dependencies necessary in order to install Azul OpenJDK
    && apt-get -q update && apt-get -y upgrade \
    && apt-get -yq install gnupg curl \
    # add Azul's public key
    && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 0xB1998361219BD9C9 \
    # download and install the package that adds
    # the Azul APT repository to the list of sources
    && curl -O https://cdn.azul.com/zulu/bin/zulu-repo_1.0.0-3_all.deb \
    # install the package
    && apt-get install ./zulu-repo_1.0.0-3_all.deb \
    # update the package sources
    && apt-get update \
    # install Azul Zulu JRE
    && apt-get -y install zulu17-ca-jre-headless

# Copy the compiled WAR to the application directory created above
# Automatically creates the $APP_DIR directory
COPY --chown=1000 ./target/${WAR_FILENAME} ${APP_DIR}

# Specify the user to run as (in numeric format for compatibility with Kubernetes/OpenShift's SCC)
USER 1000

# Specify the container working directory
WORKDIR ${APP_DIR}

# Launch Dependency-Track
CMD java $JAVA_OPTIONS -DdependencyTrack.logging.level=$LOGGING_LEVEL -jar ${WAR_FILENAME} -context ${CONTEXT}

# Specify which port Dependency-Track listens on
EXPOSE 8080

# Dependency-Track's default logging level
ENV LOGGING_LEVEL=INFO

# Environment variables that can be passed at runtime
ENV JAVA_OPTIONS="-XX:+UseParallelGC -XX:MaxRAMPercentage=90.0"

# The web context defaults to the root. To override, supply an alternative context which starts with a / but does not end with one
# Example: /dtrack
ENV CONTEXT=""

# Injects the build-time ARG "WAR_FILENAME" as an environment variable that can be used in the CMD.
ENV WAR_FILENAME ${WAR_FILENAME}

# Add a healthcheck using the Dependency-Track version API
HEALTHCHECK --interval=5m --timeout=3s CMD wget --proxy off -q -O /dev/null http://127.0.0.1:8080${CONTEXT}/api/version || exit 1
