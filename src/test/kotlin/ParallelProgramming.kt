import org.junit.jupiter.api.Test
import java.util.Date
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.system.measureTimeMillis

class ParallelProgramming {
    @Test
    fun membuatThread() {
        println("START PROGRAM - ${Date()}")
        // Membuat thread
        val runnable = Runnable {
            // Thread akan dijalankan disini
            println("Mulai Thread - ${Date()}")
            Thread.sleep(2000) // Hentikan 2 detik
            println("Hello World!")
            Thread.sleep(500)
            println("Selesai Thread - ${Date()}")
            // Seluruh proses runnable akan memakan waktu 2.5 detik
        }

        val thread = Thread(runnable)
        thread.start() // Jalankan thread
        Thread.sleep(3500) // Delay untuk menunggu proses thread yang sudah dibuat
        println("FINISH PROGRAM - ${Date()}")
    }

    @Test
    fun multipleThread() {
        println("START MAIN THREAD \n")
        val thread1 = Thread {
            println("Mulai Thread 1    - ${Date()}")
            Thread.sleep(1000)
            println("Nama thread 1     : ${Thread.currentThread().name} - ${Date()}")
            Thread.sleep(1000)
            println("Selesai Thread 1  - ${Date()}")
        } // Berjalan 2 detik

        val thread2 = Thread {
            println("Mulai Thread 2    - ${Date()}")
            Thread.sleep(500)
            println("Nama thread 2     : ${Thread.currentThread().name}- ${Date()}")
            Thread.sleep(500)
            println("Selesai Thread 2  - ${Date()}")
        } // Berjalan 1 detik

        thread1.start()
        thread2.start()
        Thread.sleep(3000) // Menunggu proses dari thread 1 dan 2
        println("\nFINISH MAIN THREAD")
    }

    @Test
    fun menggunakanExecutorService_SingleThread() {
        println("START MAIN THREAD\n")
        val executorService = Executors.newSingleThreadExecutor()
        (1..10).forEach {
            executorService.execute {
                Thread.sleep(1000)
                println("Done $it - ${Date()} in ${Thread.currentThread().name}")
            }
            println("Insert runnable $it kedalam thread pool - ${Date()}")
        }
        println("\nWAITING\n")
        Thread.sleep(11000) // 11 detik
        println("\nFINISH MAIN THREAD")
    }

    @Test
    fun menggunakanExecutorService_FixNThread() {
        println("START MAIN THREAD\n")
        val executorService = Executors.newFixedThreadPool(3)
        (1..10).forEach {
            executorService.execute {
                Thread.sleep(1000)
                println("Done $it - ${Date()} in ${Thread.currentThread().name}")
            }
            println("Insert runnable $it kedalam thread pool - ${Date()}")
        }
        println("\nWAITING\n")
        Thread.sleep(11000) // 11 detik
        println("\nFINISH MAIN THREAD")
    }

    @Test
    fun menggunakanExecutorService_CachedThread() {
        println("START MAIN THREAD | ${Date()}\n")
        val executorService = Executors.newCachedThreadPool()
        (1..10).forEach {
            executorService.execute {
                Thread.sleep(1000)
                println("Done $it - ${Date()} in ${Thread.currentThread().name}")
            }
            println("Insert runnable $it kedalam thread pool - ${Date()}")
        }
        println("\nWAITING\n")
        Thread.sleep(11000) // 11 detik
        println("\nFINISH MAIN THREAD | ${Date()}")
    }

    fun getFoo(): String {
        Thread.sleep(1000)
        return "Foo"
    }

    fun getBar(): String {
        Thread.sleep(1000)
        return "Bar"
    }

    // Tanpa Callable dan Future
    @Test
    fun getFooBarNonParallel() {
        val time = measureTimeMillis {
            val foo = getFoo()
            val bar = getBar()
            val result = foo + bar
            println("Result = $result")
        }
        println("Total time : $time")
    }

    // Future Get
    @Test
    fun getFooBarFuture() {
        val executorService = Executors.newFixedThreadPool(10)
        val time = measureTimeMillis {
            val foo: Future<String> = executorService.submit(Callable { getFoo() })
            val bar: Future<String> = executorService.submit(Callable { getBar() })
            val result = foo.get() + bar.cancel(true)
            println("Result = $result")
        }
        println("Total time : $time")
    }
}