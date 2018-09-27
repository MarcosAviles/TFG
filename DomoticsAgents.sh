#!/bin/bash

cd /home/pi/Desktop/Magentix2/magentix2-2.1.1/

sh Start-Magentix.sh &

sleep 15s;

java -jar /home/pi/Desktop/TFG/dist/TFG.jar &

sleep 1s;

java -jar /home/pi/Desktop/S_Mov/dist/S_Mov.jar &

sleep 1s;

java -jar /home/pi/Desktop/Camara/dist/Camara.jar &

sleep 1s;

java -jar /home/pi/Desktop/Temperatura/dist/Temperatura.jar &

sleep 1s;

java -jar /home/pi/Desktop/Lluvia/dist/Lluvia.jar &

sleep 1s;

java -jar /home/pi/Desktop/Persiana/dist/Persiana.jar &

sleep 1s;

java -jar /home/pi/Desktop/Cochera/dist/Cochera.jar &

sleep 1s;

java -jar /home/pi/Desktop/Offline/dist/Offline.jar &

sleep 1s;

java -jar /home/pi/Desktop/Telegram/dist/Telegram.jar &

sleep 1s;

java -jar /home/pi/Desktop/Led/dist/Led.jar &