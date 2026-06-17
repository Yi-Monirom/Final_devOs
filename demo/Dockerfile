FROM eclipse-temurin:25-jdk

RUN apt update && apt install -y \
    nginx \
    openssh-server \
    php-cli \
    mysql-client \
    git \
    curl \
    maven \
    ansible \
    && rm -rf /var/lib/apt/lists/*


RUN mkdir -p /run/sshd && \
    echo 'root:root@123' | chpasswd && \
    sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config && \
    sed -i 's/#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config


EXPOSE 80
EXPOSE 22


CMD service ssh start && \
    service nginx start && \
    tail -f /dev/null