#!/bin/bash
# create the demoCA folder
sudo rm -r demoCA/
mkdir demoCA
mkdir demoCA/certs
mkdir demoCA/crl
mkdir demoCA/newcerts
mkdir demoCA/private
touch demoCA/index.txt
touch demoCA/serial
echo "01\n" > demoCA/serial
rm -r server/*
openssl ecparam -out server/server.key.pem -outform PEM -name secp256r1 -genkey -noout
openssl pkcs8 -topk8 -inform PEM -outform DER -in server/server.key.pem -out server/server.key.der -nocrypt
openssl req -new -nodes -key server/server.key.pem -outform pem -out server/server.csr -sha256
openssl ca -days 3650 -keyfile CA/private/ca.key.pem -cert CA/ca.cert.pem -in server/server.csr -out server/server.cert.pem -md sha256 -outdir ./server
openssl pkcs12 -export -out server/server.cert.p12 -inkey server/server.key.pem -in server/server.cert.pem -certfile CA/ca.cert.pem -name "celsiserver"
openssl pkcs12 -nokeys -clcerts -in server/server.cert.p12 -out server/server.cert.crt
openssl x509 -inform pem -outform der -in server/server.cert.pem -out server/server.cert.cer