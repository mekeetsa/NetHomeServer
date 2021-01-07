# Generating https certifcates for nginx using openssl

Reference:
https://www.freecodecamp.org/news/how-to-get-https-working-on-your-local-development-environment-in-5-minutes-7af615770eec/

## Step 1: Root SSL certificate
 
Generate a RSA-2048 key and save it to a file `rootCA.key`. This file will be used as the key to generate the Root SSL certificate. You will be prompted for a pass phrase which you’ll need to enter each time you use this particular key to generate a certificate.

    openssl genrsa -des3 -out rootCA.key 2048

You can use the key you generated to create a new Root SSL certificate. Save it to a file named `rootCA.pem`. This certificate will have a validity of 3650 days. Feel free to change it to any number of days you want. You’ll also be prompted for other optional information.

    openssl req -x509 -new -nodes -key rootCA.key -sha256 -days 3650 -out rootCA.pem

## Step 2: Domain SSL certificate

The root SSL certificate can now be used to issue a certificate specifically for your local development environment.

Create a new OpenSSL configuration file `server.csr.cnf` so you can import these settings when creating a certificate instead of entering them on the command line.

Create a `v3.ext` file in order to create a X509 v3 certificate.

Create a certificate key for the `nethome` host using the configuration settings stored in server.csr.cnf. This key is stored in server.key.

    openssl req -new -sha256 -nodes -out server.csr -newkey rsa:2048 -keyout server.key -config <( cat server.csr.cnf )

A certificate signing request is issued via the root SSL certificate we created earlier to create a domain certificate for localhost. The output is a certificate file called server.crt.

    openssl x509 -req -in server.csr -CA rootCA.pem -CAkey rootCA.key -CAcreateserial -out server.crt -days 1825 -sha256 -extfile v3.ext

Install the certificates in the `nginx` config directory:

    sudo cp -a server.crt server.key /etc/nginx/
    sudo systmctl restart nginx

## Step 3: Trust the root SSL certificate

Before you can use the newly created Root SSL certificate to start issuing domain certificates, you need to to tell your browser to trust your root certificate so all individual certificates issued by it are also trusted.

