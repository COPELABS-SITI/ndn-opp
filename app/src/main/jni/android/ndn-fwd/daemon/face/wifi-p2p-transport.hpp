#ifndef COPELABS_NFD_ANDROID_WIFI_P2P_TRANSPORT_HPP
#define COPELABS_NFD_ANDROID_WIFI_P2P_TRANSPORT_HPP

#include "daemon/face/transport.hpp"

namespace nfd {
namespace face {

class WifiP2pTransport : public Transport {
public:
    WifiP2pTransport(const FaceUri& uri, ndn::nfd::FacePersistency persistency);
private:
    virtual void doClose() override;
    virtual void doSend(Packet&& packet) override;
    virtual void beforeChangePersistency(ndn::nfd::FacePersistency newP) override;
};

} // namespace face
} // namespace nfd

#endif //COPELABS_NFD_ANDROID_WIFI_P2P_TRANSPORT_HPP
