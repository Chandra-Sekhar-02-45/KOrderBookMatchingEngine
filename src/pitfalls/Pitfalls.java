package pitfalls;

import java.util.concurrent.atomic.AtomicInteger;

public class Pitfalls {

    public static void main(String[] args) {

        System.out.println("=== RACE CONDITION DEMO ===");
        raceConditionDemo();

        System.out.println("\n=== FIX USING ATOMICINTEGER ===");
        raceConditionFixed();

        System.out.println("\n=== DEADLOCK DEMO ===");
        deadlockDemo();

        System.out.println(
                "\n=== DEADLOCK FIX ==="
        );

        deadlockFixed();

        System.out.println(
                "\n=== VOLATILE COUNTER DEMO ==="
        );

        volatileCounterDemo();
    }

    static class VolatileCounter {

        volatile int count = 0;

        void increment() {
            count++;
        }
    }

    private static void raceConditionDemo() {

        Counter counter = new Counter();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.increment();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.increment();
            }
        });

        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.increment();
            }
        });

        startAndWait(t1, t2, t3);

        System.out.println("Expected Count = 300000");
        System.out.println("Actual Count = " + counter.count);
    }

    private static void raceConditionFixed() {

        AtomicCounter counter =
                new AtomicCounter();

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.increment();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.increment();
            }
        });

        Thread t3 = new Thread(() -> {
            for (int i = 0; i < 100000; i++) {
                counter.increment();
            }
        });

        startAndWait(t1, t2, t3);

        System.out.println("Expected Count = 300000");
        System.out.println("Actual Count = " + counter.getCount());
    }

    private static void startAndWait(Thread... threads) {

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class Counter {

        int count = 0;

        void increment() {
            count++;
        }
    }

    static class AtomicCounter {

        AtomicInteger count =
                new AtomicInteger();

        void increment() {
            count.incrementAndGet();
        }

        int getCount() {
            return count.get();
        }
    }

    private static void deadlockDemo() {

        Object lockA = new Object();
        Object lockB = new Object();

        Thread t1 = new Thread(() -> {

            synchronized (lockA) {

                System.out.println(
                        "Thread-1 acquired LockA"
                );

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                synchronized (lockB) {

                    System.out.println(
                            "Thread-1 acquired LockB"
                    );
                }
            }
        });

        Thread t2 = new Thread(() -> {

            synchronized (lockB) {

                System.out.println(
                        "Thread-2 acquired LockB"
                );

                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                synchronized (lockA) {

                    System.out.println(
                            "Thread-2 acquired LockA"
                    );
                }
            }
        });

        t1.start();
        t2.start();

        try {

            t1.join(1000);
            t2.join(1000);

        } catch (Exception e) {

            e.printStackTrace();
        }

        System.out.println(
                "Deadlock occurred because both threads waited forever."
        );
    }

    private static void deadlockFixed() {

        Object lockA = new Object();
        Object lockB = new Object();

        Thread t1 = new Thread(() -> {

            synchronized (lockA) {

                System.out.println(
                        "Thread-1 acquired LockA"
                );

                synchronized (lockB) {

                    System.out.println(
                            "Thread-1 acquired LockB"
                    );
                }
            }
        });

        Thread t2 = new Thread(() -> {

            synchronized (lockA) {

                System.out.println(
                        "Thread-2 acquired LockA"
                );

                synchronized (lockB) {

                    System.out.println(
                            "Thread-2 acquired LockB"
                    );
                }
            }
        });

        t1.start();
        t2.start();

        try {

            t1.join();
            t2.join();

        } catch (InterruptedException e) {

            e.printStackTrace();
        }

        System.out.println(
                "Deadlock avoided by consistent lock ordering."
        );
    }

    private static void volatileCounterDemo() {

        VolatileCounter counter =
                new VolatileCounter();

        Thread t1 = new Thread(() -> {

            for (int i = 0; i < 100000; i++) {
                counter.increment();
            }
        });

        Thread t2 = new Thread(() -> {

            for (int i = 0; i < 100000; i++) {
                counter.increment();
            }
        });

        Thread t3 = new Thread(() -> {

            for (int i = 0; i < 100000; i++) {
                counter.increment();
            }
        });

        t1.start();
        t2.start();
        t3.start();

        try {

            t1.join();
            t2.join();
            t3.join();

        } catch (InterruptedException e) {

            e.printStackTrace();
        }

        System.out.println(
                "Expected Count = 300000"
        );

        System.out.println(
                "Actual Count = "
                        + counter.count
        );

        System.out.println(
                "volatile provides visibility, NOT atomicity."
        );
    }
}