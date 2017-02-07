#ifndef COPELABS_NFD_ANDROID_WFD_TRANSPORT_HPP
#define COPELABS_NFD_ANDROID_WFD_TRANSPORT_HPP

#include "daemon/face/transport.hpp"

namespace nfd {
namespace face {

class WfdTransport : public Transport {
public:
    WfdTransport(const FaceUri& uri, ndn::nfd::FacePersistency persistency);
private:
    virtual void doClose() override;
    virtual void doSend(Packet&& packet) override;
    virtual void beforeChangePersistency(ndn::nfd::FacePersistency newP) override;
};

} // namespace face
} // namespace nfd

#endif //COPELABS_NFD_ANDROID_WFD_TRANSPORT_HPP
