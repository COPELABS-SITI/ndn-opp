#include "core/logger.hpp"
#include "core/scheduler.hpp"
#include "core/global-io.hpp"

NFD_LOG_INIT("Scheduler");

namespace nfd {
namespace scheduler {

std::unique_ptr<Scheduler> g_scheduler = nullptr;

Scheduler& getGlobalScheduler() {
	if (g_scheduler.get() == nullptr) {
		NFD_LOG_INFO("getGlobalScheduler() : create Scheduler.");
		g_scheduler.reset(new Scheduler(getGlobalIoService()));
	} else
		NFD_LOG_INFO("getGlobalScheduler() : re-using Scheduler.");

	return *g_scheduler;
}

EventId schedule(const time::nanoseconds& after, const Scheduler::Event& event) {
	return getGlobalScheduler().scheduleEvent(after, event);
}

void cancel(const EventId& eventId) {
	getGlobalScheduler().cancelEvent(eventId);
}

void resetGlobalScheduler() {
	if(g_scheduler.get() != nullptr) {
		NFD_LOG_INFO("resetGlobalScheduler() : resetting.");
		g_scheduler.reset();
		g_scheduler = nullptr;
	} else
		NFD_LOG_INFO("resetGlobalScheduler() : already clean.");
}

ScopedEventId::ScopedEventId() {}

ScopedEventId::ScopedEventId(const EventId& event) : m_event(event) {}

ScopedEventId::ScopedEventId(ScopedEventId&& other) : m_event(other.m_event) {
	other.release();
}

ScopedEventId& ScopedEventId::operator=(const EventId& event) {
	if (m_event != event) {
		scheduler::cancel(m_event);
		m_event = event;
	}
	return *this;
}

ScopedEventId::~ScopedEventId() {
	scheduler::cancel(m_event);
}

void ScopedEventId::cancel() {
	scheduler::cancel(m_event);
}

void ScopedEventId::release() {
	m_event.reset();
}

} // namespace scheduler
} // namespace nfd
