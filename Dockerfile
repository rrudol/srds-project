FROM python:2.7.15

RUN easy_install pyYaml
RUN easy_install six
RUN pip install ccm

RUN ccm create test -v 2.0.5 -n 3 -s

sudo ifconfig lo0 alias 127.0.0.2 up
sudo ifconfig lo0 alias 127.0.0.3 upsudo ifconfig lo0 alias 127.0.0.3 up