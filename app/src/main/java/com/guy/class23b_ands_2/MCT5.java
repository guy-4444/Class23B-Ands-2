package com.guy.class23b_ands_2;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MCT5 {

    // with tags
    /*
     V5 - stop duplicate tasks.
        - remove tasks by tag.
      + Initialize this module in your Application class:
            MCT5.initHelper();
          + you can create new task by calling:
                MCT5.get().cycle(cycleTicker, 4, 1500);
                MCT5.get().cycle(cycleTicker, 4, 1500, "Activity_Main");
                4 times every 1.5 seconds
                MCT5.CycleTicker cycleTicker = new MCT5.CycleTicker() {
                    @Override
                    public void periodic(int repeatsRemaining) {
                        Log.d("pttt", "AAAA");
                    }
                };
          + The task will run four times before closing itself
            or you can close it by yourself:
                MCT5.get().remove(cycleTicker);
          + Number of repeats must be more than 0 or the timer doesn't start.
          + You can run the timer continuously if you sent MCT5.CONTINUOUSLY_REPEATS instead of a number
          + This module support more than one of the same callback
          + The tasks will run on the background - if you want to be avoid from double timers
            on the background call removeAllCallbacks in activity's onDestroy()
     */

    public interface KillTicker {
        void killMe(Task task);
    }

    public interface CycleTicker {
        void periodic(int repeatsRemaining);
        void done();
    }

    public interface OneTimeTicker {
        void done();
    }

    public class Task {
        private ScheduledExecutorService scheduleTaskExecutor;
        CycleTicker cycleTickerCallback;
        OneTimeTicker oneTimeTicker;
        KillTicker killTickerCallBack;
        String tag;
        int repeats;
        boolean IM_DONE = false;

        public Task(KillTicker killTickerCallBack, CycleTicker cycleTickerCallback, int repeats, int periodInMilliseconds, String tag) {
            this.killTickerCallBack = killTickerCallBack;
            this.cycleTickerCallback = cycleTickerCallback;
            this.repeats = repeats;
            this.tag = tag;

            scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    tickerFunction();
                }
            }, 0, periodInMilliseconds, TimeUnit.MILLISECONDS);
        }

        public Task(KillTicker killTickerCallBack, OneTimeTicker oneTimeTicker, int delayInMilliseconds, String tag) {
            this.killTickerCallBack = killTickerCallBack;
            this.oneTimeTicker = oneTimeTicker;
            this.repeats = 1;
            this.tag = tag;

            scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
            scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    singleTickerFunction();
                }
            }, delayInMilliseconds, 1, TimeUnit.MILLISECONDS);
        }

        private void singleTickerFunction() {
            oneTimeTicker.done();
            killMe();
        }

        private void tickerFunction() {
            // send done function second after last call
            if (IM_DONE) {
                cycleTickerCallback.done();
                killMe();
            }
            else {
                cycleTickerCallback.periodic(repeats);

                if (!(repeats == CONTINUOUSLY_REPEATS)) {
                    repeats--;
                    if (repeats <= 0) {
                        IM_DONE = true;
                    }
                }
            }
        }

        public void killMe() {
            try {
                scheduleTaskExecutor.shutdown();
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            scheduleTaskExecutor = null;
            cycleTickerCallback = null;
            killTickerCallBack.killMe(this);
        }
    }

    public static final int CONTINUOUSLY_REPEATS = -999;

    private ArrayList<Task> tasks;
    private Object locker = new Object();
    private static MCT5 instance;

    KillTicker killTickerCallBack = new KillTicker() {
        @Override
        public void killMe(Task task) {
            synchronized (locker) {
                tasks.remove(task);
            }
        }
    };

    public static MCT5 get() {
        return instance;
    }

    public static MCT5 initHelper() {
        if (instance == null)
            instance = new MCT5();
        return instance;
    }

    private MCT5() {
        tasks = new ArrayList<>();
    }

    public int getNumOfActiveTickers() {
        synchronized (locker) {
            return tasks.size();
        }
    }

    public void cycle(CycleTicker cycleTicker, int repeats, int periodInMilliseconds, String tag) {
        if (repeats == CONTINUOUSLY_REPEATS || repeats > 0) {
            synchronized (locker) {
                tasks.add(new Task(killTickerCallBack, cycleTicker, repeats, periodInMilliseconds, tag));
            }
        }
    }

    public void single(OneTimeTicker oneTimeTicker, int delayInMilliseconds, String tag) {
        synchronized (locker) {
            tasks.add(new Task(killTickerCallBack, oneTimeTicker, delayInMilliseconds, tag));
        }
    }

    public void cycle(CycleTicker cycleTicker, int repeats, int periodInMilliseconds) {
        cycle(cycleTicker, repeats, periodInMilliseconds, "");
    }

    public void single(OneTimeTicker oneTimeTicker, int delayInMilliseconds) {
        single(oneTimeTicker, delayInMilliseconds,"");
    }

    public void remove(CycleTicker cycleTicker) {
        synchronized (locker) {
            for (int i = tasks.size() - 1; i >= 0; i--) {
                if (tasks.get(i).cycleTickerCallback == cycleTicker) {
                    tasks.get(i).killMe();
                    // break; Continue loop because there may be more duplicate tasks
                }
            }
        }
    }


    public void remove(OneTimeTicker oneTimeTicker) {
        synchronized (locker) {
            for (int i = tasks.size() - 1; i >= 0; i--) {
                if (tasks.get(i).oneTimeTicker == oneTimeTicker) {
                    tasks.get(i).killMe();
                    // break; Continue loop because there may be more duplicate tasks
                }
            }
        }
    }

    public void removeAll() {
        synchronized (locker) {
            for (int i = tasks.size() - 1; i >= 0; i--) {
                tasks.get(i).killMe();
            }
        }
    }

    public void removeAllByTag(String tag) {
        synchronized (locker) {
            for (int i = tasks.size() - 1; i >= 0; i--) {
                if (tasks.get(i).tag.equals(tag)) {
                    tasks.get(i).killMe();
                }
            }
        }
    }
}