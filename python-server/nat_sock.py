import socket
import requests

while True:
    UDP_IP = "0.0.0.0"
    UDP_PORT = 3845

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind((UDP_IP, UDP_PORT))

    data, addr = sock.recvfrom(1024)
    print("Received message:", data.decode(), addr)
    Authorization = data.decode().strip()
    if len(addr) == 0:
        requests.post("http://127.0.0.1:3337/nat", headers=header, json={"error":'error'})
    if len(addr) == 2:
        ip, port = addr
        header = {
            "Authorization":Authorization
        }
        requests.post("http://127.0.0.1:3337/nat", headers=header, json={"ip":ip, "port":port})

    sock.close()
