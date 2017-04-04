#include "opp-factory.hpp"
#include "opp-transport.hpp"

#include "daemon/face/face.hpp"
#include "daemon/face/generic-link-service.hpp"

#include "daemon/fw/face-table.hpp"

#include "ndn-cxx/util/face-uri.hpp"

namespace nfd {

NFD_LOG_INIT("OppFactory");
NFD_REGISTER_PROTOCOL_FACTORY(OppFactory);

    const std::string& OppFactory::getId() {
      static std::string id("opp");
      return id;
    }

    void OppFactory::processConfig(OptionalConfigSection configSection,
                                   FaceSystem::ConfigContext& context) {
        providedSchemes.insert("opp");
    }

    void OppFactory::createFace(const FaceUri& uri,
                                ndn::nfd::FacePersistency persistency,
                                bool localFields,
                                const FaceCreatedCallback& onCreated,
                                const FaceCreationFailedCallback& onFailure)
    {
        NFD_LOG_INFO("CreateFace : " << uri);
        unique_ptr<face::GenericLinkService> linkService = make_unique<face::GenericLinkService>();
        unique_ptr<face::OppTransport> transport = make_unique<face::OppTransport>(uri);
        shared_ptr<Face> face = make_shared<Face>(std::move(linkService), std::move(transport));

        //TODO: set the correct Local URI
        onCreated(face);
    }

    std::vector<shared_ptr<const Channel>> OppFactory::getChannels() const {
        std::vector<shared_ptr<const Channel>> channels;
        channels.reserve(m_channels.size());
        for (const auto& ch : m_channels)
            channels.push_back(ch.second);

        return channels;
    }

} // namespace nfd
