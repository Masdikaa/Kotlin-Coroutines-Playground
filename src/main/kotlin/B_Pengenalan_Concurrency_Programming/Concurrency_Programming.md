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