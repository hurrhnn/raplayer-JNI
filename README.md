# raplayer_jni
# NOTICE - 프로젝트 클론 시 submodule까지 한번에 받아주세요! (`git clone https://github.com/hurrhnn/raplayer-JNI.git --recursive`)

[raplayer](https://github.com/hurrhnn/raplayer) c native library를 이용한 음성 채팅 앱 입니다.

## requirements
android
```
Android NDK 25 이상
CMake 3.12 이상
디버그(기본) 빌드 시 gcov 바이너리 path를 확인하시기 바랍니다. 또는 raplayer의 cmake 빌드 타겟을 Release로 변경하여 주세요.
```

python-server
```
server.py의 의존성 pyjwt를 pip에서 설치하여 주세요.
```

해당 앱(프로젝트)은 python-server 디렉토리 안 server.py, nat_sock.py 서버와 통신합니다. 서버 프로그램 환경을 구축 해 주세요.
