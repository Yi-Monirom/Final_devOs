FROM ubuntu:latest

ENV DEBIAN_FRONTEND=noninteractive

USER root

RUN apt-get update && apt-get install -y \
    nginx \
    openssh-server \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir /var/run/sshd && \
    echo 'root:root' | chpasswd && \
    sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config && \
    sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config

RUN mkdir -p /var/www/html/app

EXPOSE 80 22

CMD service ssh start && nginx start && /usr/sbin/sshd -D