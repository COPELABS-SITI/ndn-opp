#ifndef COPELABS_NFD_ANDROID_OPP_TRANSPORT_HPP
#define COPELABS_NFD_ANDROID_OPP_TRANSPORT_HPP

#include <queue>

#include "daemon/face/transport.hpp"

namespace nfd {
namespace face {

class OppTransport : public Transport {
public:
    OppTransport(const FaceUri& uri);
    void commuteState(TransportState newState);
private:
    virtual void doClose() override;
    virtual void doSend(Packet&& packet) override;
    virtual void beforeChangePersistency(ndn::nfd::FacePersistency newP) override;

private:
    std::queue<Block> m_sendQueue;
};

} // namespace face
} // namespace nfd

#endif //COPELABS_NFD_ANDROID_WIFI_P2P_TRANSPORT_HPP
