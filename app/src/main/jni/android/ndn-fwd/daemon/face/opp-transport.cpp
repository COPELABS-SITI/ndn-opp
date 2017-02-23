#include "opp-transport.hpp"

#include "daemon/face/transport.hpp"

#include "ndn-cxx/util/face-uri.hpp"

namespace nfd {
namespace face {

OppTransport::OppTransport(const FaceUri& uri) : Transport() {
    this->setLocalUri(uri);
    this->setRemoteUri(uri);
    this->setState(nfd::face::TransportState::DOWN);
    this->setScope(ndn::nfd::FACE_SCOPE_NON_LOCAL);
    this->setPersistency(ndn::nfd::FacePersistency::FACE_PERSISTENCY_PERMANENT);
    this->setLinkType(ndn::nfd::LINK_TYPE_POINT_TO_POINT);
    this->setMtu(MTU_UNLIMITED);
}

void OppTransport::commuteState(TransportState newState) {
    this->setState(newState);
    // This should also assign the Socket (or whatever is used for communications)
}

void OppTransport::doClose() {
    // This is the result of an explicit close. Allow current packet transmission to complete ?
    // Disallow new sends to occur through this Face but empty the queue if possible.
}

void OppTransport::doSend(Packet&& packet) {
    // Pass the packet to the Socket obtained from Java ?
}

void OppTransport::beforeChangePersistency(ndn::nfd::FacePersistency newP) {
    // Persistency change ignored.
}

} // namespace face
} // namespace nfd