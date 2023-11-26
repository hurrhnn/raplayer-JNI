#include <jni.h>
#include <string>

using namespace std;

extern "C" {
#include <raplayer.h>

typedef struct {
    JavaVM *g_vm;
    jclass mainActivityClz;
    jobject mainActivityObj, audioTrackObj;
} jni_callback;

JNIEXPORT jstring JNICALL Java_xyz_hurrhnn_raplayer_1jni_MainActivity_stringFromJNI(JNIEnv *env,
                                                                                    __attribute__((unused)) jobject obj) {
    string hello = "libraplayer version v";
    hello.append(RAPLAYER_VERSION);
    return env->NewStringUTF(hello.c_str());
}

void client_frame_callback(void *frame, int frame_size, void *user_args) {
    auto *jniCallbackCtx = static_cast<jni_callback *>(user_args);

    JNIEnv *env;
    jint res = (*jniCallbackCtx->g_vm).GetEnv((void **) &env, JNI_VERSION_1_6);
    if (res != JNI_OK) {
        res = (*jniCallbackCtx->g_vm).AttachCurrentThread(&env, nullptr);
        if (JNI_OK != res) {
            return;
        }
    }

    frame_size *= (2 * 2); // 2 channels * 16 bit
    jbyteArray jFrame = (*env).NewByteArray(frame_size);
    (*env).SetByteArrayRegion(jFrame, 0, frame_size, static_cast<const jbyte *>(frame));

    jmethodID mid = (*env).GetMethodID((*env).FindClass("xyz/hurrhnn/raplayer_jni/MainActivity"),
                                       "bufferCallback",
                                       "([BLandroid/media/AudioTrack;)V");
    env->CallVoidMethod((*jniCallbackCtx).mainActivityObj, mid, jFrame,
                        (*jniCallbackCtx).audioTrackObj);
}

JNIEXPORT jboolean JNICALL Java_xyz_hurrhnn_raplayer_1jni_MainActivity_startClientFromJNI(
        JNIEnv *env,
        jobject obj, jstring address, jint port, jobject audio) {

    void *status = std::malloc(sizeof(int));

    jni_callback jniCallbackCtx;
    (*env).GetJavaVM(&jniCallbackCtx.g_vm);
    jniCallbackCtx.mainActivityClz = (*env).GetObjectClass((*env).NewGlobalRef(obj));
    jniCallbackCtx.mainActivityObj = (*env).NewGlobalRef(obj);
    jniCallbackCtx.audioTrackObj = (*env).NewGlobalRef(audio);

    ra_client((char *) env->GetStringUTFChars(address, nullptr), port, client_frame_callback, &jniCallbackCtx, (int *) status);
    return true;
}
}
