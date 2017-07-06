#include <jni.h>
#include <coffeejni.h>
#include <coffeecatch.h>
#include <stdlib.h>
#include <map>

#include "name.hpp"

#include "core/logger.hpp"
#include "core/version.hpp"
#include "core/global-io.hpp"
#include "core/config-file.hpp"
#include "core/privilege-helper.hpp"

#include "daemon/face/face.hpp"
#include "daemon/face/transport.hpp"
#include "daemon/face/opp-transport.hpp"
#include "daemon/nfd-android.hpp"
#include "daemon/fw/face-table.hpp"

#include "daemon/table/fib.hpp"
#include "daemon/table/fib-entry.hpp"

#include "rib/route.hpp"
#include "rib/rib-update.hpp"
#include "rib/rib-manager.hpp"
#include "rib/service.hpp"

#include "ndn-cxx/encoding/block.hpp"
#include "ndn-cxx/util/time.hpp"

#include <boost/thread.hpp>
#include <boost/asio/io_service.hpp>
#include <boost/property_tree/info_parser.hpp>

NFD_LOG_INIT("NFD-JNI");
// @TODO: clean memory leaks from GetStrings and stuff.

namespace nfd {
	namespace scheduler {
		void resetGlobalScheduler();
	}
	void resetGlobalIoService();
}

static std::unique_ptr<nfd::Nfd> g_nfd;
static std::unique_ptr<nfd::rib::Service> g_nrd;

JavaVM* g_vm;
jobject forwardingDaemonInstance;
// Caching of java.util.ArrayList
static jclass list;
static jmethodID newList;
static jmethodID listAdd;
// Caching of pt.ulusofona.copelabs.ndn classes and methods.
static jclass name;
static jmethodID newName;

static jclass face;
static jmethodID newFace;

static jclass forwardingDaemon;
static jmethodID afterFaceAdded;

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

static jclass cls_opp_channel;
static jmethodID mth_send;

void initializeLogging(nfd::ConfigSection& cfg) {
	nfd::ConfigFile config(&nfd::ConfigFile::ignoreUnknownSection);
	nfd::LoggerFactory::getInstance().setConfigFile(config);

	config.parse(cfg, true, "JNI");
	config.parse(cfg, false, "JNI");
}

std::string convertString(JNIEnv* env, jstring input) {
    const char* str = env->GetStringUTFChars(input, NULL);
    std::string result(str);
    env->ReleaseStringUTFChars(input, str);
    return result;
}

jobject constructFace(JNIEnv* env, const nfd::Face& current) {
    std::string remoteUri = current.getRemoteUri().toString();
    int queueSize = -1;
    // If this is an opportunistic face, get the queueSize.
    if(remoteUri.compare(0, 6, "opp://") == 0) {
        nfd::face::OppTransport* oppTransport = (nfd::face::OppTransport*) current.getTransport();
        queueSize = oppTransport->getQueueSize();
    }

    return env->NewObject(face, newFace,
                                current.getId(),
                                env->NewStringUTF(current.getRemoteUri().toString().c_str()),
                                (int) current.getScope(),
                                (int) current.getPersistency(),
                                (int) current.getLinkType(),
                                (int) current.getState(),
                                queueSize);
}

#define PERFORM_ATTACHED(OPERATIONS) {                            \
    JNIEnv* env;                                                    \
    int envStatus = g_vm->GetEnv((void **) &env, JNI_VERSION_1_6);  \
    bool hadToAttach = false;                                       \
    if(envStatus == JNI_EDETACHED) {                                \
        NFD_LOG_INFO("JNI Environment not attached");               \
        if (g_vm->AttachCurrentThread((JNIEnv **) &env, NULL) != 0) \
            NFD_LOG_INFO("Failed to attach JNI Environment");       \
        else                                                        \
            hadToAttach = true;                                     \
    } else if (envStatus == JNI_EVERSION)                           \
        NFD_LOG_INFO("JNI not supported by Java version");          \
    OPERATIONS;                                                     \
    if(hadToAttach) g_vm->DetachCurrentThread();                    \
}

void afterFaceAdd(const nfd::Face& current) {
    PERFORM_ATTACHED(
        NFD_LOG_INFO("Face added : " << current.getId() << " faceUri=" << current.getRemoteUri());
        env->CallVoidMethod(forwardingDaemonInstance, afterFaceAdded, constructFace(env, current));
    );
}

static void jniStart(JNIEnv* env, jobject fDaemon, jstring homepath, jstring configuration) {
    COFFEE_TRY_JNI(env,
        // Initialization.
        forwardingDaemonInstance = env->NewGlobalRef(fDaemon);

        std::string home = convertString(env, homepath);
        ::setenv("HOME", home.c_str(), true);
        NFD_LOG_INFO("Use [" << home << "] as a security storage");

        NFD_LOG_INFO("Parsing configuration...");
        nfd::ConfigSection config;
        std::istringstream input(convertString(env, configuration));
        boost::property_tree::read_info(input, config);

        NFD_LOG_INFO("Initializing Logging.");
        initializeLogging(config);

        NFD_LOG_INFO("Setting NFD.");
        g_nfd.reset(new nfd::Nfd(config));
        NFD_LOG_INFO("Setting NRD.");
        g_nrd.reset(new nfd::rib::Service(config, g_nfd->m_keyChain));

        NFD_LOG_INFO("Connecting FaceTable.afterAdd signal.");
        g_nfd->getFaceTable().afterAdd.connect(afterFaceAdd);

        NFD_LOG_INFO("Initializing NFD.");
        g_nfd->initialize();
        NFD_LOG_INFO("Initializing NRD.");
        g_nrd->initialize();

        // Actual start.
        boost::thread([] {
            NFD_LOG_INFO("Started secondary thread");
            try {
                COFFEE_TRY() {
                    nfd::getGlobalIoService().run();
                } COFFEE_CATCH() {
                    PERFORM_ATTACHED(coffeecatch_throw_exception(env));
                } COFFEE_END();
            } catch (const std::exception& e) {
                NFD_LOG_FATAL("std::exception: " << e.what());
            } catch (const nfd::PrivilegeHelper::Error& e) {
                NFD_LOG_FATAL("PrivilegeHelper: " << e.what());
            } catch (...) {
                NFD_LOG_FATAL("Unknown fatal error");
            }
        });
    );
}

static void jniStop(JNIEnv* env, jobject) {
    COFFEE_TRY_JNI(env,
        nfd::getGlobalIoService().post( [] {
            NFD_LOG_DEBUG("Stopping I/O service.");
            nfd::getGlobalIoService().stop();

            NFD_LOG_INFO("Cleaning up NFD ...");
            g_nfd->cleanup();
            g_nfd.reset(); g_nfd = nullptr;
            NFD_LOG_INFO("Cleaning up NRD ...");
            g_nrd.reset(); g_nrd = nullptr;

            NFD_LOG_INFO("Resetting Global Scheduler");
            nfd::scheduler::resetGlobalScheduler();
            NFD_LOG_INFO("Resetting Global I/O Service");
            nfd::resetGlobalIoService();
        });
    );
}

static jstring jniGetVersion(JNIEnv* env, jobject) {
	return env->NewStringUTF(NFD_VERSION_BUILD_STRING);
}

static jobject jniGetNameTree(JNIEnv* env, jobject) {
    jobject nametree = env->NewObject(list, newList);

    COFFEE_TRY_JNI(env,
        if(g_nfd.get() != nullptr)
            for(auto&& entry : g_nfd->getNameTree())
                env->CallBooleanMethod(nametree, listAdd,
                    env->NewObject(name, newName, env->NewStringUTF(entry.getName().toUri().c_str())));
    );

	return nametree;
}

static jobject jniGetFaceTable(JNIEnv* env, jobject) {
	jobject faceList = env->NewObject(list, newList);

    COFFEE_TRY_JNI(env,
        if (g_nfd.get() != nullptr)
            for(const nfd::Face& current : g_nfd->getFaceTable())
                env->CallBooleanMethod(faceList, listAdd, constructFace(env, current));
    );

	return faceList;
}

static void jniCreateFace(JNIEnv* env, jobject, jstring uri, jint persistency, jboolean localFields) {
    COFFEE_TRY_JNI(env,
        if(g_nfd.get() != nullptr) {
            std::string faceUri = convertString(env, uri);
            NFD_LOG_INFO("CreateFace: " << faceUri);
            g_nfd->createFace(faceUri, (ndn::nfd::FacePersistency) persistency, (bool) localFields);
        }
    );
}

std::map<long, jobject> m_opportunistic_channels;

void performSend(long faceId, ndn::Block bl) {
    PERFORM_ATTACHED(
        NFD_LOG_INFO("Perform Send from Face : " << faceId << " of " << bl.size() << " bytes.");
        nfd::Face *current = g_nfd->getFaceTable().get(faceId);
        if(current != nullptr && current->getTransport()->getState() == nfd::face::TransportState::UP) {
            jobject oppChannel = m_opportunistic_channels.find(faceId)->second;
            jbyteArray packetBytes = env->NewByteArray(bl.size());
            if(packetBytes != NULL) {
                NFD_LOG_INFO("Attempting to map ByteArray region.");
                env->SetByteArrayRegion(packetBytes, 0, bl.size(), (const jbyte*) bl.wire());
                NFD_LOG_INFO("Calling actual mth_send.");
                env->CallVoidMethod(oppChannel, mth_send, packetBytes);
            } else
                NFD_LOG_WARN("Cannot allocate buffer for sending Block.");
        }
    );
}

static void jniSendComplete(JNIEnv* env, jobject, jlong faceId, jboolean result) {
    COFFEE_TRY_JNI(env,
        if(g_nfd.get() != nullptr) {
            nfd::Face *current = g_nfd->getFaceTable().get(faceId);
            if(current != nullptr) {
                nfd::face::OppTransport* oppTransport = (nfd::face::OppTransport*) current->getTransport();
                oppTransport->onSendComplete(result);

            } else
                NFD_LOG_ERROR("Could not retrieve face #" << faceId);
        }
    );
}

static void jniReceiveOnFace(JNIEnv* env, jobject, jlong faceId, jint receivedBytes, jbyteArray buffer) {
    COFFEE_TRY_JNI(env,
        NFD_LOG_DEBUG("Receive on Face " << faceId << " buffer=" << buffer << ", receivedBytes=" << (int) receivedBytes);
        if(g_nfd.get() != nullptr) {
            nfd::Face *current = g_nfd->getFaceTable().get(faceId);
            if(current != nullptr) {
                jbyte* nativeCopy = env->GetByteArrayElements(buffer, 0);
                NFD_LOG_DEBUG("Passing buffer to face #" << faceId);
                nfd::face::OppTransport* oppTransport = (nfd::face::OppTransport*) current->getTransport();
                oppTransport->handleReceive((uint8_t*) nativeCopy, (size_t) receivedBytes);
                env->ReleaseByteArrayElements(buffer, nativeCopy, 0);
            } else
                NFD_LOG_ERROR("Could not retrieve face #" << faceId);
        }
    );
}

static void jniBringUpFace(JNIEnv* env, jobject, jlong faceId, jobject oppChannel) {
    COFFEE_TRY_JNI(env,
        if(g_nfd.get() != nullptr) {
            nfd::Face* current = g_nfd->getFaceTable().get(faceId);
            if(current != nullptr) {
                nfd::face::OppTransport* oppT = (nfd::face::OppTransport*) current->getTransport();
                if(oppT->getState() == nfd::face::TransportState::DOWN) {
                    // Associate faceId to oppChannel so that it is used when OppTransport sends.
                    // Also when a packet is received it should be passed through that Transport.
                    NFD_LOG_INFO("Associating OppChannel to face #" << faceId);
                    m_opportunistic_channels[faceId] = env->NewGlobalRef(oppChannel);
                    NFD_LOG_INFO("Commuting transport state of face #" << faceId << " to UP.");
                    oppT->commuteState(nfd::face::TransportState::UP);
                }
            }
        }
    );
}

static void jniBringDownFace(JNIEnv* env, jobject, jlong faceId) {
    COFFEE_TRY_JNI(env,
        if(g_nfd.get() != nullptr) {
            nfd::Face* current = g_nfd->getFaceTable().get(faceId);
            if(current != nullptr) {
                nfd::face::OppTransport* oppT = (nfd::face::OppTransport*) current->getTransport();
                if(oppT->getState() == nfd::face::TransportState::UP) {
                    NFD_LOG_INFO("Commuting transport state of face #" << faceId << " to DOWN.");
                    oppT->commuteState(nfd::face::TransportState::DOWN);
                    NFD_LOG_INFO("Detaching OppChannel from face #" << faceId);
                    jobject oppChannel = m_opportunistic_channels.find(faceId)->second;
                    env->DeleteGlobalRef(oppChannel);
                }
            }
        }
    );
}

static void jniDestroyFace(JNIEnv* env, jobject, jlong faceId) {
    COFFEE_TRY_JNI(env,
        if(g_nfd.get() != nullptr) {
            NFD_LOG_INFO("DestroyFace: " << faceId);
            g_nfd->destroyFace(faceId);
        }
    );
}

static jobject jniGetForwardingInformationBase(JNIEnv* env, jobject) {
	jobject fib = env->NewObject(list, newList);

    COFFEE_TRY_JNI(env,
        if(g_nfd.get() != nullptr) {
            for(auto&& entry : g_nfd->getForwardingInformationBase()) {
                jobject jfibEntry = env->NewObject(fibEntry, newFibEntry, env->NewStringUTF(entry.getPrefix().toUri().c_str()));

                for (auto&& next : entry.getNextHops())
                    env->CallVoidMethod(jfibEntry, addNextHop, next.getFace().getId(), next.getCost());

                env->CallBooleanMethod(fib, listAdd, jfibEntry);
            }
        }
    );

	return fib;
}

void onRibUpdateSuccess(const nfd::rib::RibUpdate& update) {
    g_nrd->m_ribManager->onRibUpdateSuccess(update);
}

void onRibUpdateFailure(const nfd::rib::RibUpdate& update, uint32_t code, const std::string& error) {
    g_nrd->m_ribManager->onRibUpdateFailure(update, code, error);
}

static void jniAddRoute(JNIEnv* env, jobject, jstring prefix, jlong faceId, jlong origin, jlong cost, jlong flags) {
    COFFEE_TRY_JNI(env,
        if(g_nrd.get() != nullptr) {
            nfd::rib::Route route;
            route.faceId = faceId; route.origin = origin; route.cost = cost; route.flags = flags;
            route.expires = ndn::time::steady_clock::TimePoint::max();

            ndn::Name routePrefix = ndn::Name(convertString(env, prefix));
            NFD_LOG_INFO("Adding route " << routePrefix.toUri()
                                         << " faceId=" << route.faceId
                                         << " origin=" << route.origin
                                         << " cost=" << route.cost);

            nfd::rib::RibUpdate update;
            update.setAction(nfd::rib::RibUpdate::REGISTER).setName(routePrefix).setRoute(route);

            g_nrd->m_ribManager->m_rib.beginApplyUpdate(update,
                std::bind(&onRibUpdateSuccess, update),
                std::bind(&onRibUpdateFailure, update, _1, _2));

            // @TODO: this causes SIGBUS (code 1: addr. algn.) on Android. [ = std::set::insert(..)]
            //g_nrd->m_ribManager->m_registeredFaces.insert(faceId);
        }
    );
}

static jobject jniGetPendingInterestTable(JNIEnv* env, jobject) {
	jobject pit = env->NewObject(list, newList);

    COFFEE_TRY_JNI(env,
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
    );

	return pit;
}

static jobject jniGetContentStore(JNIEnv* env, jobject) {
	jobject cs = env->NewObject(list, newList);

    COFFEE_TRY_JNI(env,
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
    );

	return cs;
}

static jobject jniGetStrategyChoiceTable(JNIEnv* env, jobject) {
	jobject sct = env->NewObject(list, newList);

    COFFEE_TRY_JNI(env,
        if(g_nfd.get() != nullptr) {
            for(auto&& entry : g_nfd->getStrategyChoiceTable()) {
                jobject jstrategy = env->NewObject(sctEntry, newSctEntry,
                        env->NewStringUTF(entry.getPrefix().toUri().c_str()),
                        env->NewStringUTF(entry.getStrategyInstanceName().toUri().c_str()));
                env->CallBooleanMethod(sct, listAdd, jstrategy);
            }
        }
    );

	return sct;
}

static JNINativeMethod nativeMethods[] = {
//	{ "jniInitialize", "(Ljava/lang/String;Ljava/lang/String;)V", (void*) jniInitialize },
//	{ "jniCleanUp", "()V", (void*) jniCleanUp },
	{ "jniStart", "(Ljava/lang/String;Ljava/lang/String;)V", (void*) jniStart },
	{ "jniStop", "()V", (void*) jniStop },

	{ "jniGetVersion", "()Ljava/lang/String;", (void*) jniGetVersion },
	{ "jniGetNameTree"                  , "()Ljava/util/List;" , (void*) jniGetNameTree },
	{ "jniGetFaceTable"                 , "()Ljava/util/List;" , (void*) jniGetFaceTable },
	{ "jniGetPendingInterestTable"      , "()Ljava/util/List;" , (void*) jniGetPendingInterestTable },
	{ "jniGetForwardingInformationBase" , "()Ljava/util/List;" , (void*) jniGetForwardingInformationBase },
	{ "jniGetStrategyChoiceTable"       , "()Ljava/util/List;" , (void*) jniGetStrategyChoiceTable },
	{ "jniGetContentStore"              , "()Ljava/util/List;" , (void*) jniGetContentStore },

	{ "jniCreateFace", "(Ljava/lang/String;IZ)V", (void*) jniCreateFace },
	{ "jniBringUpFace", "(JLpt/ulusofona/copelabs/ndn/android/umobile/OpportunisticChannel;)V", (void*) jniBringUpFace },
	{ "jniBringDownFace", "(J)V", (void*) jniBringDownFace },
	{ "jniDestroyFace", "(J)V", (void*) jniDestroyFace },
	{ "jniReceiveOnFace", "(JI[B)V", (void*) jniReceiveOnFace },
    { "jniSendComplete", "(JZ)V", (void*) jniSendComplete },

	{ "jniAddRoute", "(Ljava/lang/String;JJJJ)V", (void*) jniAddRoute }
};

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_vm = vm;
	JNIEnv* env;
	if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
		NFD_LOG_DEBUG("JNI version error.");
		return JNI_ERR;
	} else {
		NFD_LOG_DEBUG("Registering Native methods.");
		forwardingDaemon = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/umobile/OpportunisticDaemon")));
		env->RegisterNatives(forwardingDaemon, nativeMethods, sizeof(nativeMethods) / sizeof(JNINativeMethod));

		NFD_LOG_DEBUG("Caching JNI classes.");
		list = static_cast<jclass>(env->NewGlobalRef(env->FindClass("java/util/ArrayList")));

		name     = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/models/Name")));
		face     = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/models/Face")));
		fibEntry = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/models/FibEntry")));
		pitEntry = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/models/PitEntry")));
		sctEntry = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/models/SctEntry")));
		csEntry  = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/models/CsEntry")));

        cls_opp_channel = static_cast<jclass>(env->NewGlobalRef(env->FindClass("pt/ulusofona/copelabs/ndn/android/umobile/OpportunisticChannel")));

		newList = env->GetMethodID(list, "<init>", "()V");
		newName = env->GetMethodID(name, "<init>", "(Ljava/lang/String;)V");
		newFace = env->GetMethodID(face, "<init>", "(JLjava/lang/String;IIIII)V");
		newFibEntry = env->GetMethodID(fibEntry, "<init>", "(Ljava/lang/String;)V");
		newPitEntry = env->GetMethodID(pitEntry, "<init>", "(Ljava/lang/String;)V");
		newSctEntry = env->GetMethodID(sctEntry, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");
		newCsEntry  = env->GetMethodID(csEntry, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");

        afterFaceAdded = env->GetMethodID(forwardingDaemon, "afterFaceAdded", "(Lpt/ulusofona/copelabs/ndn/android/models/Face;)V");

		listAdd      = env->GetMethodID(list    , "add"         , "(Ljava/lang/Object;)Z");
		addNextHop   = env->GetMethodID(fibEntry, "addNextHop"  , "(JI)V");
		addInRecord  = env->GetMethodID(pitEntry, "addInRecord" , "(J)V");
		addOutRecord = env->GetMethodID(pitEntry, "addOutRecord", "(J)V");

		mth_send = env->GetMethodID(cls_opp_channel, "send", "([B)V");
	}
	return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM* vm, void *reserved) {
	JNIEnv* env;
	if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
		return;
	} else {
		if (0 != NULL) {
            env->DeleteGlobalRef(forwardingDaemon);
			env->DeleteGlobalRef(list);
			env->DeleteGlobalRef(name);
			env->DeleteGlobalRef(face);
			env->DeleteGlobalRef(fibEntry);
			env->DeleteGlobalRef(pitEntry);
			env->DeleteGlobalRef(sctEntry);
			env->DeleteGlobalRef(csEntry);
			env->DeleteGlobalRef(cls_opp_channel);
		}
	}
}
