# Raspberry Pi Linux system based on Yocto Project

You can use this repo to build a Linux system based on the Yocto Project and its [meta-raspberrypi](https://github.com/agherzan/meta-raspberrypi) layer using Docker.

Clone this repo and run

```bash
docker build .
```

By default, the image is built for Raspberry Pi Zero Wi-Fi. Edit `MACHINE` in [local.conf](build/conf/local.conf) to build for a different [model](meta/meta-raspberrypi/conf/machine/)

MACHINE           | Model
----------------- | -----------------------------
raspberrypi-cm    | Raspberry Pi Compute Module
raspberrypi-cm3   | Raspberry Pi 3 Compute Module
raspberrypi       | Raspberry Pi Model B+
raspberrypi0-wifi | Raspberry Pi Zero with Wi-Fi
raspberrypi0      | Raspberry Pi Zero
raspberrypi2      | Raspberry Pi 2
raspberrypi3-64   | Raspberry Pi 3 64-bit build
raspberrypi3      | Raspberry Pi 3 32-bit build
raspberrypi4-64   | Raspberry Pi 4 64-bit build
raspberrypi4      | Raspberry Pi 4 32-bit build

## Evaluate build output

To evaluate build output, use `docker image ls` to find image id, then create a container using `docker run -it image_id`.

To copy image files from the container to host, use `docker ps` to find container id, then use [docker cp](https://docs.docker.com/engine/reference/commandline/cp/) e.g.

```bash
docker cp container_id:/home/pi/yocto-raspberry-pi/build/tmp/deploy/images/raspberrypi/core-image-base-raspberrypi-20210226153757.rootfs.wic.bmap ./build/tmp/deploy/images/raspberrypi/
```

## Write image to SD card

To write image to a SD card, use bmaptool.

On Ubuntu, to install bmaptool

```bash
sudo apt install bmap-tools
```

Use `lsblk` to find SD card device, and

```bash
sudo bmaptool copy --bmap build/tmp/deploy/images/raspberrypi/core-image-base-raspberrypi-20210226153757.rootfs.wic.bmap build/tmp/deploy/images/raspberrypi/core-image-base-raspberrypi-20210226153757.rootfs.wic.bz2 /dev/sda
```

On macOS, to install bmaptool

```bash
git clone https://github.com/intel/bmap-tools.git
cd bmap-tools
python3 setup.py install
pip3 install six
```

Find SD card device using `diskutil list`, then unmount disk and copy image to SD card

```bash
diskutil unmountDisk /dev/disk2
sudo bmaptool copy --bmap build/tmp/deploy/images/raspberrypi/core-image-base-raspberrypi-20210226153757.rootfs.wic.bmap build/tmp/deploy/images/raspberrypi/core-image-base-raspberrypi-20210226153757.rootfs.wic.bz2 /dev/rdisk2
```

Note the `r` in device path i.e. `/dev/rdisk2` in the example above.

## Configure Wi-Fi

Use `wpa_passphrase` utility to print out network configuration

```bash
wpa_passphrase ssid password
```

Copy the output and add, all but the commented out plain text password line, to end of `/etc/wpa_supplicant.conf`.

Bring up the Wi-Fi network

```bash
ifup wlan0
```

## Serial console

To enable [serial console through expansion headers](https://www.raspberrypi.org/documentation/configuration/uart.md), add `console=ttyAMA0,115200` to kernel command line in file `cmdline.txt` on boot partition

```text
dwc_otg.lpm_enable=0 root=/dev/mmcblk0p2 rootfstype=ext4 console=ttyAMA0,115200 console=tty1 rootwait
```

To get a login prompt, edit `/etc/inittab` to add

```text
AMA0:12345:respawn:/sbin/getty -L 115200 ttyAMA0
```

For Raspberry Pi 3, 4, and Zero Wi-Fi, at the end of file `config.txt` on boot partition, add

```text
dtoverlay=disable-bt
```
