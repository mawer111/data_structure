package com.ss.code.TimeWheel;


import java.util.concurrent.Executor;

public class TimerTaskLinkedList {

    TimerTask head;
    TimerTask tail;

    private Executor executor;

    public TimerTaskLinkedList(Executor executor) {
        this.executor = executor;
    }

    private void invokeTask(TimerTask task) {
        this.executor.execute(task.getTask());
    }

    public void work(long deadline) {
        if (head == null) {
            return;
        }
        TimerTask cur = null;
        cur = head;
        while (cur != null) {
            TimerTask next = cur.getNext();
            long round = cur.getRound();
            if (round <= 0) {
                //invoke task
                long expire = cur.getDeadLine();
                if (expire > deadline) {
                    throw new IllegalStateException();
                }
                this.invokeTask(cur);
                removeTask(cur);
            }else{
                cur.setRound(cur.getRound() - 1);
            }
            cur = next;
        }
    }

    public void removeTask(TimerTask task) {
        if (task == null) {
            return;
        }
        TimerTask next = task.getNext();
        if (task == head) {
            if (task == tail) {
                head = null;
                tail = null;
            }
            if (next != null) {
                next.setPrev(null);
            }
            head = next;
        }else{
            if (task == tail) {
                tail = task.getPrev();
            }
            TimerTask prev = task.getPrev();
            if (prev != null) {
                prev.setNext(next);
            }
            if (next != null) {
                next.setPrev(prev);
            }
        }

        task.setPrev(null);
        task.setNext(null);
    }


    public void addTask(TimerTask task) {
        if (head == null) {
            tail = head = task;
        }else{
            task.setPrev(tail);
            tail.setNext(task);
            tail = task;
        }
    }

}
