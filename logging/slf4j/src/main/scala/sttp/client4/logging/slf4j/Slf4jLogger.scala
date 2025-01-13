package sttp.client4.logging.slf4j

import org.slf4j.{LoggerFactory, MDC}
import sttp.client4.logging.{LogLevel, Logger}
import sttp.monad.MonadError

class Slf4jLogger[F[_]](name: String, monad: MonadError[F]) extends Logger[F] {
  private val underlying = LoggerFactory.getLogger(name)

  private def setContext(context: Map[String, Any]): Unit =
    context.foreach { case (k, v) =>
      MDC.put(k, v.toString)
    }

  private def clearContext(context: Map[String, Any]): Unit =
    context.keys.foreach { key =>
      MDC.remove(key)
    }

  override def apply(
      level: LogLevel,
      message: => String,
      context: Map[String, Any]
  ): F[Unit] = monad.eval {
    setContext(context)
    try
      level match {
        case LogLevel.Trace if underlying.isTraceEnabled =>
          underlying.trace(message)

        case LogLevel.Debug if underlying.isDebugEnabled =>
          underlying.debug(message)

        case LogLevel.Info if underlying.isInfoEnabled =>
          underlying.info(message)

        case LogLevel.Warn if underlying.isWarnEnabled =>
          underlying.warn(message)

        case LogLevel.Error if underlying.isErrorEnabled =>
          underlying.error(message)

        case _ => ()
      }
    finally
      clearContext(context)
  }

  override def apply(level: LogLevel, message: => String, throwable: Throwable, context: Map[String, Any]): F[Unit] =
    monad.eval {
      setContext(context)
      try
        level match {
          case LogLevel.Trace if underlying.isTraceEnabled =>
            underlying.trace(message, throwable)

          case LogLevel.Debug if underlying.isDebugEnabled =>
            underlying.debug(message, throwable)

          case LogLevel.Info if underlying.isInfoEnabled =>
            underlying.info(message, throwable)

          case LogLevel.Warn if underlying.isWarnEnabled =>
            underlying.warn(message, throwable)

          case LogLevel.Error if underlying.isErrorEnabled =>
            underlying.error(message, throwable)

          case _ => ()
        }
      finally
        clearContext(context)
    }
}
