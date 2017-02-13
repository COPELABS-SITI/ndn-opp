#include "wifi-p2p-transport.hpp"

#include "daemon/face/transport.hpp"

#include "ndn-cxx/util/face-uri.hpp"

namespace nfd {
namespace face {

WifiP2pTransport::WifiP2pTransport(const FaceUri& uri, ndn::nfd::FacePersistency persistency) : Transport() {
    this->setLocalUri(uri);
    this->setRemoteUri(uri);
    this->setState(nfd::face::TransportState::DOWN);
    this->setScope(ndn::nfd::FACE_SCOPE_NON_LOCAL);
    this->setPersistency(persistency);
    this->setLinkType(ndn::nfd::LINK_TYPE_POINT_TO_POINT);
    this->setMtu(MTU_UNLIMITED);
}

void WifiP2pTransport::doClose() {}

void WifiP2pTransport::doSend(Packet&& packet) {}

void WifiP2pTransport::beforeChangePersistency(ndn::nfd::FacePersistency newP) {}

} // namespace face
} // namespace nfd