#!/bin/bash

echo "Start"
openssl rand -base64 756 > /etc/mongodb/pki/keyfile
chmod 400 /etc/mongodb/pki/keyfile
chown mongodb:mongodb /etc/mongodb/pki/keyfile
echo "END"