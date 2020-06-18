package com.ss.code.TimeWheel;

public class TimerTask  {

    private long round;

    /**
     * nano seconds.
     */
    private long deadLine;


    private TimerTask next;

    private TimerTask prev;

    private Runnable task;

    public TimerTask(Runnable runnable) {
        this.task = runnable;
    }

    public Runnable getTask() {
        return this.task;
    }

    public long getRound() {
        return round;
    }

    public void setRound(long round) {
        this.round = round;
    }

    public long getDeadLine() {
        return deadLine;
    }

    public void setDeadLine(long deadLine) {
        this.deadLine = deadLine;
    }

    public TimerTask getNext() {
        return next;
    }

    public void setNext(TimerTask next) {
        this.next = next;
    }

    public TimerTask getPrev() {
        return prev;
    }

    public void setPrev(TimerTask prev) {
        this.prev = prev;
    }
}
