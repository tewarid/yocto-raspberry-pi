SUMMARY = "HomeKitADK recipe"
DESCRIPTION = "HomeKitADK recipe created by bitbake-layers"
LICENSE = "Apache-2.0"

SRC_URI = "git://github.com/apple/HomeKitADK.git;branch=master \
file://0001-build-with-yocto-meta-raspberry-pi.patch"
SRCREV = "4967f698bdcf0af122e13e986a2c9b595a68cdc5"

PACKAGES = "homekitadk homekitadk-dbg"

S = "${WORKDIR}/git"

LIC_FILES_CHKSUM = "file://${S}/LICENSE.md;md5=3b83ef96387f14655fc854ddc3c6bd57"

DEPENDS = "openssl coreutils-native"

# Use avahi for MDNS
# Add PACKAGECONFIG_pn-avahi += "libdns_sd" to local or distro conf
DEPENDS += "avahi"
RDEPENDS_${PN} = "libavahi-compat-libdnssd avahi-daemon"
EXTRA_OEMAKE = "'CC=${CC} -I${STAGING_INCDIR}/avahi-compat-libdns_sd'"

HOMEKITADK_BUILD_TYPE ?= "Release"

lcl_maybe_fortify = "${@oe.utils.conditional('HOMEKITADK_BUILD_TYPE','Debug','','-D_FORTIFY_SOURCE=2',d)}"

EXTRA_OEMAKE += "TARGET=Linux DOCKER=0 BUILD_TYPE=${HOMEKITADK_BUILD_TYPE} apps tools"

do_install() {
    install -m 0755 -d ${D}${bindir}
    install -m 0755 ${B}/Output/Linux-arm-${DISTRO}-${TARGET_OS}/${HOMEKITADK_BUILD_TYPE}/IP/Applications/Lightbulb.OpenSSL ${D}${bindir}
    install -m 0755 ${B}/Output/Linux-arm-${DISTRO}-${TARGET_OS}/${HOMEKITADK_BUILD_TYPE}/IP/Applications/Lock.OpenSSL ${D}${bindir}
    install -m 0755 ${B}/Output/Linux-arm-${DISTRO}-${TARGET_OS}/${HOMEKITADK_BUILD_TYPE}/Tools/AccessorySetupGenerator.OpenSSL ${D}${bindir}
}

FILES_${PN} += "${bindir}/*"
FILES_${PN}-dbg += "${bindir}/.debug/*"
