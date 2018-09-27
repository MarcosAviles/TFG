import picamera
import time

fecha = time.strftime("%c")

camara = picamera.PiCamera()
#camara.resolution=(1920,1080)
ruta = "/home/pi/Desktop/Fotos/"+fecha+".jpeg"
camara.capture(ruta)

#camara.start_recording("video_prueba.h264")
#camara.wait_recording(20)
#camara.stop_recording()
camara.close()
print(ruta)

