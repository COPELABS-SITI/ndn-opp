#ifndef ANDROID_LOGGING_HPP
#define ANDROID_LOGGING_HPP

#include <ndn-cxx/common.hpp>

#include <mutex>
#include <unordered_map>

namespace ndn {
namespace util {

enum class LogLevel;
class Logger;

class Logging : noncopyable {
public:
  static void addLogger(Logger& logger);
  static void setLevel(const std::string& moduleName, LogLevel level);
  static void setLevel(const std::string& config);

private:
  Logging();

  void addLoggerImpl(Logger& logger);
  void setLevelImpl(const std::string& moduleName, LogLevel level);
  void setDefaultLevel(LogLevel level);
  void setLevelImpl(const std::string& config);
  static Logging& get();

private:
  std::mutex m_mutex;
  std::unordered_map<std::string, LogLevel> m_enabledLevel; ///< moduleName => minimum level
  std::unordered_multimap<std::string, Logger*> m_loggers; ///< moduleName => logger
};

inline void Logging::addLogger(Logger& logger) {
  get().addLoggerImpl(logger);
}

inline void Logging::setLevel(const std::string& moduleName, LogLevel level) {
  get().setLevelImpl(moduleName, level);
}

inline void Logging::setLevel(const std::string& config) {
  get().setLevelImpl(config);
}

} // namespace util
} // namespace ndn

#endif // ANDROID_LOGGING_HPP
