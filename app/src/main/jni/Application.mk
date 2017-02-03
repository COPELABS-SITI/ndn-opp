APP_ABI := armeabi-v7a

APP_STL := gnustl_static
APP_CPPFLAGS += -fexceptions -frtti -std=c++11 -Wno-deprecated-declarations
APP_LIBCRYSTAX += static

NDK_TOOLCHAIN_VERSION := 4.9
APP_PLATFORM := android-17
