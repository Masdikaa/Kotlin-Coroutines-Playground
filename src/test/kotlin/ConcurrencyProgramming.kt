import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date
import kotlin.concurrent.thread
import kotlin.test.Test

@OptIn(DelicateCoroutinesApi::class)
class ConcurrencyProgramming {

    @Test
    fun testSuspendFunction() {
        runBlocking { // Running Coroutine and Block Thread
            sayHello("Masdika")
        }
    }

    @Test
    fun testCoroutine() {
        println("START PROGRAM  | ${Date()}")
        GlobalScope.launch {
            sayHello("Masdika")
        }

        // Waiting GlobalScope finishing processes
        runBlocking {
            delay(3000)
        }
        println("FINISH PROGRAM | ${Date()}")
    }

    suspend fun sayHello(name: String) {
        delay(1000)
        println("Hello - ${Thread.currentThread().name} | ${Date()}")
        delay(1000) // Delay suspend function
        println("$name - ${Thread.currentThread().name} | ${Date()}")
    }

    // Comparison between Thread and Coroutine
    @Test
    fun testThread() {
        repeat(100_000) {
            thread {
                Thread.sleep(1000)
                println("Thread $it | ${Date()}")
            }
        }

        println("WAITING")
        Thread.sleep(10_000)
        println("FINISH PROGRAM | ${Date()}")
    }

    @Test
    fun testManyCoroutine() {
        repeat(100_000) {
            GlobalScope.launch {
                delay(1000)
                println("Coroutine $it | ${Date()}")
            }
        }

        println("WAITING")
        runBlocking {
            delay(10_000)
        }
        println("FINISH PROGRAM | ${Date()}")
    }

    @Test
    fun testRunBlocking() {
        runBlocking {
            GlobalScope.launch {
                delay(2000)
                println("Finish Coroutine - ${Thread.currentThread().name}")
            }
        }
    }

    @Test
    fun testJobStart() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            val job: Job = GlobalScope.launch(start = CoroutineStart.LAZY) {
                delay(2000)
                println("Finish Coroutine - ${Thread.currentThread().name}")
            }
            job.start()
            delay(3000)
        }
        println("FINISH PROGRAM  | ${Date()}")
    }

    @Test
    fun testJobJoin() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            val job: Job = GlobalScope.launch() {
                delay(2000)
                println("Finish Coroutine - ${Thread.currentThread().name}")
            }
            job.join()
        }
        println("FINISH PROGRAM  | ${Date()}")
    }

    @Test
    fun testJobCancel() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            val job: Job = GlobalScope.launch() {
                delay(2000)
                println("Finish Coroutine - ${Thread.currentThread().name}")
            }
            println("WAITING")
            job.cancel()
            delay(3000)
        }
        println("FINISH PROGRAM  | ${Date()}")
    }
}