import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Date
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlin.system.measureTimeMillis
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

    suspend fun getProcessA(): Int {
        delay(1000)
        return 10
    }

    suspend fun getProcessB(): Int {
        delay(1000)
        return 20
    }

    @Test
    fun testSequential() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            val time = measureTimeMillis {
                val resultA = getProcessA()
                val resultB = getProcessB()

                println("Hasil A: $resultA")
                println("Hasil B: $resultB")
                println("Total  : ${resultA + resultB}")
            }

            println("Total Waktu: $time")
        }
        println("FINISH PROGRAM | ${Date()}")
    }

    @Test
    fun testAsync() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            val time = measureTimeMillis {
                val deferredA: Deferred<Int> = async {
                    getProcessA()
                }

                val deferredB: Deferred<Int> = async {
                    getProcessB()
                }

                val resultA = deferredA.await()
                val resultB = deferredB.await()

                println("Hasil A: $resultA")
                println("Hasil B: $resultB")
                println("Total  : ${resultA + resultB}")
            }
            println("Total Waktu: $time")
        }
        println("FINISH PROGRAM | ${Date()}")
    }

    suspend fun countNumber(number: Int): Int {
        delay(1000)
        return number * 10
    }

    @Test
    fun testAwaitAll() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            val time = measureTimeMillis {
                val deferred1 = async { countNumber(1) }
                val deferred2 = async { countNumber(2) }
                val deferred3 = async { countNumber(3) }
                val deferred4 = async { countNumber(4) }

                val result: List<Int> = awaitAll(
                    deferred1, deferred2, deferred3, deferred4
                )

                val total = result.sum()
                println("Hasil per item : $result")
                println("Total          : $total")
            }
            println("Total Waktu    : $time")
        }
        println("FINISH PROGRAM | ${Date()}")
    }

    @Test
    fun testCoroutineContext() {
        runBlocking {
            // Mengakses context dalam runBlocking
            val job = launch {
                // Mengakses context dalam launch
                val context: CoroutineContext = coroutineContext
                println("Context    : $context")
                println("Job        : ${context[Job]}")
                println("Dispatcher : ${context[CoroutineDispatcher]}")
            }
            job.join()
        }
    }

    @Test
    fun testInheritanceCoroutineContext() {
        runBlocking {
            // Context dapat dikirim (misal dispatcher atau job) sebagai parameter
            // Parameter akan digabung dengan default context
            val job = launch(context = Dispatchers.Default + CoroutineName("TestCoroutine")) {
                println("Running on: ${Thread.currentThread().name}")
                println("Coroutine Name: ${coroutineContext[CoroutineName]?.name}")
            }
            job.join()
        }
    }

    @Test
    fun testDispatcher() {
        runBlocking {
            println("Parent Thread - ${Thread.currentThread().name}")

            // Dispatcher Default
            val jobDefault = launch(context = Dispatchers.Default) {
                println("Job Default running in    : ${Thread.currentThread().name}")
            }

            // Dispatcher IO
            val jobIO = launch(context = Dispatchers.IO) {
                println("Job IO running in         : ${Thread.currentThread().name}")
            }

            // Dispatcher Unconfined
            val jobUnconfined = launch(context = Dispatchers.Unconfined) {
                println("Job Unconfined running in : ${Thread.currentThread().name}")
            }

            joinAll(jobDefault, jobIO, jobUnconfined)
        }
    }

    @Test
    fun testWithContext() {
        val dispatcherClient = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
        val dispatcherServer = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

        runBlocking {
            // Mulai coroutine di dispatcherClient (Thread Awal)
            val job = launch(context = dispatcherClient) {
                println("1. Start pada Thread  : ${Thread.currentThread().name}")

                // Pindah ke dispatcherServer (Thread Proses) menggunakan withContext
                val result: String = withContext(context = dispatcherServer) {
                    println("2. Proses pada Thread : ${Thread.currentThread().name}")
                    Thread.sleep(1000) // Simulasi proses berat
                    "Data Success" // Return value
                }

                // Otomatis kembali ke dispatcherClient (Thread Awal)
                println("4. Kembali ke Thread  : ${Thread.currentThread().name}")
                println("Hasil data            : $result")
            }
            job.join()
        }
    }

    @Test
    fun testFinallyError() {
        runBlocking {
            println("START PROGRAM  | ${Date()}")
            val job = launch {
                try {
                    println("Start Coroutine :${Date()}")
                    delay(2000)
                    println("End Coroutine   :${Date()}")
                } finally {
                    println("Masuk Finally")
                    println("Is Active: $isActive")
                    // Masalah: delay() adalah suspend function.
                    // Karena job sudah di-cancel, delay ini akan langsung gagal (throw error)
                    // dan baris di bawahnya tidak akan tereksekusi.
                    delay(1000)
                    println("Log ini tidak akan pernah muncul")
                }
            }

            delay(1000)
            println("Membatalkan job...")
            job.cancelAndJoin()
            println("FINISH PROGRAM | ${Date()}")
        }
    }

    @Test
    fun testNonCancellable() {
        runBlocking {
            println("START PROGRAM  | ${Date()}")
            val job = launch {
                try {
                    println("Start Coroutine :${Date()}")
                    delay(2000)
                    println("End Coroutine   :${Date()}")
                } finally {
                    withContext(context = NonCancellable) {
                        println("Masuk Finally (Non-Cancellable")
                        println("Is Active: $isActive")
                        delay(1000)
                        println("Log tetap muncul!")
                    }
                }
            }
            delay(1000)
            println("Membatalkan job...")
            job.cancelAndJoin()
            println("FINISH PROGRAM | ${Date()}")
        }
    }

    @Test
    fun testCoroutineScope() {
        println("START PROGRAM  | ${Date()}")

        // 1. Membuat Scope sendiri
        //    Scope ini akan menggunakan Dispatcher IO sebagai default contextnya
        val scope = CoroutineScope(context = Dispatchers.IO)

        // 2. Menjalankan coroutine dalam scope
        // launch dibawah merupakan milik scope, bukan GlobalScope
        val job1 = scope.launch(start = CoroutineStart.LAZY) {
            println("Start Job 1 | ${Date()}")
            delay(2000)
            println("End Job 1   | ${Date()}")
        }

        val job2 = scope.launch(start = CoroutineStart.LAZY) {
            println("Start Job 2 | ${Date()}")
            delay(2000)
            println("End Job 2   | ${Date()}")
        }

        runBlocking {
            // Menjalankan Job
            job1.start()
            job2.start()

            delay(1000)
            println("Membatalkan scope...")

            // 3. Membatalkan scope
            //    Saat scope dibatalkan, job1 dan job2 otomatis akan batal
            scope.cancel()

            delay(2000) // Menunggu untuk memastikan tidak ada log "End Job"
        }

        println("FINISH PROGRAM | ${Date()}")
    }

    suspend fun getFoo(): Int {
        delay(1000)
        return 10
    }

    suspend fun getBar(): Int {
        delay(1000)
        return 20
    }

    // Function ini menggunakan coroutineScope untuk memparallelkan proses
    suspend fun getSum(): Int = coroutineScope {
        println("Mulai hitung...")

        // async berjalan di dalam scope milik 'coroutineScope'
        val foo = async { getFoo() }
        val bar = async { getBar() }

        // Menunggu hasil keduanya
        // Jika salah satu error, getSum akan langsung error (batal)
        foo.await() + bar.await()
    }

    @Test
    fun testCoroutineScopeFunction() {
        runBlocking {
            println("START PROGRAM  | ${Date()}")

            // Memanggil function yang di dalamnya ada parallel process
            // Baris ini akan suspend selama 1 detik (bukan 2 detik, karena paralel)
            val result = getSum()
            println("Hasil : $result")

            println("FINISH PROGRAM | ${Date()}")
        }
    }

    @Test
    fun testParentWaitsForChild() {
        runBlocking {
            val parentJob = launch {
                println("Parent Start  : ${Date()}")

                // Launching child
                launch {
                    println("Child Start   : ${Date()}")
                    delay(2000) // Child butuh 2 detik
                    println("Child End     : ${Date()}")
                }

                println("Parent Finish Code (Tapi belum mati)")
            }

            parentJob.join()
            println("Semua selesai : ${Date()}")
        }
    }

    @Test
    fun testParentCancel() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            val parentJob = launch {
                println("Parent Start")

                launch {
                    try {
                        println("Child 1 Start")
                        delay(5000) // Child lama
                        println("Child 1 End")
                    } catch (e: CancellationException) {
                        println("Child 1 Kena Cancel! : $e")
                    }
                }

                launch {
                    try {
                        println("Child 2 Start")
                        delay(5000) // Child lama
                        println("Child 2 End")
                    } catch (e: CancellationException) {
                        println("Child 2 Kena Cancel! : $e")
                    }
                }
            }

            delay(1000) // Biarkan berjalan selama 1 detik (proses child 5 detik)
            println("Membatalkan parent...")
            parentJob.cancel()
            parentJob.join()
        }
        println("FINISH PROGRAM | ${Date()}")
    }

    @Test
    fun testJobParentChild() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            // Membuat instance Parent Job manual
            val masterJob = Job()

            // Membuat scope dengan Parent Job
            // Semua coroutine yang lahir dari sini akan otomatis menjadi Child Job dari Master Job
            val scope = CoroutineScope(context = Dispatchers.IO)

            scope.launch {
                println("Child 1 Start")
                delay(3000)
                println("Child 1 Done")
            }

            scope.launch {
                println("Child 2 Start")
                delay(3000)
                println("Child 2 Done")
            }

            // Membatalkan Master Job (Parent Job)
            delay(1000)
            println("Membatalkan Master Job...")
            masterJob.cancel()

            // child1 dan child2 akan berhenti dan tidak mencetak "Done"
            masterJob.join() // Menunggu proses pembatalan selesai
        }
        println("FINISH PROGRAM | ${Date()}")
    }

    @Test
    fun testBreakingRelationship() {
        runBlocking {
            val parentJob = launch {
                println("Parent Start")

                // MASALAH: Mengirimkan Job() baru sebagai context
                // Akibatnya: Coroutine ini BUKAN anak dari parentJob
                // Dia menjadi "Anak Tiri" yang punya Job sendiri
                launch(Job()) {
                    println("Child (Independent) Start")
                    delay(3000)
                    println("Child (Independent) Done") // Ini akan tetap jalan walau parent mati
                }
            }

            delay(1000)
            println("Membatalkan Parent...")
            parentJob.cancel() // Cancel parent

            delay(3000) // Menunggu pembuktian
        }
    }

    @Test
    fun testCancelChildren() {
        println("START PROGRAM  | ${Date()}")
        runBlocking {
            // 1. Membuat Parent Job dan Scope
            val parentJob = Job()
            val scope = CoroutineScope(Dispatchers.IO + parentJob)

            // 2. Meluncurkan Anak Pertama (Child 1)
            val child1 = scope.launch {
                println("Child 1 Start")
                delay(2000)
                println("Child 1 Selesai (Tidak akan tercetak)")
            }

            delay(500) // Biarkan Child 1 jalan sebentar

            // 3. Membatalkan HANYA Anak-anaknya
            println("Membatalkan semua children...")
            parentJob.cancelChildren()
            // parentJob.cancel() <-- Kalau pakai ini, Child 2 di bawah tidak akan jalan!

            // 4. Meluncurkan Anak Kedua (Child 2)
            // Karena Parent masih hidup, Child 2 BISA jalan
            val child2 = scope.launch {
                println("Child 2 Start (Parent masih hidup!)")
                delay(1000)
                println("Child 2 Selesai")
            }

            child2.join() // Menunggu Child 2
        }
        println("FINISH PROGRAM | ${Date()}")
    }
}