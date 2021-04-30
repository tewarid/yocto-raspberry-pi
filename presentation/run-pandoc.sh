#!/bin/sh

# create standalone revealjs presentation
pandoc --lua-filter presentation/filter.lua \
    --slide-level=2 \
    -o presentation/slides.html \
    -t revealjs -s \
    -V minScale=0.2 -V maxScale=1.5 \
    -V width=\"100%\" -V height=\"150%\" \
    -V margin=0.05 \
    presentation/front-matter.md README.md

# create PDF slides using beamer
pandoc --lua-filter presentation/filter.lua \
    --slide-level=2 \
    -o presentation/slides.pdf \
    -t beamer \
    -V theme:default \
    -V aspectratio:169 \
    presentation/front-matter.md README.md
