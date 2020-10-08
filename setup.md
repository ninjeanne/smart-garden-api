Server-Setup plumeria.jeanne.tech
======================

Update server
---------------------
```bash
root@doc:~# apt update
root@doc:~# apt dist-upgrade
root@doc:~# uname -a
Linux XXX 4.9.0-11-amd64 #1 SMP Debian 4.9.189-3+deb9u1 (2019-09-20) x86_64 GNU/Linux
```

Basics
------
```bash
root@doc:~# groupadd plumeria          # same file system privileges
root@doc:~# groupadd plumeria-adm      # "admin"-privileges: some commands via sudo are allowed
root@doc:~# useradd -m -s /bin/bash -g plumeria -G plumeria-adm -c "Plumeria CI" plumeria-ci
root@doc:~# mkdir /usr/local/share/plumeria
root@doc:~# useradd -d /usr/local/share/plumeria -s /bin/false -g plumeria -c "Plumeria App" plumeria-app
root@doc:~# chown -R root:plumeria /usr/local/share/plumeria/
root@doc:~# chmod 2770 /usr/local/share/plumeria/ # "saves" the default group
root@doc:~# for user in jeanne plumeria-ci; do ln -s /usr/local/share/plumeria "/home/$user/plumeria"; done
```

Das kam in die /etc/sudoders dazu
---------------------------------
```bash
# Allow members of group plumeria to execute commands related to their app
%plumeria-adm ALL=NOPASSWD:/bin/systemctl status smart-garden-api
%plumeria-adm ALL=NOPASSWD:/bin/systemctl start smart-garden-api
%plumeria-adm ALL=NOPASSWD:/bin/systemctl sdtop smart-garden-api
%plumeria-adm ALL=NOPASSWD:/bin/systemctl restart smart-garden-api
%plumeria-adm ALL=NOPASSWD:/bin/journalctl -u smart-garden-api
%plumeria-adm ALL=NOPASSWD:/bin/journalctl -lu smart-garden-api
%plumeria-adm ALL=NOPASSWD:/bin/journalctl -flu smart-garden-api
```

SSH
---
```bash
root@doc:~# mkdir /home/{jeanne,plumeria-ci}/.ssh
root@doc:~# touch /home/{jeanne,plumeria-ci}/.ssh/authorized_keys
root@doc:~# chmod 0600 /home/{jeanne,plumeria-ci}/.ssh/authorized_keys
```
plus add the authorized public key in every folder

Copy some Spring Boot Service to the server
---------------------------------------
```bash
scp demo.jar plumeria.jeanne.tech:/tmp
```

set up application for testing
---------------------------------------
```bash
mv /tmp/demo.jar /usr/local/share/plumeria/smart-garden-api.jar
chmod 0660 /usr/local/share/plumeria/smart-garden-api.jar
cat << EOF > application.properties
> # Listen only on localhost -- Will be published though Reverse Proxy
> server.address=127.0.0.1
> EOF
```

Test if the application is listening to the correct port and address
--------------------------------------------------
```bash
cat application.properties
# Listen only on localhost -- Will be published though Reverse Proxy
server.address=127.0.0.1
plumeria-app@doc:~$ java -jar smart-garden-api.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.2.0.RELEASE)
.....
lsof -Pni
COMMAND   PID      USER   FD   TYPE  DEVICE SIZE/OFF NODE NAME
java    22447 plumeria-app   16u  IPv6 2366285      0t0  TCP 127.0.0.1:8080 (LISTEN)
```

setup systemd service
---------------------------
```bash
vim /etc/systemd/system/smart-garden-api.service
cat /etc/systemd/system/smart-garden-api.service
[Unit]
Description=Smart Garden API App

[Service]
Type=simple
User=plumeria-app
ExecStart=/usr/bin/java -jar smart-garden-api.jar
KillSignal=SIGINT
SuccessExitStatus=130
WorkingDirectory=/usr/local/share/plumeria

[Install]
WantedBy=multi-user.target

systemctl daemon-reload
systemctl start smart-garden-api
sudo -u plumeria-app lsof -Pni
COMMAND   PID      USER   FD   TYPE  DEVICE SIZE/OFF NODE NAME
java    23201 plumeria-app   16u  IPv6 2371125      0t0  TCP 127.0.0.1:8080 (LISTEN)

curl localhost:8080/status
{"testing":123,"status":"online"}

systemctl enable smart-garden-api
```

setup reverse proxy
------------------------
```bash
sudo apt install apache2
sudo a2enmod ssl
sudo service apache2 restart 
root@doc:/etc/apache2/sites-available# nano smart-garden-api.conf

<VirtualHost *:80>
        ServerName plumeria.jeanne.tech
        ServerAdmin webmaster@jeanne.tech
        DocumentRoot /var/www/smart-garden-api

        ErrorLog ${APACHE_LOG_DIR}/smart-garden-api_error.log
        CustomLog ${APACHE_LOG_DIR}/smart-garden-api_access.log combined
RewriteEngine on
RewriteCond %{SERVER_NAME} =plumeria.jeanne.tech
RewriteRule ^ https://%{SERVER_NAME}%{REQUEST_URI} [END,NE,R=permanent]
</VirtualHost>

# vim: syntax=apache ts=4 sw=4 sts=4 sr noet
root@doc:/etc/apache2/sites-available# mkdir /var/www/smart-garden-api
root@doc:/etc/apache2/sites-available# chown -R www-data:www-data /var/www/smart-garden-api
root@doc:/etc/apache2/sites-available# a2ensite smart-garden-api.conf
root@doc:/etc/apache2/sites-available# apachectl configtest
root@doc:/etc/apache2/sites-available# systemctl reload apache2
```

SSL per Let's Encrypt einrichten und aktivieren
-----------------------------------------------
```bash
root@doc:/etc/apache2/sites-available# certbot
Saving debug log to /var/log/letsencrypt/letsencrypt.log
Plugins selected: Authenticator apache, Installer apache

Which names would you like to activate HTTPS for?
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
1: plumeria.jeanne.tech
2: XXX
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Select the appropriate numbers separated by commas and/or spaces, or leave input
blank to select all options shown (Enter 'c' to cancel): 1
Obtaining a new certificate
Performing the following challenges:
http-01 challenge for plumeria.jeanne.tech
Waiting for verification...
Cleaning up challenges
...

Please choose whether or not to redirect HTTP traffic to HTTPS, removing HTTP access.
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
1: No redirect - Make no further changes to the webserver configuration.
2: Redirect - Make all requests redirect to secure HTTPS access. Choose this for
new sites, or if you're confident your site works on HTTPS. You can undo this
change by editing your web server's configuration.
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Select the appropriate number [1-2] then [enter] (press 'c' to cancel): 1

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Congratulations! You have successfully enabled https://plumeria.jeanne.tech

You should test your configuration at:
...
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

IMPORTANT NOTES:
 - Congratulations! Your certificate and chain have been saved at:
   /etc/letsencrypt/live/plumeria.jeanne.tech/fullchain.pem
   Your key file has been saved at:
   /etc/letsencrypt/live/plumeria.jeanne.tech/privkey.pem
   Your cert will expire on 2020-01-21. To obtain a new or tweaked
   version of this certificate in the future, simply run certbot again
   with the "certonly" option. To non-interactively renew *all* of
   your certificates, run "certbot renew"
 - If you like Certbot, please consider supporting our work by:

   Donating to ISRG / Let's Encrypt:   https://letsencrypt.org/donate
   Donating to EFF:                    https://eff.org/donate-le
root@doc:/etc/apache2/sites-available# nano smart-garden-api-le-ssl.conf

<IfModule mod_ssl.c>
<VirtualHost *:443>
        ServerName plumeria.jeanne.tech
        ServerAdmin webmaster@jeanne.tech
        DocumentRoot /var/www/smart-garden-api

        ErrorLog ${APACHE_LOG_DIR}/smart-garden-api_error.log
        CustomLog ${APACHE_LOG_DIR}/smart-garden-api_access.log combined

        SSLEngine on
        SSLCertificateFile /etc/letsencrypt/live/plumeria.jeanne.tech/fullchain.pem
        SSLCertificateKeyFile /etc/letsencrypt/live/plumeria.jeanne.tech/privkey.pem
        Include /etc/letsencrypt/options-ssl-apache.conf

        # Reverse Proxy without forwarding
        ProxyPreserveHost On
        ProxyRequests Off

        ProxyPass / http://127.0.0.1:8080/
        ProxyPassReverse / http://127.0.0.1:8080/

        <Proxy *>
          Allow from all
        </Proxy>

</VirtualHost>
</IfModule>
root@doc:/etc/apache2/sites-available# a2ensite smart-garden-api-le-ssl.conf
root@doc:/etc/apache2/sites-available# apachectl configtest
Syntax OK
root@doc:/etc/apache2/sites-available# systemctl reload apache2
root@doc:/etc/apache2/sites-available# systemctl status apache2
```

automatically renewing of let's encrypt certificates
-----------------------------------------------------
```bash
root@doc:/etc/apache2/sites-available# crontab -l
[...]
# m h  dom mon dow   command
30 2 * * 1 /usr/bin/certbot renew >> /var/log/le-renew.log
```
