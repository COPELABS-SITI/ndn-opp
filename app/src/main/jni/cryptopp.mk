# Building the CryptOpp shared library.
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := cryptopp_static
CRYPTOPP_SRCS := $(shell find jni/cryptopp -name '*.cpp' | cut -d '/' -f2- | sort)
CRYPTOPP_EXCL := \
	cryptopp/bench.cpp \
	cryptopp/bench2.cpp \
	cryptopp/cryptlib_bds.cpp \
	cryptopp/datatest.cpp \
	cryptopp/dlltest.cpp \
	cryptopp/fipstest.cpp \
	cryptopp/regtest.cpp \
	cryptopp/test.cpp \
	cryptopp/fipsalgt.cpp \
	cryptopp/pch.cpp \
	cryptopp/validat1.cpp \
	cryptopp/validat2.cpp \
	cryptopp/validat3.cpp
LOCAL_SRC_FILES := $(filter-out $(CRYPTOPP_EXCL), $(CRYPTOPP_SRCS))
LOCAL_EXPORT_CFLAGS := $(LOCAL_CFLAGS)
LOCAL_CPPFLAGS := -Os --visibility=hidden -ffunction-sections -fdata-sections -Wl,-gc-sections -Wl,--icf=safe

include $(BUILD_STATIC_LIBRARY)
