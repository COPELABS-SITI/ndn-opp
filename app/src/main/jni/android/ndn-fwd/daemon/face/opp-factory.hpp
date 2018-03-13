#ifndef COPELABS_NFD_ANDROID_OPP_FACTORY_HPP
#define COPELABS_NFD_ANDROID_OPP_FACTORY_HPP

#include "daemon/face/face.hpp"
#include "daemon/face/protocol-factory.hpp"

#include <atomic>

#include "ndn-cxx/util/face-uri.hpp"

namespace nfd {

class OppFactory : public ProtocolFactory {
public:
    static const std::string& getId();

    void processConfig(OptionalConfigSection configSection,
                       FaceSystem::ConfigContext& context) override;

    virtual void createFace(
        const FaceUri& uri,
        ndn::nfd::FacePersistency persistency,
        bool localFields,
        const FaceCreatedCallback& onCreated,
        const FaceCreationFailedCallback& onFailure) override;

    virtual std::vector<shared_ptr<const Channel>> getChannels() const override;

private:
    std::map<long, shared_ptr<Channel>> m_channels;
};

} // namespace nfd

#endif // COPELABS_NFD_ANDROID_WIFI_P2P_FACTORY_HPP
