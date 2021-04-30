#!/bin/bash
docker run --rm -i -t -v `pwd`:/workdir -w /workdir --entrypoint presentation/run-pandoc.sh tewarid/pandoc:2.0
