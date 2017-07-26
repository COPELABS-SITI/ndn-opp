#ifndef COPELABS_NFD_ANDROID_OPP_TRANSPORT_HPP
#define COPELABS_NFD_ANDROID_OPP_TRANSPORT_HPP

#include <deque>

#include "daemon/face/transport.hpp"

// From nfd-jni.cpp
void performSend(long, ndn::Block);

namespace nfd {
namespace face {

// The OppTransport implements the logic of queueing and sending out packets based on whether the corresponding neighbor peer
// is within transmission range or not.
class OppTransport : public Transport {
public:
    OppTransport(const FaceUri& uri);
    void commuteState(TransportState newState);
    void handleReceive(const uint8_t *buffer, size_t buf_size);
    void sendNextPacket();
    void onSendComplete(bool succeeded);

    int getQueueSize();
    void removePacket(uint32_t nonce);

private:
    virtual void doClose() override;
    virtual void doSend(Packet&& packet) override;
    virtual void afterChangePersistency(ndn::nfd::FacePersistency oldP) override;

private:
    std::deque<Packet> m_sendQueue;
};

} // namespace face
} // namespace nfd

#endif //COPELABS_NFD_ANDROID_WIFI_P2P_TRANSPORT_HPP
