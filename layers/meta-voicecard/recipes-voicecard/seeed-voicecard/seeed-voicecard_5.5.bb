SUMMARY = "seeed-voicecard module"
DESCRIPTION = "${SUMMARY}"
LICENSE = "GPLv3"

inherit module

SRC_URI = "git://github.com/respeaker/seeed-voicecard.git;branch=master \
file://0001-patch-for-kernel-version-5.10.25.patch"
SRCREV = "rel-v5.5"

S = "${WORKDIR}/git"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=84dcc94da3adb52b53ae4fa38fe49e5d"

# The inherit of module.bbclass will automatically name module packages with
# "kernel-module-" prefix as required by the oe-core build environment.

RPROVIDES_${PN} += "kernel-module-seeed-voicecard"
