#include "wifi-p2p-factory.hpp"
#include "wifi-p2p-transport.hpp"
#include "daemon/face/face.hpp"
#include "daemon/face/generic-link-service.hpp"
#include "daemon/fw/face-table.hpp"

#include "ndn-cxx/util/face-uri.hpp"

namespace nfd {

NFD_LOG_INIT("WifiP2pFactory");

    void WifiP2pFactory::createFace(
        const FaceUri& uri, ndn::nfd::FacePersistency persistency, bool localFields,
        const FaceCreatedCallback& onCreated, const FaceCreationFailedCallback& onFailure) {
        NFD_LOG_INFO("CreateFace : " << uri);
        unique_ptr<face::GenericLinkService> linkService = make_unique<face::GenericLinkService>();
        unique_ptr<face::WifiP2pTransport> transport = make_unique<face::WifiP2pTransport>(uri, persistency);
        shared_ptr<Face> face = make_shared<Face>(std::move(linkService), std::move(transport));

        //TODO: set the correct Local URI
        onCreated(face);
    }

    std::vector<shared_ptr<const Channel>> WifiP2pFactory::getChannels() const {
        std::vector<shared_ptr<const Channel>> channels;
        channels.reserve(m_channels.size());
        for (const auto& ch : m_channels)
            channels.push_back(ch.second);

        return channels;
    }

} // namespace nfd
