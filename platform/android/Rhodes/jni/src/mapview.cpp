#include "rhodes/JNIRhodes.h"

#include <common/rhoparams.h>
#include <common/map/MapEngine.h>

#undef DEFAULT_LOGCATEGORY
#define DEFAULT_LOGCATEGORY "MapView"

namespace rho
{
namespace common
{
namespace map
{

class AndroidImage : public IDrawingImage
{
private:
    AndroidImage(AndroidImage const &);
    AndroidImage &operator=(AndroidImage const &);

    AndroidImage(int *count, jobject *bitmap);

    void init();

public:
    AndroidImage(jobject bitmap);
    ~AndroidImage();

    AndroidImage *clone();

    jobject bitmap() const {return *m_bitmap;}

    int width() const {return m_width;}
    int height() const {return m_height;}

private:
    int *m_count;
    jobject *m_bitmap;
    int m_width;
    int m_height;
};

class AndroidDrawingContext : public IDrawingContext
{
public:
    AndroidDrawingContext(jobject device, jobject canvas)
        :m_device(device), m_canvas(canvas)
    {}

    void drawImage(int x, int y, IDrawingImage *image);
    void drawText(int x, int y, String const &text, int color);

private:
    jobject m_device;
    jobject m_canvas;
};

class AndroidMapDevice : public IDrawingDevice
{
public:
    AndroidMapDevice();
    ~AndroidMapDevice();

    void attach(jobject jDevice) {m_jdevice = jDevice;}

    void setMapView(IMapView *mapview) {m_mapview = mapview;}
    IMapView *mapView() const {return m_mapview;}

    IDrawingImage* createImage(String const &path);
    IDrawingImage* createImage(void const *p, size_t s);
    IDrawingImage* cloneImage(IDrawingImage *image);
    void destroyImage(IDrawingImage* image);

    void requestRedraw();

    void paint(jobject canvas);

private:
    IMapView *m_mapview;
    jobject m_jdevice;
};

AndroidImage::AndroidImage(jobject bitmap)
    :m_count(new int(1)), m_bitmap(new jobject(bitmap))
{
    init();
}

AndroidImage::AndroidImage(int *count, jobject *bitmap)
    :m_count(count), m_bitmap(bitmap)
{
    ++*m_count;
    init();
}

void AndroidImage::init()
{
    RAWTRACE(__PRETTY_FUNCTION__);

    JNIEnv *env = jnienv();
    jclass cls = getJNIClass(RHODES_JAVA_CLASS_BITMAP);
    if (!cls) return;
    jmethodID midWidth = getJNIClassMethod(env, cls, "getWidth", "()I");
    if (!midWidth) return;
    jmethodID midHeight = getJNIClassMethod(env, cls, "getHeight", "()I");
    if (!midHeight) return;

    m_width = env->CallIntMethod(*m_bitmap, midWidth);
    m_height = env->CallIntMethod(*m_bitmap, midHeight);
}

AndroidImage::~AndroidImage()
{
    RAWTRACE(__PRETTY_FUNCTION__);
    if (--*m_count == 0)
    {
        JNIEnv *env = jnienv();
        jclass cls = getJNIClass(RHODES_JAVA_CLASS_MAPVIEW);
        jmethodID mid = getJNIClassStaticMethod(env, cls, "destroyImage", "(Landroid/graphics/Bitmap;)V");

        if (cls && mid)
            env->CallStaticVoidMethod(cls, mid, *m_bitmap);

        delete m_count;
    }
}

AndroidImage *AndroidImage::clone()
{
    RAWTRACE(__PRETTY_FUNCTION__);
    return new AndroidImage(m_count, m_bitmap);
}

void AndroidDrawingContext::drawImage(int x, int y, IDrawingImage *image)
{
    RAWTRACE(__PRETTY_FUNCTION__);
    if (!image)
        return;

    jobject bitmap = ((AndroidImage*)image)->bitmap();

    JNIEnv *env = jnienv();
    jclass cls = env->GetObjectClass(m_device);
    if (!cls) return;
    jmethodID mid = getJNIClassMethod(env, cls, "drawImage", "(Landroid/graphics/Canvas;IILandroid/graphics/Bitmap;)V");
    if (!mid) return;

    env->CallVoidMethod(m_device, mid, m_canvas, x, y, bitmap);
}

void AndroidDrawingContext::drawText(int x, int y, String const &text, int color)
{
    RAWTRACE(__PRETTY_FUNCTION__);
    JNIEnv *env = jnienv();
    jclass cls = env->GetObjectClass(m_device);
    if (!cls) return;
    jmethodID mid = getJNIClassMethod(env, cls, "drawText", "(Landroid/graphics/Canvas;IILjava/lang/String;I)V");
    if (!mid) return;

    jstring jText = rho_cast<jstring>(text);
    env->CallVoidMethod(m_device, mid, m_canvas, x, y, jText, color);
    env->DeleteLocalRef(jText);
}

AndroidMapDevice::AndroidMapDevice()
    :m_mapview(NULL), m_jdevice(NULL)
{
    RAWTRACE(__PRETTY_FUNCTION__);
    JNIEnv *env = jnienv();
    jclass cls = getJNIClass(RHODES_JAVA_CLASS_MAPVIEW);
    if (!cls) return;
    jmethodID mid = getJNIClassStaticMethod(env, cls, "create", "(J)V");
    if (!mid) return;
    env->CallStaticVoidMethod(cls, mid, (jlong)this);
}

AndroidMapDevice::~AndroidMapDevice()
{
    RAWTRACE(__PRETTY_FUNCTION__);

    if (!m_jdevice)
        return;

    JNIEnv *env = jnienv();
    jclass cls = getJNIClass(RHODES_JAVA_CLASS_MAPVIEW);
    if (!cls) return;
    jmethodID mid = getJNIClassStaticMethod(env, cls, "destroy", "(Lcom/rhomobile/rhodes/mapview/MapView;)V");
    if (!mid) return;
    env->CallStaticVoidMethod(cls, mid, m_jdevice);
    m_jdevice = NULL;
}

IDrawingImage *AndroidMapDevice::createImage(String const &path)
{
    RAWTRACE(__PRETTY_FUNCTION__);

    JNIEnv *env = jnienv();
    jclass cls = getJNIClass(RHODES_JAVA_CLASS_MAPVIEW);
    if (!cls) return NULL;
    jmethodID mid = getJNIClassStaticMethod(env, cls, "createImage", "(Ljava/lang/String;)Landroid/graphics/Bitmap;");
    if (!mid) return NULL;

    jstring jPath = rho_cast<jstring>(path);
    jobject bitmap = env->CallStaticObjectMethod(cls, mid, jPath);
    env->DeleteLocalRef(jPath);
    return new AndroidImage(bitmap);
}

IDrawingImage *AndroidMapDevice::createImage(void const *p, size_t size)
{
    RAWTRACE(__PRETTY_FUNCTION__);

    JNIEnv *env = jnienv();
    jclass cls = getJNIClass(RHODES_JAVA_CLASS_MAPVIEW);
    if (!cls) return NULL;
    jmethodID mid = getJNIClassStaticMethod(env, cls, "createImage", "([B)Landroid/graphics/Bitmap;");
    if (!mid) return NULL;

    jbyteArray data = env->NewByteArray(size);
    if (!data) return NULL;
    env->SetByteArrayRegion(data, 0, size, (jbyte const *)p);

    jobject bitmap = env->CallStaticObjectMethod(cls, mid, data);
    return new AndroidImage(bitmap);
}

IDrawingImage *AndroidMapDevice::cloneImage(IDrawingImage *image)
{
    RAWTRACE(__PRETTY_FUNCTION__);
    return image ? ((AndroidImage *)image)->clone() : NULL;
}

void AndroidMapDevice::destroyImage(IDrawingImage *image)
{
    RAWTRACE(__PRETTY_FUNCTION__);
    delete image;
}

void AndroidMapDevice::requestRedraw()
{
    RAWTRACE(__PRETTY_FUNCTION__);

    if (!m_jdevice)
        return;

    JNIEnv *env = jnienv();
    jclass cls = getJNIClass(RHODES_JAVA_CLASS_MAPVIEW);
    if (!cls) return;
    jmethodID mid = getJNIClassMethod(env, cls, "redraw", "()V");
    if (!mid) return;
    env->CallVoidMethod(m_jdevice, mid);
}

void AndroidMapDevice::paint(jobject canvas)
{
    RAWTRACE(__PRETTY_FUNCTION__);

    if (!m_mapview)
        return;

    std::auto_ptr<AndroidDrawingContext> context(new AndroidDrawingContext(m_jdevice, canvas));
    m_mapview->paint(context.get());
}

} // namespace map
} // namespace common
} // namespace rho

namespace rhomap = rho::common::map;
static rhomap::AndroidMapDevice *s_mapdevice = NULL;

RHO_GLOBAL void JNICALL Java_com_rhomobile_rhodes_mapview_MapView_attachToNativeDevice
  (JNIEnv *, jobject jDevice, jlong nativeDevice)
{
    RAWTRACE("Java_com_rhomobile_rhodes_mapview_MapView_attachToNativeDevice");
    rhomap::AndroidMapDevice *device = (rhomap::AndroidMapDevice*)nativeDevice;
    device->attach(jDevice);
}

RHO_GLOBAL void JNICALL Java_com_rhomobile_rhodes_mapview_MapView_setSize
  (JNIEnv *, jobject, jlong nativeDevice, jint width, jint height)
{
    RAWTRACE("Java_com_rhomobile_rhodes_mapview_MapView_setSize");
    rhomap::AndroidMapDevice *device = (rhomap::AndroidMapDevice*)nativeDevice;
    rhomap::IMapView *mapview = device->mapView();
    if (mapview)
        mapview->setSize(width, height);
}

RHO_GLOBAL void JNICALL Java_com_rhomobile_rhodes_mapview_MapView_paint
  (JNIEnv *, jobject, jlong nativeDevice, jobject canvas)
{
    RAWTRACE("Java_com_rhomobile_rhodes_mapview_MapView_paint");
    rhomap::AndroidMapDevice *device = (rhomap::AndroidMapDevice*)nativeDevice;
    device->paint(canvas);
}

RHO_GLOBAL void mapview_close()
{
    if (s_mapdevice)
    {
        rhomap::IMapView *mapview = s_mapdevice->mapView();
        RAWTRACE1("mapview_close: mapview=%p", mapview);
        rho_map_destroy(mapview);
    }
    delete s_mapdevice;
    s_mapdevice = NULL;
}

RHO_GLOBAL void mapview_create(rho_param *p)
{
    mapview_close();

    s_mapdevice = new rhomap::AndroidMapDevice();
    rhomap::IMapView *mapview = rho_map_create(p, s_mapdevice);
    RAWTRACE1("mapview_create: mapview=%p", mapview);
    s_mapdevice->setMapView(mapview);
}

RHO_GLOBAL VALUE mapview_state_started()
{
    return rho_ruby_create_boolean(!!s_mapdevice);
}

RHO_GLOBAL double mapview_state_center_lat()
{
    if (!s_mapdevice)
        return 0;
    return s_mapdevice->mapView()->latitude();
}

RHO_GLOBAL double mapview_state_center_lon()
{
    if (!s_mapdevice)
        return 0;
    return s_mapdevice->mapView()->longitude();
}

#if 0
RHO_GLOBAL void mapview_create(rho_param *p)
{
#ifdef RHO_GOOGLE_API_KEY
    JNIEnv *env = jnienv();
    jclass clsMapView = getJNIClass(RHODES_JAVA_CLASS_MAPVIEW);
    if (!clsMapView) return;
    jmethodID midCreate = getJNIClassStaticMethod(env, clsMapView, "create", "(Ljava/lang/String;Ljava/util/Map;)V");
    if (!midCreate) return;

    if (p->type != RHO_PARAM_HASH) {
        RAWLOG_ERROR("create: wrong input parameter (expect Hash)");
        return;
    }

    jhobject paramsObj = jhobject(RhoValueConverter(env).createObject(p));
    jhstring keyObj = rho_cast<jhstring>(RHO_GOOGLE_API_KEY);
    env->CallStaticVoidMethod(clsMapView, midCreate, keyObj.get(), paramsObj.get());
#else
    RAWLOG_ERROR("MapView disabled at build time");
#endif
}

RHO_GLOBAL void mapview_close()
{
#ifdef RHO_GOOGLE_API_KEY
    JNIEnv *env = jnienv();
    jclass clsMapView = getJNIClass(RHODES_JAVA_CLASS_MAPVIEW);
    if (!clsMapView) return;
    jmethodID midClose = getJNIClassStaticMethod(env, clsMapView, "close", "()V");
    if (!midClose) return;

    env->CallStaticVoidMethod(clsMapView, midClose);
#endif
}

RHO_GLOBAL VALUE mapview_state_started()
{
#ifdef RHO_GOOGLE_API_KEY
    VALUE nil = rho_ruby_get_NIL();
    JNIEnv *env = jnienv();
    jclass cls = getJNIClass(RHODES_JAVA_CLASS_MAPVIEW);
    if (!cls) return nil;
    jmethodID mid = getJNIClassStaticMethod(env, cls, "isStarted", "()Z");
    if (!mid) return nil;

    return rho_ruby_create_boolean(env->CallStaticBooleanMethod(cls, mid));
#else
    return rho_ruby_create_boolean(0);
#endif
}

RHO_GLOBAL double mapview_state_center_lat()
{
#ifdef RHO_GOOGLE_API_KEY
    JNIEnv *env = jnienv();
    jclass cls = getJNIClass(RHODES_JAVA_CLASS_MAPVIEW);
    if (!cls) return 0;
    jmethodID mid = getJNIClassStaticMethod(env, cls, "getCenterLatitude", "()D");
    if (!mid) return 0;

    return env->CallStaticDoubleMethod(cls, mid);
#else
    return 0;
#endif
}

RHO_GLOBAL double mapview_state_center_lon()
{
#ifdef RHO_GOOGLE_API_KEY
    JNIEnv *env = jnienv();
    jclass cls = getJNIClass(RHODES_JAVA_CLASS_MAPVIEW);
    if (!cls) return 0;
    jmethodID mid = getJNIClassStaticMethod(env, cls, "getCenterLongitude", "()D");
    if (!mid) return 0;

    return env->CallStaticDoubleMethod(cls, mid);
#else
    return 0;
#endif
}
#endif
