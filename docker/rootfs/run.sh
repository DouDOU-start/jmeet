#!/bin/bash

INNER_IP="10.8.6.245"

DOMAIN="docker.jitsi.com"
EMAIL="1021217094@qq.com"
JVB_SECRET="jvb123"

APPID="appid123"
APP_SECRET="appsecret123"

# 容器初次运行时安装deb包
if [ -d "/build" ]; then
    echo "jitsi-meet-web-config jitsi-videobridge/jvb-hostname string $DOMAIN" | debconf-set-selections
    echo "jitsi-meet-web-config jitsi-meet/cert-choice select 1" | debconf-set-selections
    echo "jitsi-meet-web-config jitsi-meet/email string $EMAIL" | debconf-set-selections
    echo "jitsi-meet-web-config jitsi-meet/jaas-choice boolean no" | debconf-set-selections 

    echo "jitsi-meet-prosody jitsi-videobridge/jvbsecret password $JVB_SECRET" | debconf-set-selections  

    echo "jitsi-meet-tokens jitsi-meet-tokens/appid string $APPID" | debconf-set-selections
    echo "jitsi-meet-tokens jitsi-meet-tokens/appsecret password $APP_SECRET" | debconf-set-selections

    dpkg -i /build/jitsi-meet-web_1.0.1-1_all.deb
    dpkg -i /build/jitsi-meet-web-config_1.0.1-1_all.deb
    dpkg -i /build/jitsi-meet-prosody_1.0.1-1_all.deb
    dpkg -i /build/jitsi-meet-tokens_1.0.1-1_all.deb
    dpkg -i /build/jitsi-meet-turnserver_1.0.1-1_all.deb

    rm -rf /build/

    # 修改bosh访问路径
    # sed -i "s@bosh: '//$DOMAIN/'@bosh: '//$INNER_IP/'@g" /etc/jitsi/meet/$DOMAIN-config.js

fi

# 重启服务
service prosody restart
service nginx restart

# Docker容器后台运行,就必须有一个前台进程
dummy=/config/dummy
if [ ! -f "$dummy" ]; then
	touch $dummy
fi
tail -f $dummy