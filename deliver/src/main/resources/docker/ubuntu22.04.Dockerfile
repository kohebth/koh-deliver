FROM ubuntu:22.04

# Install necessary packages
RUN apt-get update && apt-get install -y \
    openssh-server \
    sudo \
    && rm -rf /var/lib/apt/lists/*

# Create a user
RUN useradd -ms /bin/bash ubuntu \
    && echo 'ubuntu:ubuntu' | chpasswd \
    && adduser ubuntu sudo

# Configure SSH
RUN mkdir /var/run/sshd
RUN echo 'PermitRootLogin yes' >> /etc/ssh/sshd_config

EXPOSE 22

CMD ["/usr/sbin/sshd", "-D"]
