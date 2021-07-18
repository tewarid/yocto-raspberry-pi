# OpenEmbedded/Yocto BSP layer for Matter

This repo provides a [Matter](https://github.com/project-chip/connectedhomeip) template recipe for use with OpenEmbedded/Yocto.

This layer depends on:

* URI: git://git.openembedded.org/openembedded-core
  * branch: master
  * revision: HEAD

* URI: git://git.openembedded.org/meta-openembedded
  * layers: meta-oe
  * branch: master
  * revision: HEAD

* URI: git://github.com/kraj/meta-clang
  * branch: master
  * revision: HEAD

## Contributing

The preferred way to contribute to this layer is to send GitHub pull requests or
report problems in GitHub's issue tracker.

## Recipes

recipes-matter/gn:
GN recipe that builds native executable of gn required to build Matter with OpenEmbedded/Yocto.

recipes-matter/matter:
Matter recipe that provides a template for building the `examples/all-clusters-app/linux` with OpenEmbedded/Yocto.

## Build requirements

This recipe requires clang, and GCC is not supported.

As part of its build process, Matter builds and runs some binaries on the
host. clang-native from the meta-clang layer is used to build those binaries.

For building most of the code for your target, a C++14-compliant compiler is
required. At least clang 7.0.0 (and thus the "thud" branch) is required.

## Acknowledgment

Parts of this recipe are derived from the [meta-browser](https://github.com/OSSystems/meta-browser) layer.
