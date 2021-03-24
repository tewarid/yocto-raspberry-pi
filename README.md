# Raspberry Pi Linux system based on Yocto Project

You can use this repo to build a Linux system based on the Yocto Project and its [meta-raspberrypi](https://github.com/agherzan/meta-raspberrypi) layer using Docker.

## Build instructions

Clone this repo and run

```bash
docker build .
```

### Pick a different Raspberry Pi

By default, the image is built for Raspberry Pi Zero Wi-Fi. Edit `MACHINE` in [kas-poky-raspberrypi0-wifi.yml](kas-poky-raspberrypi0-wifi.yml) to build for a different [model](meta/meta-raspberrypi/conf/machine/)

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

### Access private Git repos in build

Run ssh-agent on host and add default ~/.ssh/id_rsa key

```bash
ssh-agent
ssh-add
ssh-add -l
```

The last command in the sequence above should list the key you added.

Build with [BuildKit or `docker buildx`](https://github.com/moby/buildkit/blob/master/frontend/dockerfile/docs/syntax.md)

```bash
export DOCKER_BUILDKIT=1
docker build --ssh default=$SSH_AUTH_SOCK .
```

## Incremental build

To perform an incremental build inside a container, use `docker image ls` to find id of image and create a container

```bash
docker run -it image_id
```

Make the necessary changes to source code and rebuild

```bash
kas build kas-poky-raspberrypi0-wifi.yml
```

Note that bitbake may fail with [Invalid cross-device link error](https://bugzilla.yoctoproject.org/show_bug.cgi?id=14301). Follow the link for additional information and a patch.

### Access private Git repos in Docker container

Run Docker container with access to [ssh-agent on host](#access-private-git-repos-in-build)

```bash
docker run -it -v /run/host-services/ssh-auth.sock:/run/host-services/ssh-auth.sock -e SSH_AUTH_SOCK="/run/host-services/ssh-auth.sock" image_id
```

Since we're using a non-root user, you may get an access denied message when you run

```bash
# Access your Git server instead of example.com using ssh
ssh git@example.com
```

If so, you will need to fix access to ssh-agent socket at least once

```bash
docker exec -u 0 -it container_id /bin/bash
chmod 777 /run/host-services/ssh-auth.sock
```

## Download cache

A downloads cache can be setup under `build/downloads`. It will be copied into the image along with the source code. This can reduce build times significantly.

To copy download folder from a container to the host

```bash
docker cp container_id:/home/pi/yocto-raspberry-pi/build/downloads build/
```

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

## Configure Software Access Point

If you want to configure the Raspberry Pi as a software access point (SoftAP/hotspot) and access it via ssh, follow the instructions at [Setting up a Raspberry Pi as a routed wireless access point](https://www.raspberrypi.org/documentation/configuration/wireless/access-point-routed.md).

Notes

1. You're logged in as root so there's no need for sudo

2. nano is not available in the image, so you'll have to use vi to edit files

3. hostapd and dnsmasq are available in the image

4. iptables is not available so you will not be able to route ip packets to another network

5. dhcpcd is not available in the image so you cannot edit `/etc/dhcpcd.conf`

6. You can use the following command to bring up wlan0 interface and assign a static IP address

    ```bash
    ifconfig wlan0 up 192.168.4.1 netmask 255.255.255.0
    ```

7. `hostapd.conf` is available at `/etc/hostapd.conf`

8. You'll need to start hostapd service manually using `systemctl start hostapd`

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

At the end of file `config.txt` on boot partition, add

```conf
enable_uart=1
# Disable Bluetooth for Raspberry Pi 3, 4, and Zero W only
dtoverlay=disable-bt
```
