# Build the version of NFD wrapped with a JNI interface and a specific Runner for use in Android.
include $(CLEAR_VARS)

LOCAL_MODULE := nfd-wrapped

BOOST_LIBS = $(addprefix boost_,system filesystem date_time chrono random thread regex)
LOCAL_STATIC_LIBRARIES := $(addsuffix _static,$(BOOST_LIBS) cryptopp opencrypto openssl sqlite3)

NDNCXX := ndn-cxx/src
NDNFWD := ndn-fwd

SOURCE_DIRECTORIES := $(addprefix jni/, android $(NDNCXX) $(NDNFWD)/core $(NDNFWD)/daemon $(NDNFWD)/rib)

SOURCE_FILES := $(shell find $(SOURCE_DIRECTORIES) -name "*.cpp" | cut -d '/' -f2- | sort)
EXCLUDED_FILES := \
	$(NDNCXX)/mgmt/dispatcher.cpp \
	$(NDNCXX)/security/sec-tpm-osx.cpp \
	$(NDNCXX)/util/dummy-client-face.cpp \
	$(NDNCXX)/util/logger.cpp \
	$(NDNCXX)/util/logging.cpp \
	$(NDNCXX)/util/network-monitor.cpp \
	$(NDNCXX)/util/detail/network-monitor-impl-osx.cpp \
	$(NDNCXX)/util/detail/network-monitor-impl-rtnl.cpp \
	$(NDNFWD)/core/logger.cpp \
	$(NDNFWD)/core/global-io.cpp \
	$(NDNFWD)/core/scheduler.cpp \
	$(NDNFWD)/core/logger-factory.cpp \
	$(NDNFWD)/daemon/nfd.cpp \
	$(NDNFWD)/daemon/main.cpp \
	$(NDNFWD)/daemon/face/ethernet-factory.cpp \
	$(NDNFWD)/daemon/face/ethernet-transport.cpp \
	$(NDNFWD)/daemon/face/unix-stream-channel.cpp \
	$(NDNFWD)/daemon/face/unix-stream-factory.cpp \
	$(NDNFWD)/daemon/face/unix-stream-transport.cpp \
	$(NDNFWD)/daemon/mgmt/forwarder-status-manager.cpp \
	$(NDNFWD)/daemon/mgmt/strategy-choice-manager.cpp \
)

COFFEE_FILES := $(addprefix android/coffeecatch/, coffeecatch.c coffeejni.c)
LOCAL_SRC_FILES := $(filter-out $(EXCLUDED_FILES), $(COFFEE_FILES) $(SOURCE_FILES))

LD_OPTFLAGS := -Wl,-gc-sections -Wl,--icf=safe
CPP_OPTFLAGS := -Os --visibility=hidden -ffunction-sections -fdata-sections
COFFEECATCH_FLAGS := -funwind-tables -Wl,--no-merge-exidx-entries
INCLUDES := $(addprefix -Ijni/, android android/coffeecatch android/ndn-cxx android/ndn-fwd include include/ndn-cxx $(NDNFWD) $(NDNFWD)/daemon $(NDNFWD)/rib $(NDNFWD)/websocketpp)
LOCAL_CPPFLAGS := $(INCLUDES) $(CPP_OPTFLAGS) $(COFFEECATCH_FLAGS)
LOCAL_LDLIBS := -llog $(LD_OPTFLAGS)

include $(BUILD_SHARED_LIBRARY)
