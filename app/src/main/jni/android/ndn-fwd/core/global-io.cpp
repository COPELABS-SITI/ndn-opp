#include "core/logger.hpp"
#include "core/global-io.hpp"
#include <boost/thread/tss.hpp>

NFD_LOG_INIT("Global-IO");

namespace nfd {

std::unique_ptr<boost::asio::io_service> g_ioService = nullptr;

boost::asio::io_service& getGlobalIoService() {
	NFD_LOG_INFO("Getting IO service.");
	if (g_ioService.get() == nullptr) {
		NFD_LOG_INFO("Need to create NEW IO Service.");
		g_ioService.reset(new boost::asio::io_service());
	} else
		NFD_LOG_INFO("Re-using existing IO service.");

	return *g_ioService;
}

void resetGlobalIoService() {
	if(g_ioService.get() != nullptr) {
		NFD_LOG_INFO("resetGlobalIoService() : resetting.");
		g_ioService.reset();
		g_ioService = nullptr;
	} else
		NFD_LOG_INFO("resetGlobalIoService() : already clean.");
}
} // namespace nfd
