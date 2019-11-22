#
# Geckodriver Dockerfile
#

FROM ubuntu:latest

# Install the latest version of Firefox:
RUN export DEBIAN_FRONTEND=noninteractive \
  && apt-get update \
  && apt-get install --no-install-recommends --no-install-suggests -y \
    # Firefox dependencies:
    libgtk-3-0 \
    libdbus-glib-1-2 \
    bzip2 \
    curl \
# RUN DL='https://download.mozilla.org/?product=firefox-latest-ssl&os=linux64'
# RUN curl -sL https://download.mozilla.org/?product=firefox-latest-ssl&os=linux64 | tar -xj -C /opt
# RUN ln -s /opt/firefox/firefox /usr/local/bin/
  && apt-get -y install firefox \
 # Remove obsolete files:
 #   bzip2 \
 && apt-get autoremove --purge -y \
  && apt-get clean \
  && rm -rf \
    /tmp/* \
    /usr/share/doc/* \
    /var/cache/* \
    /var/lib/apt/lists/* \
    /var/tmp/*

# Install OpenJDK-8
RUN apt-get update && \
    apt-get install -y openjdk-8-jdk && \
    apt-get install -y ant && \
    apt-get clean;

# Fix certificate issues
RUN apt-get update && \
    apt-get install ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f;

# Setup JAVA_HOME -- useful for docker commandline
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

# Install the latest version of Geckodriver:
RUN BASE_URL=https://github.com/mozilla/geckodriver/releases/download \
  && VERSION=$(curl -sL \
    https://api.github.com/repos/mozilla/geckodriver/releases/latest | \
    grep tag_name | cut -d '"' -f 4) \
  && curl -sL "$BASE_URL/$VERSION/geckodriver-$VERSION-linux64.tar.gz" | \
    tar -xz -C /usr/local/bin

#USER webdriver

# ENTRYPOINT ["entrypoint", "geckodriver"]

# CMD ["--host", "0.0.0.0"]

EXPOSE 80 8080