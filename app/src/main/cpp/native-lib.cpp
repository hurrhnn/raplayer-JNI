#include <jni.h>
#include <string>

using namespace std;

extern "C" JNIEXPORT jstring JNICALL Java_xyz_hurrhnn_raplayer_1jni_MainActivity_stringFromJNI(JNIEnv *env,
                                                                                    __attribute__((unused)) jobject obj) {
    std::string hello = "Hello JNI World!";
    return env->NewStringUTF(hello.c_str());
}
