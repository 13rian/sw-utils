#!/bin/bash
rm CA/private/ca.key.pem
rm -rf demoCA
openssl ecparam -out CA/private/ca.key.pem -name secp256r1 -genkey -noout
rm CA/ca.cert.pem
openssl req -new -key CA/private/ca.key.pem -x509 -nodes -days 3650 -outform pem -sha256 -out CA/ca.cert.pem
openssl pkcs12 -export -out CA/ca.cert.p12 -inkey CA/private/ca.key.pem -in CA/ca.cert.pem 
openssl pkcs12 -nokeys -chain -in CA/ca.cert.p12 -out CA/ca.cert.crt
openssl x509 -inform pem -outform der -in CA/ca.cert.pem -out CA/ca.cert.cer