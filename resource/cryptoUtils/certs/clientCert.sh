#!/bin/bash
echo "02\n" > demoCA/serial
rm -r client/*
openssl ecparam -out client/client.key.pem -outform PEM -name secp256r1 -genkey -noout
openssl pkcs8 -topk8 -inform PEM -outform DER -in client/client.key.pem -out client/client.key.der -nocrypt
openssl req -new -nodes -key client/client.key.pem -outform pem -out client/client.csr -sha256
openssl ca -days 3650 -keyfile CA/private/ca.key.pem -cert CA/ca.cert.pem -in client/client.csr -out client/client.cert.pem -md sha256 -outdir ./client
openssl pkcs12 -export -out client/client.cert.p12 -inkey client/client.key.pem -in client/client.cert.pem -certfile CA/ca.cert.pem -name "celsiclient"
openssl pkcs12 -nokeys -clcerts -in client/client.cert.p12 -out client/client.cert.crt
openssl x509 -inform pem -outform der -in client/client.cert.pem -out client/client.cert.cer