package info.danbecker.ss.graph;

public class WindowOpenerTest {
    // @Test
    public void testAWT() throws InterruptedException {
        System.out.println( "Threads=" + Thread.activeCount() + ", current=" + Thread.currentThread().getName());
        WindowOpener.launchAWT( WindowOpener.DEFAULT_COUNT );
        Thread.sleep( 1000 ); // Timed exit
        // Thread.currentThread().join(); // Wait for threads to exit
        System.out.println( "Threads=" + Thread.activeCount() + ", current=" + Thread.currentThread().getName());
    }

    // @Test
    public void testSwing() throws InterruptedException {
        System.out.println( "Threads=" + Thread.activeCount() + ", current=" + Thread.currentThread().getName());
        WindowOpener.launchSwing( WindowOpener.DEFAULT_COUNT );
        Thread.sleep( 1000 ); // Timed exit
        // Thread.currentThread().join(); // Wait for threads to exit
        System.out.println( "Threads=" + Thread.activeCount() + ", current=" + Thread.currentThread().getName());
    }
}