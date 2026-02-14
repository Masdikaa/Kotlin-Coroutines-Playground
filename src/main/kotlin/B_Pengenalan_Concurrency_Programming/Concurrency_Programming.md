# Concurrency Programming

Konsep Concurrency sering diaplikasikan dalam banyak pemrograman lainya

## Parallel vs Concurrency

- Berbeda dengan parallel yang menjalankan beberapa task secara bersamaan, concurrency adalah
  menjalankan beberapa pekerjaan secara bergantian
- Dalam parallel programming, akan dibutuhkan banyak Thread, namun pada Concurrency akan menggunakan
  lebih sedikit thread

**⟶ Parallel Programming**: Menjalankan beberapa task benar-benar bersamaan

**⟶ Concurrency Programming**: Mengelola banyak task yang aktif dalam waktu yang sama

### Analogi

**Parallel**

Empat koki dengan empat kompor juga

- Semua koki masak dalam waktu yang bersamaan dengan semua kompor yang ada
- Benar benar simultan

**Concurrency**

Satu koki banyak pesanan

- Masak mie
- Sambil menunggu air mendidih koki potong sayur
- Dijalankan secara bergantian

## CPU Bound

Kapan menggunakan Parallel/Concurrency ?

Dalam task biasa terdapat 2 jenis proses

- CPU Bound
    - Banyak Algoritma yang hanya membutuhkan CPU untuk menjalankanya, algoritma jenis ini akan
      sangat tergantung dengan kecepatan CPU
    - Machine Learning sebagai contohnya, oleh karena itu sekarang banyak sekali teknologi Machine
      Learning yang banyak menggunakan GPU karena memiliki core yang lebih banyak dibanding CPU
      biasanya
    - Jenis algoritma seperti ini tidak ada benefitnya menggunakan Concurrency Programming, namun
      bisa dibantu dengan implementasi Parallel Programming

- IO Bound
    - Tidak membutuhkan kecepatan CPU tinggi, namun bergantung pada kecepatan **Input Output (IO)**
      device yang digunakan
    - Contohnya seperti membaca data dari file, database, dll
    - Concurrency programming akan memberikan benefit lebih banyak daripada Parallel Programming
      dalam kasus ini
    - Bayangkan kita membaca data dari database, dan Thread harus menunggu 1 detik untuk mendapat
      balasan dari database, padahal waktu 1 detik itu jika menggunakan Concurrency Programming,
      bisa digunakan untuk melakukan hal lain lagi

### Problem di Java Thread

Java thread tidak didesain untuk melakukan Concurrency, Java Thread hanya bisa melakukan satu hal
sampai selesai baru melakukan hal lain

## Pengenalan Coroutine

Unit kerja ringan (lightweight task) yang bisa **ditunda**, **dilanjutkan**, dan **dipindahkan**
antar thread tanpa memblokir thread itu sendiri (Thread Blocking)

Coroutine sebenarnya dieksekusi dalam sebuah Thread, namun dengan Coroutine sebuah thread bisa
memiliki kemampuan untuk menjalankan beberapa coroutine secara bergantian (concurent)

Kelebihan menggunakan Coroutine adalah murah dan cepat, sehingga tidak perlu mengkhawatirkan
pembuatan banyak coroutine karena tidak akan membebani memory

## Suspend Function

Suspend Function adalah fungsi yang bisa berhenti sementara (_suspend_) dan dilanjutkan(_resume_)
kembali tanpa thread-blocking

- Suspend computation adalah komputasi yang bisa ditangguhkan (ditunda waktu eksekusinya)
- Pada java thread, untuk menangguhkan komputasi menggunakan `Thread.sleep()` sayangnya
  `Thread.sleep()` akan mem-block thread yang sedang berjalan saat ini. Sehingga tidak bisa
  digunakan
- Syarat menjalankan suspend function di Kotlin adalah, harus dipanggil dari suspend function
  lainnya

**Aturan Emas Suspend Function**

1. Hanya bisa dipanggil dari:
    - Coroutine (`launch`, `async`)
    - Suspend function lainya

2. Suspend function tidak sama dengan Thread baru

Suspend Fun :

```kotlin
@Test
fun testSuspendFunction() {
    runBlocking { // Running Coroutine and Block Thread
        sayHello("Masdika")
    }
}

suspend fun sayHello(name: String) {
    delay(1000) // Delay is a suspend function
    println("Hello - ${Thread.currentThread().name} | ${Date()}")
    delay(1000)
    println("$name - ${Thread.currentThread().name} | ${Date()}")
}
```

Output:

```
Hello - Test worker @coroutine#1 | Tue Feb 10 11:50:13 WIB 2026
Masdika - Test worker @coroutine#1 | Tue Feb 10 11:50:14 WIB 2026
```

`delay()` merupakan `suspend fun` dan suspend fun **hanya bisa dipanggil** dari suspend fun lain
atau
didalam coroutine, maka dari itu function `sayHello()` harus berbentuk suspend fun agar bisa
memanggil `delay()`

`runBlocking()` adalah kode yang digunakan untuk menjalankan Coroutine namun memblokir Thread.
**Jangan diimplementasikan** pada real-project

Perhatikan output dari program, program berjalan selama 2 detik dalam thread yang sama
`Test worker @coroutine#1`, ini membukatikan bahwa program bukan berjalan secara parallel melainkan
concurrent

## Membuat Coroutine

Coroutine tidak bisa berjalan sendiri, Coroutine perlu berjalan didalam sebuah **Scope**. Salah satu
Scope yang bisa digunakan adalah **Global Scope**

Fungsi `launch()` akan digunakan untuk membuat coroutine dan didalamnya bisa digunakan untuk
memanggil suspend function

- Banyak scope dalam Coroutine dan akan dibahas pada materi berikutnya

```kotlin
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
    delay(1000)
    println("$name - ${Thread.currentThread().name} | ${Date()}")
}
```

Output:

```
START PROGRAM  | Tue Feb 10 11:53:26 WIB 2026
Hello - DefaultDispatcher-worker-2 @coroutine#1 | Tue Feb 10 11:53:27 WIB 2026
Masdika - DefaultDispatcher-worker-2 @coroutine#1 | Tue Feb 10 11:53:28 WIB 2026
FINISH PROGRAM | Tue Feb 10 11:53:29 WIB 2026
```

Coroutine berjalan pada GlobalScope dan runBlocking digunakan untuk menunggu GlobalScope
menyelesaikan coroutinenya karena secara default jika Main Thread berhenti maka semua coroutine juga
akan berhenti

### Coroutine Sangat Ringan

Perbandingan harga dari Thread dan Coroutine

1. Thread
   ```kotlin
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
    ```
   Output:
    ```
    * What went wrong:
    Out of memory. unable to create native thread: possibly out of memory or process/resource limits reached
    ```
   Membuat sebuah banyak thread(100.000 thread) yang dijalankan secara bersamaan dalam 1 detik
   cenderung memakan memory yang banyak dan boros sehingga program akan berhenti karena memory yang
   tidak mencukupi

2. Coroutine
    ```kotlin
    @Test
    fun testManyCoroutine(){
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
    ```
   Output:
    ```
    Coroutine 99998 | Tue Feb 10 12:21:38 WIB 2026
    Coroutine 99997 | Tue Feb 10 12:21:38 WIB 2026
    Coroutine 99999 | Tue Feb 10 12:21:38 WIB 2026
    Coroutine 99084 | Tue Feb 10 12:21:38 WIB 2026
    Coroutine 99159 | Tue Feb 10 12:21:38 WIB 2026
    Coroutine 99157 | Tue Feb 10 12:21:38 WIB 2026
    Coroutine 98820 | Tue Feb 10 12:21:38 WIB 2026
    Coroutine 99156 | Tue Feb 10 12:21:38 WIB 2026
    Coroutine 99852 | Tue Feb 10 12:21:38 WIB 2026
    FINISH PROGRAM | Tue Feb 10 12:21:46 WIB 2026
    ```
   Coroutine dapat handling 100.000 Task secara efisien

## Job

Job adalah representasi dari coroutine yang **sedang** / **akan** / dan **sudah** dijalankan

Saat sebuah coroutine dijalankan menggunakan function `launch()` sebenarnya function tersebut
mengembalikan object Job

Dengan object job, coroutine dapat dijalankan, dibatalkan atau menunggu sebuah coroutine

**Mengapa `launch()` mengembalikan Job**

- `launch()` merupakan operasi _fire-and-forget_
- tidak ada return value
- dan perlu dikontrol

Dengan menggunakan job memungkinkan untuk:

- Mengecek status
- Membatalkan coroutine
- Menunggu sampai selesai
- Mengatur relasi parent–child

Pada implementasi code Coroutine sebelumnya yang menggunakan `runBlocking()` adalah implementasi
yang kurang baik, karena `runBlocking()`

`Runs a new coroutine and blocks the current thread interruptibly until its completion.`

Menjalankan coroutine baru dan memblokir thread yang sedang berjalan hingga thread selesai

Namun konsep tersebut hanya berjalan didalam scope `runBlocking()` saja, dan tidak berlaku jika
diluarnya

```kotlin
@Test
fun testRunBlocking() {
    runBlocking {
        GlobalScope.launch {
            delay(2000)
            println("Finish Coroutine - ${Thread.currentThread().name}")
        }
    }
}
```

Kode diatas tidak akan menghasilkan output apapun

`runBlocking()` hanya menunggu coroutine yang berada **DI DALAM** scope-nya,
sementara `GlobalScope.launch()` berada **DI LUAR** scope tersebut

```
runBlocking scope
└── (tidak punya child)

GlobalScope
└── coroutine launch (delay 2 detik)
```

`delay()` tidak akan mengolong karena `delay()` adalah **suspend** (tertunda) dan belum di resume

### Menjalankan Job

```kotlin
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
```

Output:

```
START PROGRAM  | Tue Feb 10 21:00:56 WIB 2026
Finish Coroutine - DefaultDispatcher-worker-2 @coroutine#2
FINISH PROGRAM  | Tue Feb 10 21:00:59 WIB 2026
```

Saat menjalankan `launch()`, return valuenya adalah **Job** yang merupakan representasi dari
Coroutine

Intinya return value dari Coroutine adalah Job

Secara default saat menjalankan `launch()` secara otomatis akan menjalankan Coroutine/Jobnya

Dengan menggunakan paramter `start = CoroutineStart.LAZY` pada launch, yang dimana dia akan running
ketika `job.start()`. Saat `job.start()` tidak dijalankan maka tidak akan menjalankan apapun dan
menampilkan kosong

### Menunggu Job

Menunggu Job untuk menyelesaikan tasknya dapat menggunakan function `join()`

```kotlin
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
```

Output:

```
START PROGRAM  | Tue Feb 10 21:03:03 WIB 2026
Finish Coroutine - DefaultDispatcher-worker-1 @coroutine#2
FINISH PROGRAM  | Tue Feb 10 21:03:05 WIB 2026
```

Dengan menggunakan `job.join()` coroutine akan di suspend sampai jobnya selesai, dengan menggunakan
`join()` tidak perlu lagi menggunakan `delay()`

### Membatalkan Job

Membatalkan Job dapat menggunakan function `cancel()`

```kotlin
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
```

Output:

```
START PROGRAM  | Tue Feb 10 21:08:05 WIB 2026
WAITING
FINISH PROGRAM  | Tue Feb 10 21:08:08 WIB 2026
```

Dengan cancel, job tidak akan dijalankan dan langsung ke akhir program

### `joinAll` Function

Dalam pengembangan aplikasi, sering terdapat kebutuhan untuk menjalankan banyak coroutine secara
bersamaan

Jika menggunakan function `join()` biasa, kode akan menjadi repetitif karena harus memanggil
`join()` satu persatu

Untuk mengatasi masalah tersebut, Kotlin sudah menyediakan function `joinAll()` menerima parameter
berupa vararg Job atau collection of Job dan akan menunda eksekusi sampai semua job didaftarkan

```kotlin
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
```

Output:

```
START PROGRAM  | Wed Feb 11 08:35:14 WIB 2026
Job 1 Selesai - Wed Feb 11 08:35:15 WIB 2026
Job 2 Selesai - Wed Feb 11 08:35:16 WIB 2026
FINISH PROGRAM  | Wed Feb 11 08:35:16 WIB 2026
```

## Cancellable Coroutine

Materi sebelumnya telah dipelajari bahwa function `cancel()` digunakan untuk membatalkan Job, namun
perlu dipahami bahwa pembatalan Job bersifat **Cooperative**

Artinya, kode program dalam Coroutine harus "mau" atau "bisa" dibatalkan. Jika sebuah coroutine
sedang melakukan proses yang berat dan tidak menerima status pembatalan, maka coroutine tersebut *
*tidak akan berhenti** meskipun `cancel()` sudah dipanggil

`cancel()` sebenarnya hanya mengirimkan signal kepada Job bahwa ia harus berhenti, dan jika kode
didalamnya tidak peduli sinyal tersebut maka kode akan tetap dijalankan

**Implementasi Kode (Bermasalah)**
Berikut adalah kode dimana coroutine **gagal** dibatalkan karena menggunakan `Thread.sleep()` (
bersifat blocking dan tidak mengecek cancellation) alih alih `delay()` (bersifat suspend dan
mengecek cancellation)

**Uncancelable Coroutine**

```kotlin
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
```

Output:

```
START PROGRAM  | Wed Feb 11 08:52:37 WIB 2026
Membatalkan Job...
End Coroutine: Wed Feb 11 08:52:39 WIB 2026
FINISH PROGRAM  | Wed Feb 11 08:52:39 WIB 2026
```

Meskipun `job.cancel()` dipanggil setelah 1 detik, pesan `"End Coroutine"` akan tetap muncul setelah
2 detik. Ini membuktikan bahwa coroutine tersebut tidak bisa dibatalkan (non-cancellable) karena
kodenya tidak kooperatif.

**Cancellable Coroutine (Cooperative)**

Agar coroutine dapat dibatalkan, kode didalamnya harus secara aktif menerima status pembatalan dan
terdapat 2 cara untuk melakukan ini:

1. Menggunakan property `isActive`: akan bernilai true jika coroutine masih aktif dan false jika
   sudah dibatalkan
2. Menggunakan function `ensureActive()`: Fungsi ini akan mengecek apakah Job masih aktif. Jika
   tidak function ini akan melempar CancellationException untuk menghentikan paksa proses yang
   sedang berjalan

```kotlin
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
```

Output:

```
START PROGRAM  | Thu Feb 12 15:39:53 WIB 2026
Start Coroutine: Thu Feb 12 15:39:53 WIB 2026
Processing 1: Thu Feb 12 15:39:53 WIB 2026
Processing 2: Thu Feb 12 15:39:54 WIB 2026
Processing 3: Thu Feb 12 15:39:54 WIB 2026
Membatalkan Job...
FINISH PROGRAM  | Thu Feb 12 15:39:55 WIB 2026
```

## Setelah Coroutine Dibatalkan

Ketika sebuah coroutine dibatalkan dengan function `cancel()`, secara internal coroutine tersebut
akan melempar error bernama `CancellationException`. Karena dianggap sebagai exception, maka
mekanisme standard try-catch-finally dalam kotlin dapat dimanfaatkan

Blok `finally` adalah blok kode yang akan selalu dieksekusi, oleh karena itu ini blok ini adalah
blok yang tepat untuk dimanfaatkan sebagai **resource cleanup**

- Menutup koneksi database
- Menutup file yang sedang dibuka
- Menyimpan log

```kotlin
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
```

Output:

```
START PROGRAM  | Fri Feb 13 07:08:47 WIB 2026
Start Coroutine
Membatalkan Job,,,,,
Finish Coroutine - Cleanup Code
FINISH PROGRAM | Fri Feb 13 07:08:48 WIB 2026
```

## Timeout

Dalam sebuah aplikasi seringkali eksekusi sebuah program memakan waktu yang lebih lama dari yang
diharapkan, misalnya koneksi dari server sedang bermasalah seingga server tidak merespon request
data

Menunggu proses tersebut tanpa batas waktu adalah (_indefinite waiting_) adalah praktik yang buruk
yang membuat aplikasi seolah olah macet atau berhenti

Kotlin Coroutine menyediakan mekanisme bawaan untuk membatasi durasi eksekusi sebuah coroutine
menggunakan function `withTimeout()`

**Cara Kerja**

Function `withTimeout(time)` akan menjalankan blok kode didalamnya. Jika proses selesai sebelum
waktu habis, hasilnya akan dikembalikan. Namun, jika durasinya melebihi waktu yang ditentukan,
function ini akan membatalkan coroutine secara paksa dan mengembalikan
`TimeoutCancellationException`

Terdapat juga `withTimeOutOrNull()` untuk mengembalikan null daripada `TimeoutCancellationException`

```kotlin
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
```

Output:

```
START PROGRAM  | Fri Feb 13 07:21:43 WIB 2026
Start Coroutine
0. Masih proses... Fri Feb 13 07:21:43 WIB 2026
1. Masih proses... Fri Feb 13 07:21:44 WIB 2026
FINISH PROGRAM | Fri Feb 13 07:21:44 WIB 2026
```

Implementasi kode diatas adalah contoh dari timeout yang melempar `TimeoutCancellationException`

```kotlin
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
```

Output:

```
START PROGRAM  | Fri Feb 13 07:27:59 WIB 2026
Start Coroutine
Proses Timeout (Waktu habis)
Finish Coroutine - Result: null
FINISH PROGRAM | Fri Feb 13 07:28:00 WIB 2026
```

Dan diatas adalah contoh implementasi dari `withTimeoutOrNull()` untuk mengembalikan nilai Null jika
operasi melewati batas waktu timeout

## Sequential Suspend Function

Secara default kode didalam sebuah coroutine akan dieksekusi secara berurutan (_sequential_). Jika
terdapat beberapa _suspend function_ yang dipanggil, **function kedua tidak akan djialankan sebelum
function pertama selesai dieksekusi**

Sifat ini memiliki implikasi terhadap performa jika dua proses sebenarnya tidak saling bergantungan
tetapi dijalankan secara berurutan, maka waktu total eksekusi adalah **penjumlahan** dari waktu
kedua
proses tersebut

```kotlin
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
```

Ouput:

```
START PROGRAM  | Fri Feb 13 21:24:36 WIB 2026
Hasil A: 10
Hasil B: 20
Total  : 30
Total Waktu: 2006
FINISH PROGRAM | Fri Feb 13 21:24:38 WIB 2026
BUILD SUCCESSFUL in 4s
```

Total waktu menunjukan 2000 millis menunjukan keseluruhan proses akan memakan waktu 2 detik yang
dijalankan sequential

## Async Function

Sebelumnya dijelaskan bahwa secara default kode yang ada didalam coroutine berjalan secara
**sequential**

Untuk mengatasi hal ini, kotlin menyediakan function `async`

**Perbedaan `Launch` dan `Async`**

- `launch`: Digunakan untuk memulai coroutine yang **tidak mengembalikan hasil** dan mengembalikan
  object **Job**
- `asycn`: Digunakan untuk memulai coroutine yang **mengembalikan hasil** dan function ini akan
  mengembalikan object `Deferred<T>`

**Deferred**

Deferred adalah turunan dari Job yang memiliki kemampuan untuk membawa return value. Anggap deferred
sebagai "**Janji**" (Future) bahwa nilai tersebut akan tersedia dimasa depan. dan untuk menangkap
hasilnya akan menggunakan function `await()`

```kotlin
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
```

Output

```
START PROGRAM  | Sat Feb 14 06:38:05 WIB 2026
Hasil A: 10
Hasil B: 20
Total  : 30
Total Waktu: 1011
FINISH PROGRAM | Sat Feb 14 06:38:06 WIB 2026
```

Jika pada kode sebelumnya yang tidak menggunakan `async` total waktu akan mencapai 2 detik karena
djialankan secara sequential

Dan kode diatas membuktikan bahwa kode hanya memakan waktu 1 detik, karena dijalankan secara
asynchronous

### `awaitAll()` Function

Function `awaitAll()` digunakan untuk menunggu sekumpulan proses `async` selesai secara bersamaan
dan mengembalikan hasil akhirnya dalam bentuk **list**

```kotlin
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
```

Output:

```
START PROGRAM  | Sat Feb 14 06:57:54 WIB 2026
Hasil per item : [10, 20, 30, 40]
Total          : 100
Total Waktu    : 1014
FINISH PROGRAM | Sat Feb 14 06:57:55 WIB 2026
```

## Coroutine Context

Coroutine Context adalah sekumpulan data atau konfigurasi yang menentukan perilaku dari sebuah
coroutine. Ini adalah "_lingkungan_" atau "_parameter_" tempat coroutine tersebut berjalan

Context ini sebenarnya merupakan sebuah **map** yang menyimpan elemen-elemen penting dan dua yang
paling utama diantaranya:

- **Job**: Mengatur lifecycle coroutine
- **Dispatcher**: Mengatur di thread mana coroutine akan dijalankan (materi beriktunya)

Setiap coroutine pasti memiliki context. Saat membuat coroutine (launch/async) dapat menyisipkan
context tertentu, atau mewarisi context dari scope induk

```kotlin
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
```

Output:

```
Context    : [CoroutineId(2), "coroutine#2":StandaloneCoroutine{Active}@3d3ba765, BlockingEventLoop@25bc0606]
Job        : "coroutine#2":StandaloneCoroutine{Active}@3d3ba765
Dispatcher : BlockingEventLoop@25bc0606
```

`coroutineContext` adalah property global dalam scope coroutine untuk mengambil context saat ini

**Inheritance Context**

```kotlin
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
```

Output:

```
Running on: DefaultDispatcher-worker-1 @TestCoroutine#2
Coroutine Name: TestCoroutine
```

Operator `+` : Coroutine context memiliki kemampuan unik untuk digabungkan menggunakan operator `+`.
Ini akan menggabungkan konfigurasi thread pool (Dispatcher) dengan nama coroutine untuk debugging

**Note in Couroutine Context**

- Job ᯓ➤ Lifecycle (Siklus hidup menentukan kapan job mulai, berhenti, mati, atau dihancurkan)
- Dispatcher ᯓ➤ Threading (Menentukan thread yang digunakan coroutine)

## Coroutine Dispatcher

Dispatcher adalah komponen dalam **Context** yang menentukan pada **thread** mana Coroutine
dijalankan

Secara default, jika nilai dispatcher tidak ditentukan maka coroutine akan berjalan pada
`Dispatchers.Default`

**Important**

Dalam aplikasi nyata, pemiliihan Dispatcher yang tepat **sangat krusial** agar aplikasi tetap
responsif dan efisien

Kotlin menyediakan standard Dispatcher yang siap digunakan :

- `Dispatchers.Default`
    - Digunakan untuk operasi **CPU Bound** (Image processing, Machine Learning, Short Algorithm)
    - Menggunakan thread pool yang jumlahnya sama dengan core pada CPU

- `Dispatchers.IO`
    - Digunakan untuk proses Input/Output **IO Bound** (Get API Data, Read & Write Database, File
      Access)
    - Menggunakan thread pool yang dapat mengembang (elastis) hingga 64 thread (atau lebih) untuk
      menangani banyak request tunggu sekaligus

- `Dispatchers.Main`
    - Khusus untuk ekosistem UI seperti Android / JavaFX
    - Berjalan di Main Thread **UI-Thread**. Digunakan hanya untuk memperbarui UI dan tidak untuk
      melakukan proses berat
    - _Note_: Untuk menggunakan dispatcher ini pada Unit Test, diperlukan library tambahan
      `kotlinx-coroutines-test`

- `Dispatcher.Unconfined`
    - Dispatcher yang tidak memiliki pendirian. Ia memulai coroutine di thread pemanggilnya, tetapi
      setelah disuspend ia bisa melanjutkan eksekusi di thread mana saja (tergantung suspend
      function yang dipanggil)
    - Jarang digunakan dalam implementasi aplikasi pada umumnya

**Thread Pool**

Mekanisme manajemen thread untuk menyediakan sekumpulan thread yang sudah dibuat (pre-initialized)
dan siap bekerja (standby), alih alih membuat thread baru setiap ada tugas

```kotlin
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
```

Output:

```
Parent Thread - Test worker @coroutine#1
Job Default running in    : DefaultDispatcher-worker-1 @coroutine#2
Job IO running in         : DefaultDispatcher-worker-1 @coroutine#3
Job Unconfined running in : Test worker @coroutine#4
```

**Note**

Sejak kotlin versi 1.3.30, `Dispatchers.Default` dan `Dispatchers.IO` berbagi **Thread Pool** yang
sama.

Hal tersebut menjelaskan mengapa nama Default Dispatcher dan IO Dispatcher menggunakan nama yang
sama, karena memang kolamnya sama

Mesikpun memliki kolam yang sama, Kebijakan / Policy dari penggunaanya berbeda

- Default : Dibatasi sejumlah Core CPU

  Jika laptop memiliki 8 Core, maka Dispatchers.Default hanya akan menggunakan maksimal 8 thread
  dari kolam tersebut secara bersamaan. Ini agar CPU tidak overload.
- IO : Dibatasi hingga 64 thread (atau lebih, tergantung konfigurasi)

  Ini memungkinkan aplikasi melakukan banyak request network/database sekaligus (karena tugas ini
  hanya menunggu, tidak memakan CPU).

## `withContext` Function

Ada kalanya dalam sebuah alur bisnis, diperlukan untuk berpindah pindah thread:

- Mulai di Main Thread (UI)
- Pindah ke IO Thread (Ambil data Database)
- Kembali ke Main Thread (Tampilkan Data)

Jika menggunakan cara lama, dapat dilakukan dengan menggunakan nesting coroutine yang merupakan
pendekatan yang buruk untuk memanggil IOThread didalam MainThread

Cara yang modern dapat dilakukan dengan `withContext`

**withContext** merupakan suspend function yang memungkinkan untuk mengubah context (normally
dispatcher) hanya **untuk blok kode tertentu**

Setelah blok kode tersebut selesai eksekusi akan dikembalikan ke Dispatcher awal secara otomatis

Function ini juga mengembalikan value, sehingga cocok digunakan untuk mengambil hasil dari proses
thread lain

**Implementasi**

Berikut alur yang akan diimplementasikan :

1. Start dari Default Dispatcher worker 1
2. Menjalankan proses Default Dispatcher worker 2 (Berubah ke IO)
3. Kembali ke Default Dispatcher worker 1

```kotlin
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
```

Output:

```
1. Start pada Thread  : pool-1-thread-1 @coroutine#2
2. Proses pada Thread : pool-2-thread-1 @coroutine#2
4. Kembali ke Thread  : pool-1-thread-1 @coroutine#2
Hasil data            : Data Success
```

Dari output kode diatas, proses dijalankan pada pool yang berbeda saat dengan pool mulai dan dapat
kembali ke pool mulai dengan sebuah value

## Non-Cancellable Context

Telah dipelajari bahwa ketika sebuah coroutine dibatalkan maka `CancellationException` akan dilempar
dan ditangkap dalam blok `finally` untuk dibersihkan

Namun, terdapat satu batasan penting

**Kode didalam blok `finally` tidak boleh menunda terlalu lama atau memanggil suspend function
lain**. Mengapa ?, karena coroutine tersebut statusnya sudah **Cancelled**

Jika sebuah suspend function (seperti delay) dipanggil di dalam blok finally pada coroutine yang
sudah batal, maka function tersebut **akan langsung batal lagi** (melempar CancellationException
seketika).

Hal ini menjadi masalah jika proses pembersihan itu sendiri membutuhkan waktu atau berupa proses
async (misalnya: menutup koneksi database secara graceful atau mengirim sinyal "Logout" ke server).

Untuk mengatasi hal ini, Kotlin menyediakan object khusus bernama **NonCancellable**. Dengan
menggunakan
context ini, blok kode tertentu dapat **dipaksa** untuk tetap berjalan hingga selesai meskipun
parent
coroutine-nya sudah dibatalkan.

**Error Test**

```kotlin
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
```

Output:

```
START PROGRAM  | Sat Feb 14 21:55:07 WIB 2026
Start Coroutine :Sat Feb 14 21:55:07 WIB 2026
Membatalkan job...
Masuk Finally
FINISH PROGRAM | Sat Feb 14 21:55:08 WIB 2026
```

Log dalam finally tidak akan pernah dijalankan karena `delay` gagal untuk dieksekusi

**Solusi dengan NonCancellable**

```kotlin
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
                    println("Masuk Finally (Non-Cancellable)")
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
```

Output:

```
START PROGRAM  | Sat Feb 14 21:59:02 WIB 2026
Start Coroutine :Sat Feb 14 21:59:02 WIB 2026
Membatalkan job...
Masuk Finally (Non-Cancellable)
Log tetap muncul!
FINISH PROGRAM | Sat Feb 14 21:59:04 WIB 2026
```

Semua log didalam blok `withContext(NonCancellable)` akan tercetak meskipun Job utamanya sudah
dibatalkan