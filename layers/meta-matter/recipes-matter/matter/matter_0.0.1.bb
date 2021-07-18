DESCRIPTION = "Matter is the foundation for connected things."
HOMEPAGE = "https://buildwithmatter.com"

SRC_URI = "gitsm://github.com/project-chip/connectedhomeip;protocol=https"
SRCREV = "${AUTOREV}"

# Patches
SRC_URI += "file://0001-Use-udhcpc-instead-of-dhclient.patch"

S = "${WORKDIR}/git"

# Only clang is supported for now.
TOOLCHAIN = "clang"
TOOLCHAIN_class-native = "clang"

# This makes the target build use libc++ and compiler_rt instead of the GNU
# runtime. The native binaries compiled and run as part of the build still use
# libstdc++ and libgcc though (see https://github.com/kraj/meta-clang/issues/449).
RUNTIME = "llvm"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "\
    file://${S}/LICENSE;md5=86d3f3a95c324c9479bd8986968f4327 \
    file://${S}/third_party/openthread/repo/LICENSE;md5=543b6fe90ec5901a683320a36390c65f \
    "

require gn-utils.inc

# The actual directory name in out/ is irrelevant for GN.
APP_DIR = "examples/all-clusters-app/linux"
OUTPUT_DIR = "out/Release"
B = "${S}/${APP_DIR}/${OUTPUT_DIR}"

# Append instead of assigning;
# adds packages to DEPENDS.
DEPENDS += " \
    python3-native \
    python3-pip-native \
    glib-2.0-native \
    gn-native \
    ninja-native \
    glib-2.0 \
    avahi \
"

COMPATIBLE_MACHINE = "(-)"
COMPATIBLE_MACHINE_aarch64 = "(.*)"
COMPATIBLE_MACHINE_armv6 = "(.*)"
COMPATIBLE_MACHINE_armv7a = "(.*)"
COMPATIBLE_MACHINE_armv7ve = "(.*)"
COMPATIBLE_MACHINE_x86 = "(.*)"
COMPATIBLE_MACHINE_x86-64 = "(.*)"

# Also build the parts that are run on the host with clang.
BUILD_AR_toolchain-clang = "llvm-ar"
BUILD_CC_toolchain-clang = "clang"
BUILD_CXX_toolchain-clang = "clang++"
BUILD_LD_toolchain-clang = "clang"

# Make sure pkg-config, when used with the host's toolchain to build the
# binaries we need to run on the host, uses the right pkg-config to avoid
# passing include directories belonging to the target.
GN_ARGS += 'host_pkg_config="pkg-config-native"'

# Tell GN whether we want a debug build or not
GN_ARGS += '${@oe.utils.conditional('DEBUG_BUILD','1','is_debug=true','is_debug=false',d)}'

# Use lld linker, it's quicker, see https://lld.llvm.org/#performance
GN_ARGS += "use_lld=true use_gold=false"

# Don't treat compiler warning as errors, caused by clang version mismatch
GN_ARGS += "treat_warnings_as_errors=false"

# Toolchains we will use for the build. We need to point to the toolchain file
# we've created, set the right target architecture etc.
GN_ARGS += ' \
        custom_toolchain="//build/toolchain/yocto:yocto_target" \
        host_toolchain="//build/toolchain/yocto:yocto_native" \
        is_clang=true \
        target_cpu="${@gn_target_arch_name(d)}" \
'

# ARM builds need special additional flags (see ${S}/build/config/arm.gni).
# If we do not pass |arm_arch| and friends to GN, it will deduce a value that
# will then conflict with TUNE_CCARGS and CC.
def get_compiler_flag(params, param_name, d):
    """Given a sequence of compiler arguments in |params|, returns the value of
    an option |param_name| or an empty string if the option is not present."""
    for param in params:
      if param.startswith(param_name):
        return param.split('=')[1]
    return ''

ARM_FLOAT_ABI = "${@bb.utils.contains('TUNE_FEATURES', 'callconvention-hard', 'hard', 'softfp', d)}"
ARM_FPU = "${@get_compiler_flag(d.getVar('TUNE_CCARGS').split(), '-mfpu', d)}"
ARM_TUNE = "${@get_compiler_flag(d.getVar('TUNE_CCARGS').split(), '-mcpu', d)}"
ARM_VERSION_aarch64 = "8"
ARM_VERSION_armv7a = "7"
ARM_VERSION_armv7ve = "7"
ARM_VERSION_armv6 = "6"

# GN computes and defaults to it automatically where needed
# forcing it from cmdline breaks build on places where it ends up
# overriding what GN wants
TUNE_CCARGS_remove = "-mthumb"

GN_ARGS_append_arm = ' \
        arm_float_abi="${ARM_FLOAT_ABI}" \
        arm_fpu="${ARM_FPU}" \
        arm_tune="${ARM_TUNE}" \
'

python do_write_toolchain_file () {
    """Writes a BUILD.gn file for Yocto detailing its toolchains."""
    write_toolchain_file(d, d.expand("${APP_DIR}"))
}
addtask write_toolchain_file after do_patch before do_configure

do_configure() {
    ln -f -s ${TMPDIR}/hosttools/python3 ${TMPDIR}/hosttools/python
    cd ${S}/${APP_DIR}
    gn gen --args='${GN_ARGS}' "${OUTPUT_DIR}"
}

do_compile() {
    cd ${S}/${APP_DIR}
    ninja -C "${OUTPUT_DIR}"
}
do_compile[progress] = "outof:^\[(\d+)/(\d+)\]\s+"

do_install() {
    install -d ${D}${bindir}
    install -Dm 0755 ${S}/${APP_DIR}/${OUTPUT_DIR}/chip-all-clusters-app ${D}${bindir}
}

FILES_${PN} = " \
    ${bindir}/* \
"

PACKAGE_DEBUG_SPLIT_STYLE = "debug-without-src"

# There is no need to ship empty -dev packages.
ALLOW_EMPTY_${PN}-dev = "0"
