FROM eclipse-temurin:25-jdk

RUN apt update && apt install -y \
    nginx \
    openssh-server \
    php-cli \
    mysql-client \
    git \
    curl


RUN mkdir -p /run/sshd


EXPOSE 80
EXPOSE 22


CMD service ssh start && \
    service nginx start && \
    tail -f /dev/null