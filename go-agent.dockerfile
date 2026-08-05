FROM jenkins/inbound-agent:latest-jdk21

USER root

RUN apt-get update && apt-get full-upgrade -y \
    && apt-get install curl -y \
    && curl -L -O 'https://go.dev/dl/go1.26.5.linux-amd64.tar.gz'\
    && tar -C /usr/local -xzf go1.26.5.linux-amd64.tar.gz \
    && echo 'PATH=\$PATH:/usr/local/go/bin' >> /etc/environment

USER jenkins