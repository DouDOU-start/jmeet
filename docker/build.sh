#!/bin/bash

VERSION="v1.0.0"
IMAGE_NAME="jmeet-meet"

DIR_OF_ROOT=../../
DIR_OF_MEET=../
DIR_OF_LIB=../../lib-jmeet

# lib-jmeet工程编译
pushd $DIR_OF_LIB > /dev/null
    echo ""
    echo "start to compile lib-jmeet.."
    echo ""
    rm -rf dist
    npm install
    npm run build:webpack && npm run build:tsc
popd

# jmeet工程编译
pushd $DIR_OF_MEET > /dev/null
    echo ""
    echo "start to compile jmeet.."
    echo ""
    npm install
    npm install lib-jitsi-meet --force && make deploy-lib-jitsi-meet
    rm -rf libs
    make
    dpkg-buildpackage -A -rfakeroot -us -uc -tc
popd

if [ ! -d build ]; then
    mkdir build
fi

# -f 如果已存在deb包，则强制覆盖
mv -f $DIR_OF_ROOT/*.deb build

# 删除无关文件
rm -r $DIR_OF_ROOT/*.buildinfo
rm -r $DIR_OF_ROOT/*.changes



docker build -t doudou/${IMAGE_NAME}:${VERSION} .