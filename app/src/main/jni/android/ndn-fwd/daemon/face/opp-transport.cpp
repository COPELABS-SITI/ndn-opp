#include "opp-transport.hpp"

#include "daemon/face/face.hpp"
#include "daemon/face/transport.hpp"

#include "ndn-cxx/encoding/block.hpp"
#include "ndn-cxx/util/face-uri.hpp"

namespace nfd {
namespace face {

NFD_LOG_INIT("OppTransport");

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
    // Perform dequeing by sending.
}

void OppTransport::doClose() {
    // This is the result of an explicit close. Allow current packet transmission to complete ?
    // Disallow new sends to occur through this Face but empty the queue if possible.
}

void OppTransport::doSend(Packet&& packet) {
    NFD_LOG_INFO("doSend " << getFace()->getId());
    TransportState currently = this->getState();
    if(currently == TransportState::UP) {
        NFD_LOG_INFO("Transport is UP.");
        performSend(this->getFace()->getId(), packet.packet);
    } else if(currently == TransportState::DOWN) {
        NFD_LOG_INFO("Transport is DOWN. Queuing.");
        //m_sendQueue.push(packet.packet);
    }
}

void OppTransport::handleReceive(const uint8_t *buffer, size_t buf_size) {
    NFD_LOG_DEBUG("Received: " << buf_size << " bytes");

    bool isOk = true;
    Block element(buffer, buf_size);
    NFD_LOG_DEBUG("Performing actual receive of a Block of " << element.size() << " bytes");
    this->receive(Transport::Packet(std::move(element)));
}

void OppTransport::beforeChangePersistency(ndn::nfd::FacePersistency newP) {
    NFD_LOG_INFO("BeforeChangePersistency ignored.");
}

} // namespace face
} // namespace nfd