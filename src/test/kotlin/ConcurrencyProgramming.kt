import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
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

    @Test
    fun testJoinAll() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            val job1: Job = launch {
                delay(1000)
                println("Job 1 Selesai - ${Date()}")
            }

            val job2: Job = launch {
                delay(2000)
                println("Job 2 Selesai - ${Date()}")
            }

            // Menunggu kedua job sekaligus
            joinAll(job1, job2)
        }
        println("FINISH PROGRAM  | ${Date()}")
    }

    @Test
    fun testCanNotCancel() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            val job: Job = GlobalScope.launch {
                // Thread.sleep memblokir thread dan TIDAK mengecek status cancel
                // Akibatnya, sinyal cancel akan diabaikan selama proses ini
                Thread.sleep(2000)
                println("End Coroutine: ${Date()}")
            }
            // Memberi waktu sedikit agar coroutine mulai berjalan
            delay(1000)
            println("Membatalkan Job...")
            job.cancel()

            // Menunggu job benar-benar selesai (untuk membuktikan bahwa ia tidak berhenti saat di-cancel)
            job.join()
        }
        println("FINISH PROGRAM  | ${Date()}")
    }

    @Test
    fun testCancelCooperative() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            val job = GlobalScope.launch {
                println("Start Coroutine: ${Date()}")
                for (i in 1..10) {
                    // CARA 1: Cek manual
                    // if (!isActive) break

                    // CARA 2: ensureActive() (Lebih direkomendasikan)
                    // Jika job sudah di-cancel, baris ini akan throw CancellationException
                    ensureActive()
                    println("Processing $i: ${Date()}")
                    Thread.sleep(500) // Simulasi proses berat (blocking)
                }
                println("End Coroutine: ${Date()}")
            }
            delay(1200) // Biarkan berjalan sebentar (sekitar 2 loop)
            println("Membatalkan Job...")
            job.cancel()

            job.join()
        }
        println("FINISH PROGRAM  | ${Date()}")
    }

    @Test
    fun testJobFinally() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            val job: Job = GlobalScope.launch {
                try {
                    println("Start Coroutine")
                    delay(2000)
                    println("End   Coroutine")
                } finally {
                    println("Finish Coroutine - Cleanup Code")
                }
            }
            delay(1000)
            println("Membatalkan Job,,,,,")
            job.cancel() // Memicu CancellationException

            job.join() // Menunggu proses cleanup
        }
        println("FINISH PROGRAM | ${Date()}")
    }

    @Test
    fun testTimeout() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            val job = launch {
                println("Start Coroutine")
                // Atur batas waktu 1 detik untuk proses
                withTimeout(1000) {
                    repeat(10) {
                        // SImulasi proses yang membutuhkan waktu 4 detik
                        delay(400)
                        println("$it. Masih proses... ${Date()}")
                    }
                }
                println("Finish Coroutine") // Tidak akan dieksekusi
            }
            job.join()
        }
        println("FINISH PROGRAM | ${Date()}")
    }

    @Test
    fun testTimeoutOrNull() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            println("Start Coroutine")
            // Return sebuah value jika success dan null jika timeout
            val result: String? = withTimeoutOrNull(1000) {
                repeat(10) {
                    delay(400)
                }
                "Coroutine Berhasil Diselesaikan"
            }

            if (result == null) {
                println("Proses Timeout (Waktu habis)")
            } else {
                println("Hasil: $result")
            }

            println("Finish Coroutine - Result: $result")
        }
        println("FINISH PROGRAM | ${Date()}")
    }
}