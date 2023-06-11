package game

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit


class EventScheduler {
    private val scheduler: ScheduledThreadPoolExecutor = WrappingScheduledExecutor(10)
    private val newDay = LocalDateTime.now().until(LocalDate.now().plusDays(1).atStartOfDay(), ChronoUnit.MINUTES)

    companion object {
        val event = EventScheduler()

        /**
         * Creates and executes a one-shot action that becomes enabled after the given delay.
         */
        fun schedule(action: Runnable, delay: Long) {
            event.scheduler.schedule(action, delay, TimeUnit.MILLISECONDS)
        }

        /**
         * Creates and executes a periodic action that becomes enabled first after the given initial delay,
         * and subsequently with the given period.
         */
        fun scheduleAtFixedRate(action: Runnable, initialDelay: Long, period: Long, timeUnit: TimeUnit) {
            event.scheduler.scheduleAtFixedRate(action, initialDelay, period, timeUnit)
        }

        /**
         * Creates and executes a periodic action that becomes enabled first after the given initial
         * delay, and subsequently with the given delay between the termination of one execution
         * and the commencement of the next.
         */
        fun scheduleWithFixedDelay(action: Runnable, initialDelay: Long, delay: Long, timeUnit: TimeUnit) {
            event.scheduler.scheduleWithFixedDelay(action, initialDelay, delay, timeUnit)
        }

        /**
         * Creates and executes an action that happens at the start of a new day.
         */
        fun scheduleStartOfDay(action: Runnable) {
            event.scheduler.scheduleAtFixedRate(action, event.newDay, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES)
        }

        /**
         * Creates and executes an action that is performed at the specified timestamp
         */
        fun scheduleAtTimestamp(action: Runnable, timestamp: Long) {
            schedule(action, timestamp - System.currentTimeMillis())
        }
    }

}