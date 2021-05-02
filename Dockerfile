#syntax=docker/dockerfile:1.2

FROM crops/yocto:ubuntu-20.04-base

USER root

RUN DEBIAN_FRONTEND=noninteractive apt install -y python3-pip vim \
    && pip3 install kas

USER yoctouser

COPY --chown=yoctouser:yoctouser . /home/yoctouser/berry

WORKDIR /home/yoctouser/berry

SHELL ["/bin/bash", "-c"] 

RUN --mount=type=ssh,mode=777 kas build kas-poky-raspberrypi0-wifi.yml

# At this point you can copy files to host if needed
