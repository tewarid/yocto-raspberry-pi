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

A downloads cache can be setup under `build/downloads`. It will be copied into the image along with the source code. This can reduce build times by about 50%.

## Incremental build

To perform an incremental build inside a container, use `docker image ls` to find id of image and create a container

```bash
docker run -it image_id
```

Make the necessary changes to source code, and

```bash
source layers/poky/oe-init-build-env
bitbake core-image-base
```

Note that bitbake may fail with [Invalid cross-device link error](https://bugzilla.yoctoproject.org/show_bug.cgi?id=14301). Follow the link for additional information and a patch.

## Write image to SD card

If you haven't yet created a container from the image you built using `docker build`, see [Incremental build](#incremental-build)

To copy image files from the Docker container to host, use `docker ps` to find container id, then use [docker cp](https://docs.docker.com/engine/reference/commandline/cp/) e.g.

```bash
docker cp container_id:/home/pi/yocto-raspberry-pi/build/tmp/deploy/images/raspberrypi/core-image-base-raspberrypi-20210226153757.rootfs.wic.bmap ./build/tmp/deploy/images/raspberrypi/
docker cp container_id:/home/pi/yocto-raspberry-pi/build/tmp/deploy/images/raspberrypi/core-image-base-raspberrypi-20210226153757.rootfs.wic.bz2 ./build/tmp/deploy/images/raspberrypi/
```

To write image to a SD card, use bmaptool.

To install bmaptool on Ubuntu

```bash
sudo apt install bmap-tools
```

Use `lsblk` to find SD card device, and

```bash
sudo bmaptool copy --bmap build/tmp/deploy/images/raspberrypi/core-image-base-raspberrypi-20210226153757.rootfs.wic.bmap build/tmp/deploy/images/raspberrypi/core-image-base-raspberrypi-20210226153757.rootfs.wic.bz2 /dev/sdb
```

To install bmaptool on macOS

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

## Configure Bluetooth

Bring up interface and make device discoverable

```bash
hciconfig hci0 up
hciconfig hci0 piscan
```

DBUS can also used to bring up interface programmatically

```bash
dbus-send --system --print-reply --dest=org.bluez /org/bluez/hci0 org.freedesktop.DBus.Properties.Set string:"org.bluez.Adapter1" string:"Powered" variant:boolean:true
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
