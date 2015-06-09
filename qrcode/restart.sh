#!/bin/sh

kill -9 $(ps -ef|grep qr-demo-ex-1.0.jar | grep java |awk '{print $2}')

rm -f qr-demo-ex-1.0.jar

cd .. && mvn clean package -DskipTests=true && cd qrcode && mv ../target/qr-demo-ex-1.0.jar .

./start.sh

tail -f demo.log


