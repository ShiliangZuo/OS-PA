package nachos.threads;

import nachos.machine.*;

import java.util.Random;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A scheduler that chooses threads using a lottery.
 *
 * <p>
 * A lottery scheduler associates a number of tickets with each thread. When a
 * thread needs to be dequeued, a random lottery is held, among all the tickets
 * of all the threads waiting to be dequeued. The thread that holds the winning
 * ticket is chosen.
 *
 * <p>
 * Note that a lottery scheduler must be able to handle a lot of tickets
 * (sometimes billions), so it is not acceptable to maintain state for every
 * ticket.
 *
 * <p>
 * A lottery scheduler must partially solve the priority inversion problem; in
 * particular, tickets must be transferred through locks, and through joins.
 * Unlike a priority scheduler, these tickets add (as opposed to just taking
 * the maximum).
 */
public class LotteryScheduler extends PriorityScheduler {
    /**
     * Allocate a new lottery scheduler.
     */
    public LotteryScheduler() {
    }
    
    /**
     * Allocate a new lottery thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer tickets from waiting threads
     *					to the owning thread.
     * @return	a new lottery thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
        // implement me
        //return null;
        return new LotteryQueue(transferPriority);
    }

    protected ThreadState getThreadState(KThread thread) {
        if (thread.schedulingState == null)
            thread.schedulingState = new ThreadState(thread);

        return (ThreadState) thread.schedulingState;
    }

    protected class LotteryQueue extends PriorityScheduler.PriorityQueue {
        LotteryQueue(boolean transferPriority) {
            super(transferPriority);
        }

        protected ThreadState pickNextThread() {
            // implement me
            ThreadState returnThreadState = null;
            int sum = 0;

            for (KThread kThread : waitQueue) {
                ThreadState thisThreadState = getThreadState(kThread);
                sum += thisThreadState.getEffectivePriority();
            }

            Random random = new Random;
            int lotteryValue = random.nextInt(sum) + 1;

            sum = 0;

            for (KThread kThread : waitQueue) {
                ThreadState thisThreadState = getThreadState(kThread);
                sum += thisThreadState.getEffectivePriority();
                if (sum >= lotteryValue) {
                    returnThreadState = thisThreadState;
                    break;
                }
            }

            return returnThreadState;
        }

        public int getEffectivePriority() {
            //added by me
            if (transferPriority == false)
                return priorityMinimum;
            cachedEffectivePriority = priorityMinimum;
            for (KThread kThread : waitQueue) {
                int thisEffectivePriority = getThreadState(kThread).getEffectivePriority();
                /*if (thisEffectivePriority > cachedEffectivePriority)
                    cachedEffectivePriority = thisEffectivePriority;*/
                cachedEffectivePriority += thisEffectivePriority;
            }
            isQueueDirty = false;
            return cachedEffectivePriority;
        }
    }


    protected class ThreadState extends PriorityScheduler.ThreadState {
        public ThreadState(KThread thread) {
            super(thread);
        }

        /**
         * Return the effective priority of the associated thread.
         *
         * @return	the effective priority of the associated thread.
         */
        public int getEffectivePriority() {
            // implement me
            //return priority;

            if (isThreadDirty) {
                cachedEffectivePriority = this.priority;
                for (ThreadQueue queue : resources) {
                    int thisQueueEffectivePriority = ((PriorityQueue) queue).getEffectivePriority();
                    /*if (thisQueueEffectivePriority > this.cachedEffectivePriority) {
                        cachedEffectivePriority = thisQueueEffectivePriority;
                    }*/
                    cachedEffectivePriority += thisQueueEffectivePriority;
                }
            }
            isThreadDirty = false;
            return cachedEffectivePriority;
        }
    }
}
