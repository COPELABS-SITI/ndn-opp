#include "nfd-android.hpp"

#include "core/config-file.hpp"
#include "core/logger-factory.hpp"
#include "core/privilege-helper.hpp"

#include "mgmt/general-config-section.hpp"
#include "mgmt/tables-config-section.hpp"

#include "daemon/face/protocol-factory.hpp"

#include "daemon/mgmt/fib-manager.hpp"
#include "daemon/mgmt/face-manager.hpp"
#include "daemon/mgmt/command-authenticator.hpp"

#include "face/null-face.hpp"
#include "face/internal-face.hpp"

#include <ndn-cxx/security/key-chain.hpp>

namespace nfd {

NFD_LOG_INIT("NFD-Android");

Nfd::Nfd(ConfigSection& config) : m_config(config) , m_keyChain(), m_forwarder() {}

Nfd::~Nfd() = default;

void Nfd::initialize() {
	FaceTable& faceTable = m_forwarder.getFaceTable();

	NFD_LOG_INFO("Creating INTERNAL face.");
	std::tie(m_internalFace, m_internalClientFace) = face::makeInternalFace(m_keyChain);
	NFD_LOG_INFO("Adding INTERNAL face to table.");
	faceTable.addReserved(m_internalFace, face::FACEID_INTERNAL_FACE);

	NFD_LOG_INFO("Creating Dispatcher & Authenticator.");
	m_dispatcher.reset(new ndn::mgmt::Dispatcher(*m_internalClientFace, m_keyChain));
	m_authenticator = CommandAuthenticator::create();

	NFD_LOG_INFO("Creating FaceManager & FibManager.");
	m_faceManager.reset(new FaceManager(m_forwarder.getFaceTable(), *m_dispatcher, *m_authenticator));
	m_fibManager.reset(new FibManager(m_forwarder.getFib(), m_forwarder.getFaceTable(), *m_dispatcher, *m_authenticator));

	NFD_LOG_INFO("Setting the configuration files.");
	ConfigFile config(&ConfigFile::ignoreUnknownSection);
	general::setConfigFile(config);

	TablesConfigSection tablesConfig(m_forwarder);
	tablesConfig.setConfigFile(config);

	m_authenticator->setConfigFile(config);
	m_faceManager->setConfigFile(config);

	NFD_LOG_INFO("Parsing.");
	config.parse(m_config, true, "NFD");
	config.parse(m_config, false, "NFD");

	tablesConfig.ensureConfigured();

	NFD_LOG_INFO("Registering /localhost/nfd for RIB service.");
	Name topPrefix("/localhost/nfd");
	m_forwarder.getFib().insert(topPrefix).first->addNextHop(*m_internalFace, 0);
	m_dispatcher->addTopPrefix(topPrefix, false);

	NFD_LOG_INFO("Creating reserved faces (null, contentstore)");
	faceTable.addReserved(face::makeNullFace(), face::FACEID_NULL);
	faceTable.addReserved(face::makeNullFace(FaceUri("contentstore://")), face::FACEID_CONTENT_STORE);
}

void Nfd::createFace(std::string& faceUri, ndn::nfd::FacePersistency persistency, bool localFields) {
	NFD_LOG_INFO("FaceManager::createFace.");
	FaceUri uri;
	if (!uri.parse(faceUri)) {
		NFD_LOG_INFO("406 : failed to parse URI.");
		return;
	}

	if (!uri.isCanonical()) {
		NFD_LOG_INFO("406 : URI is not canonical.");
		return;
	}

	auto factory = m_faceManager->m_factories.find(uri.getScheme());
	if (factory == m_faceManager->m_factories.end()) {
		NFD_LOG_INFO("406 : received create request for unsupported protocol");
		return;
	}

	NFD_LOG_INFO("Valid parameters and factory found. Attempting creation.");
	try {
		factory->second->createFace(uri, persistency, localFields,
				bind(&Nfd::afterCreateFaceSuccess, this, localFields, _1),
				bind(&Nfd::afterCreateFaceFailure, this, _1, _2));
	}
	catch (const std::runtime_error& error) {
		NFD_LOG_ERROR("500 : Face creation failed: " << error.what());
	}
	catch (const std::logic_error& error) {
		NFD_LOG_ERROR("500 : Face creation failed: " << error.what());
	}
}

void Nfd::afterCreateFaceSuccess(bool localFields, const shared_ptr<Face>& face) {
	NFD_LOG_INFO("Face created : localFieldsEnabled=" << localFields);
	if(face->getScope() == ndn::nfd::FACE_SCOPE_LOCAL || !localFields)
		m_forwarder.getFaceTable().add(face);
}

void Nfd::afterCreateFaceFailure(uint32_t status, const std::string& reason) {
	NFD_LOG_INFO("Face creation failed [" << status << "] : " << reason);
}

void Nfd::destroyFace(FaceId id) {
	Face* face = m_forwarder.getFaceTable().get(id);
	if (face != nullptr)
		face->close();
}

NameTree& Nfd::getNameTree() {
	return m_forwarder.getNameTree();
}

FaceTable& Nfd::getFaceTable() {
	return m_forwarder.getFaceTable();
}

Pit& Nfd::getPendingInterestTable() {
	return m_forwarder.getPit();
}

Fib& Nfd::getForwardingInformationBase() {
	return m_forwarder.getFib();
}

Cs& Nfd::getContentStore() {
	return m_forwarder.getCs();
}

StrategyChoice& Nfd::getStrategyChoiceTable() {
	return m_forwarder.getStrategyChoice();
}

} // namespace nfd
