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

uint32_t extractNonce(const Transport::Packet &pkt) {
    uint32_t nonce;
    Block encodedNonce = pkt.packet.get(ndn::tlv::Nonce);
    if (encodedNonce.value_size() == sizeof(uint32_t))
        nonce = *reinterpret_cast<const uint32_t*>(encodedNonce.value());
    else {
        nonce = readNonNegativeInteger(encodedNonce);
    }
    return nonce;
}

std::string extractName(const Transport::Packet &pkt) {
    Name contentName;
    Block encodedName = pkt.packet.get(ndn::tlv::Name);
    contentName.wireDecode(encodedName);
    return contentName.toUri();
}

// Initiates the sending of the next pending packet.
void OppTransport::sendNextPacket() {
    if(!m_intrQueue.empty()) {
        NFD_LOG_DEBUG("!m_intrQueue.empty()");
        Packet current = m_intrQueue.front();
        m_intrQueue.pop_front();
        transferInterest(this->getFace()->getId(), extractNonce(current), current.packet);
        sendNextPacket();
    } else if(!m_dataQueue.empty()) {
        NFD_LOG_DEBUG("!m_dataQueue.empty()");
        Packet current = m_dataQueue.front();
        m_dataQueue.pop_front();
        transferData(this->getFace()->getId(), extractName(current), current.packet);
        sendNextPacket();
    } else
        NFD_LOG_DEBUG("Queues empty.");
}

int OppTransport::getQueueSize() {
    NFD_LOG_DEBUG("getQueueSize -> m_intrQueue.size(): " << m_intrQueue.size());
    NFD_LOG_DEBUG("getQueueSize -> m_dataQueue.size(): " << m_dataQueue.size());
    return m_intrQueue.size() + m_dataQueue.size();
}

void OppTransport::removeInterest(uint32_t nonce) {
    std::deque<Packet>::iterator it = find_if(m_intrQueue.begin(), m_intrQueue.end(),
        [&nonce] (const Packet &current) {
            return nonce == extractNonce(current);
        });

    if(it != m_intrQueue.end()) {
        NFD_LOG_INFO("Found pending packet in " << getFace()->getId());
        m_intrQueue.erase(it);
    } else
        NFD_LOG_DEBUG("Removing Interest for unknown Nonce (probably duplicate ack)");
}

void OppTransport::removeData(std::string name) {
    std::deque<Packet>::iterator it = find_if(m_dataQueue.begin(), m_dataQueue.end(),
        [&name] (const Packet &current) {
            return name.compare(extractName(current)) == 0;
        });

    if(it != m_dataQueue.end()) {
        NFD_LOG_INFO("Found pending packet in " << getFace()->getId());
        m_dataQueue.erase(it);
    } else
        NFD_LOG_DEBUG("Removing Data for unknown Name (probably duplicate ack).");
}

void OppTransport::onInterestTransferred(uint32_t nonce) {
    NFD_LOG_INFO("onInterestTransferred : " << nonce);
    removeInterest(nonce);
}

void OppTransport::onDataTransferred(std::string name) {
    NFD_LOG_INFO("onDataTransferred : " << name);
    removeData(name);
}

void OppTransport::doSend(Packet&& packet) {
    NFD_LOG_INFO("doSend " << getFace()->getId());

    if(packet.packet.type() == ndn::tlv::Interest)
        m_intrQueue.push_back(packet);
    else if (packet.packet.type() == ndn::tlv::Data)
        m_dataQueue.push_back(packet);

    if(this->getState() == TransportState::UP)
        sendNextPacket();
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