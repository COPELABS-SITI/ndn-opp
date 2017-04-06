#ifndef NFD_DAEMON_NFD_HPP
#define NFD_DAEMON_NFD_HPP

#include "ndn-cxx/face.hpp"

#include "daemon/fw/forwarder.hpp"
#include "daemon/fw/face-table.hpp"

#include "daemon/table/pit.hpp"
#include "daemon/table/fib.hpp"

#include "daemon/mgmt/fib-manager.hpp"
#include "daemon/mgmt/face-manager.hpp"
#include "daemon/mgmt/strategy-choice-manager.hpp"
#include "daemon/mgmt/forwarder-status-manager.hpp"
#include "daemon/mgmt/command-authenticator.hpp"

#include <ndn-cxx/security/key-chain.hpp>

namespace nfd {

class Nfd : noncopyable {
public:
	Nfd(ConfigSection& config);
	~Nfd();

	void initialize();

    void createFace(std::string& uri, ndn::nfd::FacePersistency persistency, bool localFields);
    void afterCreateFaceSuccess(bool localFields, const shared_ptr<Face>& face);
    void afterCreateFaceFailure(uint32_t status, const std::string& reason);
    void destroyFace(FaceId id);

	NameTree& getNameTree();
	FaceTable& getFaceTable();
	Fib& getForwardingInformationBase();
	Pit& getPendingInterestTable();
	Cs& getContentStore();
	StrategyChoice& getStrategyChoiceTable();

	ndn::KeyChain m_keyChain;
private:
	ConfigSection m_config;
	Forwarder m_forwarder;
	unique_ptr<FibManager> m_fibManager;
	unique_ptr<FaceManager> m_faceManager;
	unique_ptr<ForwarderStatusManager> m_forwarderStatusManager;
	unique_ptr<StrategyChoiceManager> m_strategyChoiceManager;

	shared_ptr<face::Face> m_internalFace;
	shared_ptr<ndn::Face> m_internalClientFace;
	shared_ptr<ndn::mgmt::Dispatcher> m_dispatcher;
	shared_ptr<nfd::CommandAuthenticator> m_authenticator;
};

} // namespace nfd

#endif // NFD_DAEMON_NFD_HPP
