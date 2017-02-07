#ifndef COPELABS_NFD_ANDROID_WFD_FACTORY_HPP
#define COPELABS_NFD_ANDROID_WFD_FACTORY_HPP

#include "daemon/face/face.hpp"
#include "daemon/face/protocol-factory.hpp"

#include "ndn-cxx/util/face-uri.hpp"

namespace nfd {

class WfdFactory : public ProtocolFactory {
public:
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

#endif // COPELABS_NFD_ANDROID_WFD_FACTORY_HPP
