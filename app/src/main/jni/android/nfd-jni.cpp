#include <jni.h>
#include <stdlib.h>

#include "core/logger.hpp"
#include "core/version.hpp"
#include "core/global-io.hpp"
#include "core/config-file.hpp"
#include "core/privilege-helper.hpp"

#include "daemon/face/face.hpp"
#include "daemon/nfd-android.hpp"
#include "daemon/fw/face-table.hpp"

#include "rib/service.hpp"

#include <boost/thread.hpp>
#include <boost/asio/io_service.hpp>
#include <boost/property_tree/info_parser.hpp>

NFD_LOG_INIT("NFD-JNI");

namespace nfd {
	namespace scheduler {
		void resetGlobalScheduler();
	}
	void resetGlobalIoService();
}

static boost::asio::io_service* g_io;
static std::unique_ptr<nfd::Nfd> g_nfd;
static std::unique_ptr<nfd::rib::Service> g_nrd;

// Caching of java.util.ArrayList
static jclass list;
static jmethodID newList;
static jmethodID listAdd;
// Caching of pt.ulusofona.copelabs.ndn classes and methods.
static jclass name;
static jmethodID newName;

static jclass face;
static jmethodID newFace;

static jclass fibEntry;
static jmethodID newFibEntry;
static jmethodID addNextHop;

static jclass pitEntry;
static jmethodID newPitEntry;
static jmethodID addInRecord;
static jmethodID addOutRecord;

static jclass sctEntry;
static jmethodID newSctEntry;

static jclass csEntry;
static jmethodID newCsEntry;

void initializeLogging(nfd::ConfigSection& cfg) {
	nfd::ConfigFile config(&nfd::ConfigFile::ignoreUnknownSection);
	nfd::LoggerFactory::getInstance().setConfigFile(config);

	config.parse(cfg, true, "JNI");
	config.parse(cfg, false, "JNI");
}

static void jniInitialize(JNIEnv* env, jclass, jstring homepath, jstring inputConfig) {
	if (g_io == nullptr) {

		std::string home = std::string(env->GetStringUTFChars(homepath, NULL));
		std::string initialConfig = std::string(env->GetStringUTFChars(inputConfig, NULL));
		::setenv("HOME", home.c_str(), true);
		NFD_LOG_INFO("Use [" << home << "] as a security storage");

		NFD_LOG_INFO("Parsing configuration...");
		nfd::ConfigSection config;
		std::istringstream input(initialConfig);
		boost::property_tree::read_info(input, config);

		NFD_LOG_INFO("Initializing Logging.");
		initializeLogging(config);

		NFD_LOG_INFO("Initializing global I/O service.");
		g_io = &nfd::getGlobalIoService();

		NFD_LOG_INFO("Setting NFD.");
		g_nfd.reset(new nfd::Nfd(config));
		NFD_LOG_INFO("Setting NRD.");
		g_nrd.reset(new nfd::rib::Service(config, g_nfd->m_keyChain));

		NFD_LOG_INFO("Initializing NFD.");
		g_nfd->initialize();
		NFD_LOG_INFO("Initializing NRD.");
		g_nrd->initialize();
	}
}

static void jniCleanUp(JNIEnv* env, jclass) {
	NFD_LOG_INFO("Cleaning up NFD...");
	g_nfd.reset(); g_nfd = nullptr;
	g_nrd.reset(); g_nrd = nullptr;
	nfd::scheduler::resetGlobalScheduler();
	nfd::resetGlobalIoService(); g_io = nullptr;
}

static void jniStart(JNIEnv* env, jclass) {
	boost::thread([] {
		try {
			g_io->run();
		} catch (const std::exception& e) {
			NFD_LOG_FATAL("std::exception: " << e.what());
		} catch (const nfd::PrivilegeHelper::Error& e) {
			NFD_LOG_FATAL("PrivilegeHelper: " << e.what());
		} catch (...) {
			NFD_LOG_FATAL("Unknown fatal error");
		}
	});
}

static void jniStop(JNIEnv* env, jclass) {
	if (g_io != nullptr)
		g_io->post( [] {
			NFD_LOG_DEBUG("Stopping I/O service.");
			g_io->stop();
		});
}

static jstring jniGetVersion(JNIEnv* env, jclass) {
	return env->NewStringUTF(NFD_VERSION_BUILD_STRING);
}

static jobject jniGetNameTree(JNIEnv* env, jclass) {
	jobject nametree = env->NewObject(list, newList);

	if(g_nfd.get() != nullptr) {
		for(auto&& entry : g_nfd->getNameTree()) {
			env->CallBooleanMethod(nametree, listAdd,
				env->NewObject(name, newName, env->NewStringUTF(entry.getName().toUri().c_str())));
		}
	}

	return nametree;
}

static jobject jniGetFaceTable(JNIEnv* env, jclass) {
	jobject faceList = env->NewObject(list, newList);

	if (g_nfd.get() != nullptr) {
		for(const nfd::Face& current : g_nfd->getFaceTable()) {
			jobject jface = env->NewObject(face, newFace,
				current.getId(),
				env->NewStringUTF(current.getLocalUri().toString().c_str()),
				env->NewStringUTF(current.getRemoteUri().toString().c_str()),
				(int) current.getScope(),
				(int) current.getPersistency(),
				(int) current.getLinkType(),
				(int) current.getState()
			);

			env->CallBooleanMethod(faceList, listAdd, jface);
		}
	}

	return faceList;
}

static void jniCreateFace(JNIEnv* env, jclass, jstring uri, jint persistency, jboolean localFields) {
	if(g_nfd.get() != nullptr) {
	    NFD_LOG_INFO("CreateFace: " << uri);
		std::string faceUri = std::string(env->GetStringUTFChars(uri, NULL));
		g_nfd->createFace(faceUri, (ndn::nfd::FacePersistency) persistency, (bool) localFields);
	}
}

static void jniDestroyFace(JNIEnv* env, jclass, jlong faceId) {
	if(g_nfd.get() != nullptr) {
		NFD_LOG_INFO("DestroyFace: " << faceId);
		g_nfd->destroyFace(faceId);
	}
}

static jobject jniGetForwardingInformationBase(JNIEnv* env, jclass) {
	jobject fib = env->NewObject(list, newList);

	if(g_nfd.get() != nullptr) {
		for(auto&& entry : g_nfd->getForwardingInformationBase()) {
			jobject jfibEntry = env->NewObject(fibEntry, newFibEntry, env->NewStringUTF(entry.getPrefix().toUri().c_str()));

			for (auto&& next : entry.getNextHops())
				env->CallVoidMethod(jfibEntry, addNextHop, next.getFace().getId(), next.getCost());

			env->CallBooleanMethod(fib, listAdd, jfibEntry);
		}
	}

	return fib;
}

static jobject jniGetPendingInterestTable(JNIEnv* env, jclass) {
	jobject pit = env->NewObject(list, newList);

	if(g_nfd.get() != nullptr) {
		for(auto&& entry : g_nfd->getPendingInterestTable()) {
			jobject jpitEntry = env->NewObject(pitEntry, newPitEntry, env->NewStringUTF(entry.getName().toUri().c_str()));

			for(auto && inEntry : entry.getInRecords())
				env->CallVoidMethod(jpitEntry, addInRecord, inEntry.getFace().getId());
			for(auto && outEntry : entry.getOutRecords())
				env->CallVoidMethod(jpitEntry, addOutRecord, outEntry.getFace().getId());

			env->CallBooleanMethod(pit, listAdd, jpitEntry);
		}
	}

	return pit;
}

static jobject jniGetContentStore(JNIEnv* env, jclass) {
	jobject cs = env->NewObject(list, newList);

	if(g_nfd.get() != nullptr) {
		for(auto&& entry : g_nfd->getContentStore()) {
			std::string sName = entry.getName().toUri();
			std::string sData = ndn::encoding::readString(entry.getData().getContent());
			jobject jCsEntry = env->NewObject(csEntry, newCsEntry,
				env->NewStringUTF(sName.c_str()),
				env->NewString((const jchar*) sData.c_str(), (jsize) sData.length()));
			env->CallBooleanMethod(cs, listAdd, jCsEntry);
		}
	}

	return cs;
}

static jobject jniGetStrategyChoiceTable(JNIEnv* env, jclass) {
	jobject sct = env->NewObject(list, newList);

	if(g_nfd.get() != nullptr) {
		for(auto&& entry : g_nfd->getStrategyChoiceTable()) {
			jobject jstrategy = env->NewObject(sctEntry, newSctEntry,
					env->NewStringUTF(entry.getPrefix().toUri().c_str()),
					env->NewStringUTF(entry.getStrategyName().toUri().c_str()));
			env->CallBooleanMethod(sct, listAdd, jstrategy);
		}
	}

	return sct;
}

static JNINativeMethod nativeMethods[] = {
	{ "jniInitialize", "(Ljava/lang/String;Ljava/lang/String;)V", (void*) jniInitialize },
	{ "jniCleanUp", "()V", (void*) jniCleanUp },
	{ "jniStart", "()V", (void*) jniStart },
	{ "jniStop", "()V", (void*) jniStop },

	{ "jniGetVersion", "()Ljava/lang/String;", (void*) jniGetVersion },
	{ "jniGetNameTree"                  , "()Ljava/util/List;" , (void*) jniGetNameTree },
	{ "jniGetFaceTable"                 , "()Ljava/util/List;" , (void*) jniGetFaceTable },
	{ "jniGetPendingInterestTable"      , "()Ljava/util/List;" , (void*) jniGetPendingInterestTable },
	{ "jniGetForwardingInformationBase" , "()Ljava/util/List;" , (void*) jniGetForwardingInformationBase },
	{ "jniGetStrategyChoiceTable"       , "()Ljava/util/List;" , (void*) jniGetStrategyChoiceTable },
	{ "jniGetContentStore"              , "()Ljava/util/List;" , (void*) jniGetContentStore },

	{ "jniCreateFace", "(Ljava/lang/String;IZ)V", (void*) jniCreateFace },
	{ "jniDestroyFace", "(J)V", (void*) jniDestroyFace }
};

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
		NFD_LOG_DEBUG("JNI version error.");
		return JNI_ERR;
	} else {
		NFD_LOG_DEBUG("Registering Native methods.");
		jclass forwardingDaemon = env->FindClass("pt/ulusofona/copelabs/ndn/android/service/ForwardingDaemon");
		env->RegisterNatives(forwardingDaemon, nativeMethods, sizeof(nativeMethods) / sizeof(JNINativeMethod));
		env->DeleteLocalRef(forwardingDaemon);
		NFD_LOG_DEBUG("Caching JNI classes.");
		list = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));

		name     = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/Name")));
		face     = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/Face")));
		fibEntry = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/FibEntry")));
		pitEntry = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/PitEntry")));
		sctEntry = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/SctEntry")));
		csEntry  = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/CsEntry")));

		newList = env->GetMethodID(list, "<init>", "()V");
		newName = env->GetMethodID(name, "<init>", "(Ljava/lang/String;)V");
		newFace = env->GetMethodID(face, "<init>", "(JLjava/lang/String;Ljava/lang/String;IIII)V");
		newFibEntry = env->GetMethodID(fibEntry, "<init>", "(Ljava/lang/String;)V");
		newPitEntry = env->GetMethodID(pitEntry, "<init>", "(Ljava/lang/String;)V");
		newSctEntry = env->GetMethodID(sctEntry, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
		newCsEntry  = env->GetMethodID(csEntry, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");

		listAdd      = env->GetMethodID(list    , "add"         , "(Ljava/lang/Object;)Z");
		addNextHop   = env->GetMethodID(fibEntry, "addNextHop"  , "(JI)V");
		addInRecord  = env->GetMethodID(pitEntry, "addInRecord" , "(J)V");
		addOutRecord = env->GetMethodID(pitEntry, "addOutRecord", "(J)V");
	}
	return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM* vm, void *reserved) {
	JNIEnv* env;
	if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
		return;
	} else {
		if (0 != NULL) {
			env->DeleteGlobalRef(list);
			env->DeleteGlobalRef(name);
			env->DeleteGlobalRef(face);
			env->DeleteGlobalRef(fibEntry);
			env->DeleteGlobalRef(pitEntry);
			env->DeleteGlobalRef(sctEntry);
			env->DeleteGlobalRef(csEntry);
		}
	}
}
