#include <jni.h>
#include <string>

using namespace std;

extern "C" {
#include <raplayer.h>

typedef struct {
    JavaVM *g_vm;
    jclass activityClz;
    jobject activityObj, argObj;
} jni_callback;

JNIEXPORT jstring JNICALL Java_xyz_hurrhnn_raplayer_1jni_Raplayer_stringFromJNI(JNIEnv *env, __attribute__((unused)) jobject obj) {
    string hello = "libraplayer version v";
    hello.append(RAPLAYER_VERSION);
    return env->NewStringUTF(hello.c_str());
}

void *provide_frame_callback(void *user_args) {
    auto *jniCallbackCtx = static_cast<jni_callback *>(user_args);

    JNIEnv *env;
    jint res = (*jniCallbackCtx->g_vm).GetEnv((void **) &env, JNI_VERSION_1_6);
    if (res != JNI_OK) {
        res = (*jniCallbackCtx->g_vm).AttachCurrentThread(&env, nullptr);
        if (JNI_OK != res) {
            void *buf = std::calloc(1, RA_MAX_DATA_SIZE);
            return buf;
        }
    }
    jmethodID mid = (*env).GetMethodID((*env).GetObjectClass(jniCallbackCtx->activityObj),
                                       "bufferWriteCallback",
                                       "(Landroid/media/AudioRecord;)Ljava/nio/ByteBuffer;");

    jobject buffer = env->CallObjectMethod((*jniCallbackCtx).activityObj, mid, (*jniCallbackCtx).argObj);
    return env->GetDirectBufferAddress(buffer);
}

void consume_frame_callback(void *frame, int frame_size, void *user_args) {
    auto *jniCallbackCtx = static_cast<jni_callback *>(user_args);

    JNIEnv *env;
    jint res = (*jniCallbackCtx->g_vm).GetEnv((void **) &env, JNI_VERSION_1_6);
    if (res != JNI_OK) {
        res = (*jniCallbackCtx->g_vm).AttachCurrentThread(&env, nullptr);
        if (JNI_OK != res) {
            return;
        }
    }
    jbyteArray jFrame = (*env).NewByteArray(frame_size);
    (*env).SetByteArrayRegion(jFrame, 0, frame_size, static_cast<const jbyte *>(frame));

    jmethodID mid = (*env).GetMethodID((*env).GetObjectClass(jniCallbackCtx->activityObj),
                                       "bufferReadCallback",
                                       "([BLandroid/media/AudioTrack;)V");
    env->CallVoidMethod((*jniCallbackCtx).activityObj, mid, jFrame,
                        (*jniCallbackCtx).argObj);
}

JNIEXPORT jlong JNICALL
Java_xyz_hurrhnn_raplayer_1jni_Raplayer_spawnRaplayerFromJNI(JNIEnv *env, jobject thiz, jlong ptr,
                                                             jboolean mode, jstring address,
                                                             jshort port) {
    return raplayer_spawn((raplayer_t *) ptr, mode,
                          (char *) env->GetStringUTFChars(address, nullptr), port);
}

JNIEXPORT jlong JNICALL
Java_xyz_hurrhnn_raplayer_1jni_Raplayer_initRaplayerFromJNI(JNIEnv *env, jobject thiz) {
    void *raplayer = std::malloc(sizeof(raplayer_t));
    raplayer_init_context((raplayer_t *) raplayer);
    return (jlong) raplayer;
}

JNIEXPORT jlong JNICALL
Java_xyz_hurrhnn_raplayer_1jni_Raplayer_registerRaplayerMediaProviderFromJNI(JNIEnv *env,
                                                                             jobject obj,
                                                                             jlong raplayer_ctx,
                                                                             jlong spawn_id,
                                                                             jobject audio) {
    auto *jniCallbackCtx = static_cast<jni_callback *>(std::malloc(sizeof(jni_callback)));
    (*env).GetJavaVM(&jniCallbackCtx->g_vm);
    jniCallbackCtx->activityClz = (*env).GetObjectClass((*env).NewGlobalRef(obj));
    jniCallbackCtx->activityObj = (*env).NewGlobalRef(obj);
    jniCallbackCtx->argObj = (*env).NewGlobalRef(audio);

    return raplayer_register_media_provider((raplayer_t *) raplayer_ctx, spawn_id,
                                            provide_frame_callback, jniCallbackCtx);
}
JNIEXPORT jlong JNICALL
Java_xyz_hurrhnn_raplayer_1jni_Raplayer_registerRaplayerMediaConsumerFromJNI(JNIEnv *env,
                                                                             jobject obj,
                                                                             jlong raplayer_ctx,
                                                                             jlong spawn_id,jobject audio) {
    auto *jniCallbackCtx = static_cast<jni_callback *>(std::malloc(sizeof(jni_callback)));
    (*env).GetJavaVM(&jniCallbackCtx->g_vm);

    jniCallbackCtx->activityClz = (*env).GetObjectClass((*env).NewGlobalRef(obj));
    jniCallbackCtx->activityObj = (*env).NewGlobalRef(obj);
    jniCallbackCtx->argObj = (*env).NewGlobalRef(audio);

    return raplayer_register_media_consumer((raplayer_t *) raplayer_ctx, spawn_id,
                                            consume_frame_callback, jniCallbackCtx);
}
}
