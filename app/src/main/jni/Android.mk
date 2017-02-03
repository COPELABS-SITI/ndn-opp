LOCAL_PATH := $(call my-dir)
LOCAL_PATH_SAVED := $(LOCAL_PATH)

# Build the version of NFD wrapped with a JNI interface and a specific Runner for use in Android.
include $(LOCAL_PATH_SAVED)/nfd-wrapped.mk

# Building the CryptOpp shared library.
include $(LOCAL_PATH_SAVED)/cryptopp.mk

# Some import stuff.
$(call import-module,boost/1.59.0)
$(call import-module,sqlite/3)
$(call import-module,openssl/1.0.2h)
