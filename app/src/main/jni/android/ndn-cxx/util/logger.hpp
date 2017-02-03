#ifndef ANDROID_LOGGER_HPP
#define ANDROID_LOGGER_HPP

#include <ndn-cxx/common.hpp>
#include <atomic>
#include <android/log.h>

namespace ndn {
namespace util {

enum class LogLevel {
	FATAL   = -1,   ///< fatal (will be logged unconditionally)
	NONE    = 0,    ///< no messages
	ERROR   = 1,    ///< serious error messages
	WARN    = 2,    ///< warning messages
	INFO    = 3,    ///< informational messages
	DEBUG   = 4,    ///< debug messages
	TRACE   = 5,    ///< trace messages (most verbose)
	ALL     = 255   ///< all messages
};

std::ostream& operator<<(std::ostream& os, LogLevel level);
LogLevel parseLogLevel(const std::string& s);

class Logger {
public:
	explicit Logger(const std::string& name);

	const std::string& getModuleName() const {
		return m_moduleName;
	}

	bool isLevelEnabled(LogLevel level) const {
		return m_currentLevel.load(std::memory_order_relaxed) >= level;
	}

	void setLevel(LogLevel level) {
		m_currentLevel.store(level, std::memory_order_relaxed);
	}

private:
	const std::string m_moduleName;
	std::atomic<LogLevel> m_currentLevel;
};

#ifdef ENABLE_LOGGING
#define NDN_LOG_INIT(name) \
		namespace { \
	inline ::ndn::util::Logger& getNdnCxxLogger() \
	{ \
			static ::ndn::util::Logger logger(BOOST_STRINGIZE(name)); \
			return logger; \
	} \
} \
struct ndn_cxx__allow_trailing_semicolon

#define NDN_LOG(level, androidLevel, msg, expression)   \
		do { \
			if (getNdnCxxLogger().isLevelEnabled(::ndn::util::LogLevel::level)) {           \
				std::ostringstream os;                                              \
				os << expression;                                                   \
				__android_log_print(ANDROID_LOG_##androidLevel,                     \
						getNdnCxxLogger().getModuleName().c_str(), "%s", os.str().c_str()); \
			}                                                                     \
		} while (false)
#else
#define NDN_LOG_INIT(n)
#define NDN_LOG(l, a, m, e)
#endif

#define NDN_LOG_TRACE(expression) NDN_LOG(TRACE, VERBOSE, TRACE, expression)
#define NDN_LOG_DEBUG(expression) NDN_LOG(DEBUG, DEBUG, DEBUG,   expression)
#define NDN_LOG_INFO(expression)  NDN_LOG(INFO,  INFO,  INFO,    expression)
#define NDN_LOG_WARN(expression)  NDN_LOG(WARN,  WARN,  WARNING, expression)
#define NDN_LOG_ERROR(expression) NDN_LOG(ERROR, ERROR, ERROR,   expression)
#define NDN_LOG_FATAL(expression) NDN_LOG(FATAL, FATAL, FATAL,   expression)

}
}

#endif // ANDROID_LOGGER_HPP
