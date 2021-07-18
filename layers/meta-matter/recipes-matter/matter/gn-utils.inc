# GN host architecture helpers.
#
# BUILD_ARCH's value corresponds to what uname returns as the machine name.
# The mapping in gn_host_arch_name() tries to match several possible values
# returned by the Linux kernel in uname(2) into the corresponding values GN
# understands.
def gn_host_arch_name(d):
    """Returns a GN architecture name corresponding to the build host's machine
    architecture."""
    import re
    arch_translations = {
        r'aarch64.*': 'arm64',
        r'arm.*': 'arm',
        r'i[3456]86$': 'x86',
        r'x86_64$': 'x64',
    }
    build_arch = d.getVar("BUILD_ARCH")
    for arch_regexp, gn_arch_name in arch_translations.items():
        if re.match(arch_regexp, build_arch):
            return gn_arch_name
    bb.fatal('Unsuported BUILD_ARCH value: "%s"' % build_arch)

# GN target architecture helpers.
#
# Determining the target architecture is more difficult, as there are many
# different values we can use on the Yocto side (e.g. TUNE_ARCH, TARGET_ARCH,
# MACHINEOVERRIDES etc). What we do is define the mapping with regular,
# non-Python variables with overrides that are generic enough (i.e. "x86"
# instead of "i586") and then use gn_target_arch_name() to return the right
# value with some validation.
GN_TARGET_ARCH_NAME_aarch64 = "arm64"
GN_TARGET_ARCH_NAME_arm = "arm"
GN_TARGET_ARCH_NAME_x86 = "x86"
GN_TARGET_ARCH_NAME_x86-64 = "x64"

def clang_install_path(d):
    """Return clang compiler install path."""
    return d.getVar("STAGING_BINDIR_NATIVE")

def gn_target_arch_name(d):
    """Returns a GN architecture name corresponding to the target machine's
    architecture."""
    name = d.getVar("GN_TARGET_ARCH_NAME")
    if name is None:
        bb.fatal('Unsupported target architecture. A valid override for the '
                 'GN_TARGET_ARCH_NAME variable could not be found.')
    return name

def write_toolchain_file(d, app_dir):
    """Creates a complete GN toolchain file under |app_dir|."""
    import string
    # Create a toolchain for yocto as a wrapper on top of the "gcc_toolchain"
    # template, to accommodate our cross-compilation needs.
    toolchain_tmpl = string.Template(
        'gcc_toolchain("${toolchain_name}") {\n'
        '  cc = "${cc}"\n'
        '  cxx = "${cxx}"\n'
        '  ar = "${ar}"\n'
        '  toolchain_args = {\n'
        '    current_cpu = "${current_cpu}"\n'
        '    current_os = "linux"\n'
        '    is_clang = true\n'
        '  }\n'
        '}\n'
    )

    native_toolchain = {
        'toolchain_name': 'yocto_native',
        'current_cpu': gn_host_arch_name(d),
        'cc': d.expand('${BUILD_CC}'),
        'cxx': d.expand('${BUILD_CXX}'),
        'ar': d.expand('${BUILD_AR}'),
    }
    target_toolchain = {
        'toolchain_name': 'yocto_target',
        'current_cpu': gn_target_arch_name(d),
        'cc': d.expand('${CC}'),
        'cxx': d.expand('${CXX}'),
        'ar': d.expand('${AR}'),
    }

    source_dir = d.expand("${S}")
    app_dir_absolute_path = os.path.join(source_dir, app_dir)
    toolchain_dir = os.path.join(app_dir_absolute_path, "build", "toolchain", "yocto")
    bb.utils.mkdirhier(toolchain_dir)
    file_path = os.path.join(toolchain_dir, "BUILD.gn")
    with open(file_path, 'w') as toolchain_file:
        rel_path = os.path.relpath(source_dir, app_dir_absolute_path)
        toolchain_file.write(
            '# This file has been generated automatically.\n'
            '\n'
            f'import("//{rel_path}/build/toolchain/gcc_toolchain.gni")\n'
            '\n'
        )
        toolchain_file.write(toolchain_tmpl.substitute(native_toolchain))
        toolchain_file.write(toolchain_tmpl.substitute(target_toolchain))
