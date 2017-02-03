#include "wfd-face-manager.hpp"
#include "daemon/face/face.hpp"
#include "daemon/face/generic-link-service.hpp"
#include "daemon/fw/face-table.hpp"

#include "ndn-cxx/util/face-uri.hpp"

namespace nfd {

NFD_LOG_INIT("WfdFaceManager");

WfdFaceManager::WfdFaceManager(FaceTable& faceTable) : m_faceTable(faceTable) {}

const FaceId WfdFaceManager::createFace(std::string& faceUri) {
	NFD_LOG_INFO("createFace.");
	FaceUri uri;
	if (!uri.parse(faceUri)) {
		NFD_LOG_INFO("406 : failed to parse URI.");
		return face::INVALID_FACEID;
	}

	if (!uri.isCanonical()) {
		NFD_LOG_INFO("406 : URI is not canonical.");
		return face::INVALID_FACEID;
	}

	NFD_LOG_INFO("Valid parameters and factory found. Creating face for " << uri.getHost());
	return face::FACEID_RESERVED_MAX;
}

void WfdFaceManager::destroyFace(const FaceId faceId) {
	Face* face = m_faceTable.get(faceId);
	if (face != nullptr)
		face->close();
}

} // namespace nfd
