#syntax=docker/dockerfile:1.2

FROM ubuntu:20.04

RUN apt update \
    && DEBIAN_FRONTEND=noninteractive apt install -y gawk wget git-core diffstat unzip texinfo gcc-multilib build-essential chrpath socat cpio python3 python3-pip python3-pexpect xz-utils debianutils iputils-ping python3-git python3-jinja2 libegl1-mesa libsdl1.2-dev pylint3 xterm python3-subunit mesa-common-dev libpseudo locales vim \
    && locale-gen "en_US.UTF-8" \
    && adduser --gecos "" --disabled-password  pi

USER pi

COPY --chown=pi:pi . /home/pi/docker-meta-raspberrypi

WORKDIR /home/pi/docker-meta-raspberrypi

SHELL ["/bin/bash", "-c"] 

RUN --mount=type=ssh,mode=777 git submodule update --init --recursive \
    && source layers/poky/oe-init-build-env \
    && bitbake core-image-base

# At this point you can copy files to host to cache/deploy/stage
