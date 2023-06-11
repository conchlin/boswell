package game

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class WrappingScheduledExecutor(corePoolSize: Int) : ScheduledThreadPoolExecutor(corePoolSize) {

    /**
     *  Always use a try-catch within your run method.
     *
     * Any thrown exception or error reaching the ScheduledExecutorService causes the executor to halt.
     * This work stoppage happens silently, you'll not be informed. By using try-catches within runnable methods,
     * we can catch both the exceptions and errors. This wrapper does that.
     *
     * https://stackoverflow.com/questions/6894595/scheduledexecutorservice-exception-handling
     * http://code.nomad-labs.com/2011/12/09/mother-fk-the-scheduledexecutorservice/
     */

    override fun schedule(
        command: Runnable, delay: Long, timeUnit: TimeUnit
    ): ScheduledFuture<*> {
        return super.schedule(wrapRunnable(command), delay, timeUnit)
    }

    override fun scheduleAtFixedRate(
        command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit
    ): ScheduledFuture<*> {
        return super.scheduleAtFixedRate(wrapRunnable(command), initialDelay, period, unit)
    }

    override fun scheduleWithFixedDelay(
        command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit
    ): ScheduledFuture<*> {
        return super.scheduleWithFixedDelay(wrapRunnable(command), initialDelay, delay, unit)
    }

    private fun wrapRunnable(command: Runnable): Runnable {
        return LogOnExceptionUnit(command)
    }

    private class LogOnExceptionUnit(private val command: Runnable) : Runnable {
        override fun run() {
            try {
                command.run()
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException(e)
            }
        }
    }
}