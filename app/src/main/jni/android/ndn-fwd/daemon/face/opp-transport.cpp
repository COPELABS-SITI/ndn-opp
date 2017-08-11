#include "opp-transport.hpp"

#include "daemon/face/face.hpp"
#include "daemon/face/transport.hpp"

#include "ndn-cxx/util/face-uri.hpp"

#include "ndn-cxx/encoding/tlv.hpp"

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

// Used to change the state of this OppTransport. When the state changes to UP, we need to start
// the process of sending out the packets that are currently queued.
void OppTransport::commuteState(TransportState newState) {
    NFD_LOG_DEBUG("Commuting state.");
    this->setState(newState);
    if(newState == TransportState::UP)
        sendNextPacket();
}

// When the OppTransport closes. Part of the Transport interface.
void OppTransport::doClose() {
    // This is the result of an explicit close. Allow current packet transmission to complete ?
    // Disallow new sends to occur through this Face but empty the queue if possible.
    this->close();
}

// Initiates the sending of the next pending packet.
void OppTransport::sendNextPacket() {
    if(!m_sendQueue.empty())
        performSend(this->getFace()->getId(), m_sendQueue.front().packet);
    else
        NFD_LOG_DEBUG("Queue empty.");
}

void OppTransport::removePacket(uint32_t nonce) {
    std::deque<Packet>::iterator it = find_if(m_sendQueue.begin(), m_sendQueue.end(),
    [&nonce] (const Packet &current) {
        // Only Interest packets have a Nonce. Ignore Data packets.
        if(current.packet.type() != ndn::tlv::Interest)
            return false;

        uint32_t currentNonce;
        Block encodedNonce = current.packet.get(ndn::tlv::Nonce);
        if (encodedNonce.value_size() == sizeof(uint32_t))
            currentNonce = *reinterpret_cast<const uint32_t*>(encodedNonce.value());
        else {
            currentNonce = readNonNegativeInteger(encodedNonce);
        }

        NFD_LOG_DEBUG("Packet with Nonce " << currentNonce << " " << nonce);
        return (nonce == currentNonce);
    });

    if(it != m_sendQueue.end()) {
        NFD_LOG_INFO("Found pending packet in " << getFace()->getId());
        m_sendQueue.erase(it);
    }
}

int OppTransport::getQueueSize() {
    return m_sendQueue.size();
}

void OppTransport::onSendComplete(bool succeeded) {
    NFD_LOG_INFO("onSendComplete. Succeeded ? " << succeeded);

    if(succeeded) {
        m_sendQueue.pop_front();
        sendNextPacket();
    } else NFD_LOG_DEBUG("Packet sending failed.");
}

void OppTransport::doSend(Packet&& packet) {
    NFD_LOG_INFO("doSend " << getFace()->getId());

    //if(packet.packet.type() == ndn::tlv::Interest)
        m_sendQueue.push_back(packet);
    //else if (packet.packet.type() == ndn::tlv::Data)
    //    m_dataQueue.push_back(packet);

    TransportState currently = this->getState();
    if(currently == TransportState::UP && m_sendQueue.size() == 1) {
        NFD_LOG_INFO("Transport is UP. Sending.");
        sendNextPacket();
    } else if(currently == TransportState::DOWN)
        NFD_LOG_INFO("Transport is DOWN. Queuing.");
}

void OppTransport::handleReceive(const uint8_t *buffer, size_t buf_size) {
    NFD_LOG_DEBUG("Received: " << buf_size << " bytes");

    bool isOk = true;
    Block element(buffer, buf_size);
    NFD_LOG_DEBUG("Performing actual receive of a Block of " << element.size() << " bytes");
    this->receive(Transport::Packet(std::move(element)));
}

void OppTransport::afterChangePersistency(ndn::nfd::FacePersistency oldP) {
    NFD_LOG_INFO("AfterChangePersistency ignored.");
}

} // namespace face
} // namespace nfd