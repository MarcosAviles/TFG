import picamera
import time
import subprocess

fecha = time.strftime("%c")

camara = picamera.PiCamera()
#camara.resolution=(1920,1080)
aux = "/home/pi/Desktop/Videos/"+fecha+".h264"
aux= aux.replace(' ','_')
aux= aux.replace(':','.')
ruta = aux.replace('h264','mp4')
camara.start_recording(aux)
camara.wait_recording(8)
camara.stop_recording()

#camara.start_recording("video_prueba.h264")
#camara.wait_recording(20)
#camara.stop_recording()
camara.close()

convertir = 'MP4Box -add '+aux+' '+ruta
error= subprocess.Popen(convertir, shell = True)
error.wait()
import os
os.remove(aux)
print(ruta)

