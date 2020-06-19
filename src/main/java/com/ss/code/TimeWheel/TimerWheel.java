package com.ss.code.TimeWheel;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TimerWheel {

    public static void main(String[] args) {
        TimerWheel timerWheel = new TimerWheel(64,TimeUnit.MILLISECONDS,20L);
        timerWheel.start();

        timerWheel.scheduleTask(new Runnable() {
            public void run() {
                System.out.println("hello timer wheel 1 second");
            }
        },TimeUnit.SECONDS,1);

        timerWheel.scheduleTask(new Runnable() {
            public void run() {
                System.out.println("hello timer wheel 10 second");
            }
        },TimeUnit.SECONDS,10);
    }

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 20, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());

    private AtomicInteger state = new AtomicInteger();

    public static final int TIMER_STATE_STARTED = 1;

    public static final int TIMER_STATE_READY = 0;

    private final int slotLength;

    private  long tickDuration = 20L;

    private  long tickDurationNano = 0;

    private TimeUnit tickUnit = TimeUnit.MILLISECONDS;

    private Queue<TimerTask> timerTasks = new ConcurrentLinkedQueue();

    private Thread scheduleThread;

    private long startTime;

    private TimerTaskLinkedList[] timerListes;

    private TimerTaskLinkedList[] dayTimerList;

    private TimerTaskLinkedList[] monthTimerList;

    private CountDownLatch startCountDownLatch = new CountDownLatch(1);

    public TimerWheel(int length,TimeUnit unit,Long tickDuration) {
        this.slotLength = length;
        this.tickDuration = tickDuration;
        this.tickUnit = unit;
        this.tickDurationNano = unit.toNanos(tickDuration);
    }

    public void start() {
        if (state.get() == TIMER_STATE_STARTED) {
            return;
        }

        while (true) {
            if (state.compareAndSet(TIMER_STATE_READY, TIMER_STATE_STARTED)) {
                this.startTime = System.nanoTime();
                doStart();
                break;
            }
        }

    }

    private long tick = 0;

    public void doStart() {
        this.timerListes = new TimerTaskLinkedList[slotLength];
        scheduleThread = new ScheduleThread();
        scheduleThread.start();
    }

    public void scheduleTask(Runnable task, TimeUnit unit, long delay) {

        if (state.get() != TIMER_STATE_STARTED) {
            try {
                startCountDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TimerTask timerTask = new TimerTask(task);
        timerTask.setDeadLine(System.nanoTime() + unit.toNanos(delay) - startTime);
        this.timerTasks.add(timerTask);
    }

     class ScheduleThread extends Thread {

         private void waitTick() {
             long deadLine = tickDurationNano * (tick + 1);
             try {
                 long current = System.nanoTime() - startTime;
                 long needSleepTime = (deadLine - current);
//                 System.out.println("need sleep nano time:" + needSleepTime);
                 if (needSleepTime <= 0) {
                     return;
                 }
                 TimeUnit.NANOSECONDS.sleep(needSleepTime);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }

        @Override
        public void run() {
            startTime = System.nanoTime();
            startCountDownLatch.countDown();

            while (true) {
                waitTick();
                transformPreAddTask();
                TimerTaskLinkedList slot = timerListes[(int) (tick) % (slotLength - 1)];
                if (slot != null) {
                    slot.work(tickUnit.toNanos((tick + 1) * tickDuration));
                }
                tick++;
            }
        }

         private void transformPreAddTask() {
             for (int i = 0; i < 10000; i++) {
                 TimerTask task = timerTasks.poll();
                 if (task == null) {
                     break;
                 }
                 /**
                  * nano seconds.
                  */
//                 int slot_index = (int) (tick & (slotLength - 1));

                 long calculated = task.getDeadLine() / tickDurationNano;
                 long round = (calculated - tick) / slotLength;

                 long ticks = Math.max(calculated, tick);
                 int slot_index = (int) (ticks % (slotLength - 1));

                 task.setRound(round);
                 TimerTaskLinkedList list = timerListes[slot_index];
                 if (list == null) {
                     list = new TimerTaskLinkedList(executor);
                     timerListes[slot_index] = list;
                 }
                 list.addTask(task);
             }
         }
    }



}
