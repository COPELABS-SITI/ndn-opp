APP_ABI := armeabi-v7a arm64-v8a x86

APP_STL := gnustl_shared
APP_CPPFLAGS += -fexceptions -frtti -std=c++11 -Wno-deprecated-declarations
APP_LIBCRYSTAX += shared

NDK_TOOLCHAIN_VERSION := 4.9
APP_PLATFORM := android-18
