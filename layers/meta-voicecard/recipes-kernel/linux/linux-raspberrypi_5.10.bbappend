SRC_URI += "file://seeed-2mic-voicecard-overlay.dts;subdir=git/arch/${ARCH}/boot/dts/overlays"
SRC_URI += "file://seeed-4mic-voicecard-overlay.dts;subdir=git/arch/${ARCH}/boot/dts/overlays"
SRC_URI += "file://seeed-8mic-voicecard-overlay.dts;subdir=git/arch/${ARCH}/boot/dts/overlays"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"

