#include "wfd-transport.hpp"

#include "daemon/face/transport.hpp"

#include "ndn-cxx/util/face-uri.hpp"

namespace nfd {
namespace face {

WfdTransport::WfdTransport(const FaceUri& uri, ndn::nfd::FacePersistency persistency) : Transport() {
    this->setLocalUri(uri);
    this->setRemoteUri(uri);
    this->setState(nfd::face::TransportState::DOWN);
    this->setScope(ndn::nfd::FACE_SCOPE_NON_LOCAL);
    this->setPersistency(persistency);
    this->setLinkType(ndn::nfd::LINK_TYPE_POINT_TO_POINT);
    this->setMtu(MTU_UNLIMITED);
}

void WfdTransport::doClose() {}

void WfdTransport::doSend(Packet&& packet) {}

void WfdTransport::beforeChangePersistency(ndn::nfd::FacePersistency newP) {}

} // namespace face
} // namespace nfd