# gn-native contains the GN binary used to configure Matter.
SUMMARY = "A meta-build system that generates build files for Ninja."
HOMEPAGE = "https://gn.googlesource.com/gn/"

inherit native

SRC_URI = "git://gn.googlesource.com/gn;protocol=https"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/git"

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=0fca02217a5d49a14dfe2d11837bb34d"

# The build system expects the linker to be invoked via the compiler. If we use
# the default value for BUILD_LD, it will fail because it does not recognize
# some of the arguments passed to it.
BUILD_LD = "${CXX}"

# Use LLVM's ar rather than binutils'. Depending on the optimizations enabled
# in the build ar(1) may not be enough.
BUILD_AR = "llvm-ar"

DEPENDS = "clang-native ninja-native python3-native"

do_configure[noexec] = "1"

do_compile() {
    python3 build/gen.py
    ninja -C out
}

do_install() {
	install -d ${D}${bindir}
    install -D -m 0755  ${S}/out/gn ${D}${bindir}/gn
}

INSANE_SKIP_${PN} += "already-stripped"
