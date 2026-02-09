# Parallel Programming

Parallel Programming adalah proses untuk memecahkan suatu masalah dengan membaginya menjadi bagian
bagian kecil, dan dijalankan secara bersamaan pada waktu yang bersamaan pula

Proses atau task akan dibagi dan dijalankan pada **processor core yang terpisah** sehingga lebih
efisien waktu

"Banyak Koki yang membuat banyak menu masakan secara bersamaan"

## Process and Thread

| **Process**                                   | **Thread**                                                  |
|:----------------------------------------------|:------------------------------------------------------------|
| Process adalah sebuah eksekusi program        | Thread adalah segmen dari process                           |
| Process mengkonsumsi memory besar             | Thread menggunakan memory kecil                             |
| Process saling terisolasi dengan process lain | Thread bisa saling berhubungan jika dalam process yang sama |
| Process lama untuk dijalankan dihentikan      | Thread cepat untuk dijalankan dan dihentikan                |

## Main Thread

**Main Thread** adalah tempat utama aplikasi berjalan

Program utama dalam sebuah aplikasi akan otomatis dijalankan dalam **Main Thread**

Saat menjalankan Unit Test, proses tersebut juga akan dijalankan dalam sebuah Thread tersendiri,
begitu juga saat mengembangkan Aplikasi Android, App tersebut akan dijalankan dalam sebuah Thread

```kotlin
fun main() {
    val thread = Thread.currentThread().name
    println("Hello World - $thread")
}
```

Output : `Hello World - main`

## Membuat Thread

- Kotlin menggunakan Java Thread, sehingga cara membuatnya akan sama dengan Java
- Untuk membuat Thread, bisa menggunakan interface Runnable sebagai kode yang akan dieksekusi, lalu
  menggunakan method `Thread.start()` untuk menjalankan thread tersebut
- **Ingat**, Thread akan berjalan secara paralel sehingga tidak akan menunggu Main Thread
- Kotlin memiliki helper function `thread()` untuk mempermudah membuat thread

Code :

```kotlin
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
```

Output :

```
START PROGRAM - Mon Feb 09 14:36:21 WIB 2026
Mulai Thread - Mon Feb 09 14:36:21 WIB 2026
Hello World!
Selesai Thread - Mon Feb 09 14:36:23 WIB 2026
FINISH PROGRAM - Mon Feb 09 14:36:24 WIB 2026
```

## Multiple Thread

Tidak ada batasan dalam membuat thread, semua akan berjalan sendiri sendiri secara paralel

```kotlin
@Test
fun multipleThread() {
    println("START MAIN THREAD /n")
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
    println("/nFINISH MAIN THREAD")
}
```

Output :

```
START MAIN THREAD 

Mulai Thread 2    - Mon Feb 09 14:48:44 WIB 2026
Mulai Thread 1    - Mon Feb 09 14:48:44 WIB 2026
Nama thread 2     : Thread-4- Mon Feb 09 14:48:45 WIB 2026
Nama thread 1     : Thread-3 - Mon Feb 09 14:48:45 WIB 2026
Selesai Thread 2  - Mon Feb 09 14:48:45 WIB 2026
Selesai Thread 1  - Mon Feb 09 14:48:46 WIB 2026

FINISH MAIN THREAD
```

## Executor Service

### Masalah dengan Thread

- Thread adalah object yang mahal, sekitar 512kb - 1MB
- Penggunaan thread secara manual sangat tidak disarankan
- Disarankan untuk menggunakan ulang thread jika sudah selesai digunakan

Dari permasalahan diatas, Executor Service adalah solusi yang diberikan Java

- Executor Service adalah fitur dari JVM yang digunakan untuk manajemen thread
- Executor Service adalah interface, objectnya dapat dibuat menggunakan class Executors

Helper Method dalam Executor Service:

| Method                    | Keterangan                                                            |
|:--------------------------|:----------------------------------------------------------------------|
| `newSingleThreadExecutor` | Membuat ExecutorService dengan 1 thread                               |
| `newFixedThreadPool(int)` | Membuat ExecutorService dengan n thread                               |
| `newCachedThreadPool()`   | Membuat ExecutorService dengan thread akan meningkat sesuai kebutuhan |

### Threadpool

- Implementasi Executor Service yang terdapat dalam interface `Executors` adalah class
  `ThreadPoolExecutor`
- Didalam ThreadPool terdapat data queue tempat penyimpanan semua proses sebelum dieksekusi
- Dengan ini Runnable dapat dieksekusi sebanyak banyaknya walaupun thread tidak cukup untuk semua
  Runnable
- Runnable yang tidak dieksekusi akan menunggu di queue sampai Thread sudah selesai mengeksekusi
  Runnable lainya

### Menggunakan Executor Service

**Single Thread Executor**

```kotlin
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
```

Output:

```
START MAIN THREAD

Insert runnable 1 kedalam thread pool - Mon Feb 09 15:10:16 WIB 2026
Insert runnable 2 kedalam thread pool - Mon Feb 09 15:10:16 WIB 2026
Insert runnable 3 kedalam thread pool - Mon Feb 09 15:10:16 WIB 2026
Insert runnable 4 kedalam thread pool - Mon Feb 09 15:10:16 WIB 2026
Insert runnable 5 kedalam thread pool - Mon Feb 09 15:10:16 WIB 2026
Insert runnable 6 kedalam thread pool - Mon Feb 09 15:10:16 WIB 2026
Insert runnable 7 kedalam thread pool - Mon Feb 09 15:10:16 WIB 2026
Insert runnable 8 kedalam thread pool - Mon Feb 09 15:10:16 WIB 2026
Insert runnable 9 kedalam thread pool - Mon Feb 09 15:10:16 WIB 2026
Insert runnable 10 kedalam thread pool - Mon Feb 09 15:10:16 WIB 2026

WAITING

Done 1 - Mon Feb 09 15:10:17 WIB 2026 in pool-1-thread-1
Done 2 - Mon Feb 09 15:10:18 WIB 2026 in pool-1-thread-1
Done 3 - Mon Feb 09 15:10:19 WIB 2026 in pool-1-thread-1
Done 4 - Mon Feb 09 15:10:20 WIB 2026 in pool-1-thread-1
Done 5 - Mon Feb 09 15:10:21 WIB 2026 in pool-1-thread-1
Done 6 - Mon Feb 09 15:10:22 WIB 2026 in pool-1-thread-1
Done 7 - Mon Feb 09 15:10:23 WIB 2026 in pool-1-thread-1
Done 8 - Mon Feb 09 15:10:24 WIB 2026 in pool-1-thread-1
Done 9 - Mon Feb 09 15:10:25 WIB 2026 in pool-1-thread-1
Done 10 - Mon Feb 09 15:10:26 WIB 2026 in pool-1-thread-1

FINISH MAIN THREAD
```

Karena Thread hanya ada 1 maka proses runnable akan dijalankan satu persatu (perhatikan date), namun
akan dimasukan secara bersamaan

**Fixed Thread Executor**

```kotlin
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
```

Output:

```
START MAIN THREAD

Insert runnable 1 kedalam thread pool - Mon Feb 09 15:16:10 WIB 2026
Insert runnable 2 kedalam thread pool - Mon Feb 09 15:16:10 WIB 2026
Insert runnable 3 kedalam thread pool - Mon Feb 09 15:16:10 WIB 2026
Insert runnable 4 kedalam thread pool - Mon Feb 09 15:16:10 WIB 2026
Insert runnable 5 kedalam thread pool - Mon Feb 09 15:16:10 WIB 2026
Insert runnable 6 kedalam thread pool - Mon Feb 09 15:16:10 WIB 2026
Insert runnable 7 kedalam thread pool - Mon Feb 09 15:16:10 WIB 2026
Insert runnable 8 kedalam thread pool - Mon Feb 09 15:16:10 WIB 2026
Insert runnable 9 kedalam thread pool - Mon Feb 09 15:16:10 WIB 2026
Insert runnable 10 kedalam thread pool - Mon Feb 09 15:16:10 WIB 2026

WAITING

Done 1 - Mon Feb 09 15:16:11 WIB 2026 in pool-1-thread-1
Done 2 - Mon Feb 09 15:16:11 WIB 2026 in pool-1-thread-2
Done 3 - Mon Feb 09 15:16:11 WIB 2026 in pool-1-thread-3
Done 4 - Mon Feb 09 15:16:12 WIB 2026 in pool-1-thread-1
Done 5 - Mon Feb 09 15:16:12 WIB 2026 in pool-1-thread-2
Done 6 - Mon Feb 09 15:16:12 WIB 2026 in pool-1-thread-3
Done 7 - Mon Feb 09 15:16:13 WIB 2026 in pool-1-thread-1
Done 8 - Mon Feb 09 15:16:13 WIB 2026 in pool-1-thread-2
Done 9 - Mon Feb 09 15:16:13 WIB 2026 in pool-1-thread-3
Done 10 - Mon Feb 09 15:16:14 WIB 2026 in pool-1-thread-1

FINISH MAIN THREAD
```

Fixed threadpool akan diisi dengan 3 thread, dan akan dijalankan bergantian setiap 3 thread selesai
karena memiliki 3 thread

**Cached Thread Executor** (Sesuai Kebutuhan)

```kotlin
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
```

Output:

```
START MAIN THREAD | Mon Feb 09 15:23:47 WIB 2026

Insert runnable 1 kedalam thread pool - Mon Feb 09 15:23:47 WIB 2026
Insert runnable 2 kedalam thread pool - Mon Feb 09 15:23:47 WIB 2026
Insert runnable 3 kedalam thread pool - Mon Feb 09 15:23:47 WIB 2026
Insert runnable 4 kedalam thread pool - Mon Feb 09 15:23:47 WIB 2026
Insert runnable 5 kedalam thread pool - Mon Feb 09 15:23:47 WIB 2026
Insert runnable 6 kedalam thread pool - Mon Feb 09 15:23:47 WIB 2026
Insert runnable 7 kedalam thread pool - Mon Feb 09 15:23:47 WIB 2026
Insert runnable 8 kedalam thread pool - Mon Feb 09 15:23:47 WIB 2026
Insert runnable 9 kedalam thread pool - Mon Feb 09 15:23:47 WIB 2026
Insert runnable 10 kedalam thread pool - Mon Feb 09 15:23:47 WIB 2026

WAITING

Done 1 - Mon Feb 09 15:23:48 WIB 2026 in pool-1-thread-1
Done 2 - Mon Feb 09 15:23:48 WIB 2026 in pool-1-thread-2
Done 3 - Mon Feb 09 15:23:48 WIB 2026 in pool-1-thread-3
Done 4 - Mon Feb 09 15:23:48 WIB 2026 in pool-1-thread-4
Done 5 - Mon Feb 09 15:23:48 WIB 2026 in pool-1-thread-5
Done 6 - Mon Feb 09 15:23:48 WIB 2026 in pool-1-thread-6
Done 7 - Mon Feb 09 15:23:48 WIB 2026 in pool-1-thread-7
Done 8 - Mon Feb 09 15:23:48 WIB 2026 in pool-1-thread-8
Done 9 - Mon Feb 09 15:23:48 WIB 2026 in pool-1-thread-9
Done 10 - Mon Feb 09 15:23:48 WIB 2026 in pool-1-thread-10

FINISH MAIN THREAD | Mon Feb 09 15:23:58 WIB 2026
```

Thread masih memiliki banyak antrian kosong, dan secara otomatis menjalankan seluruh secara
bersamaan karena dapat diatasi dalam 1 waktu

### Callable

Thread akan mengeksekusi isi dari method `run()` yang ada didalam interface `Runnable`, masalahnya
adalah return value dari Runnable adalah Void(Unit) yang tidak mengembalikan data

Untuk mengeksekusi kode yang mengembalikan data, bisa menggunakan interface **Callable** dimana
terdapat method `call()` dengan Generic return value

Gunakan `ExecutorService.submit(callable)` untuk mengeksekusi Callable dan hasilnya adalah
`Future<T>`

### Future

Return value untuk mengeksekusi Callable.

Future digunakan untuk mendapatkan informasi seperti kapan mulai dan selesai, atau bisa mendapatkan
data return dari callable. Future juga bisa digunakan untuk membatalkan Callable

**Get Foo dan Bar Non Parallel**

```kotlin
fun getFoo(): String {
    Thread.sleep(1000)
    return "Foo"
}

fun getBar(): String {
    Thread.sleep(1000)
    return "Bar"
}

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
```

Output:

```
Result = FooBar
Total time : 2002
```

Tanpa paralel, total time akan menjadi 2 detik, 1 detik untuk `getFoo()` dan 1 untuk `getBar()`

**Get Foo dan Bar dengan Callable dan Future**

```kotlin
fun getFoo(): String {
    Thread.sleep(1000)
    return "Foo"
}

fun getBar(): String {
    Thread.sleep(1000)
    return "Bar"
}

@Test
fun getFooBarFuture() {
    val executorService = Executors.newFixedThreadPool(10)
    val time = measureTimeMillis {
        val foo: Future<String> = executorService.submit(Callable { getFoo() })
        val bar: Future<String> = executorService.submit(Callable { getBar() })
        val result = foo.get() + bar.get()
        println("Result = $result")
    }
    println("Total time : $time")
}
```

Output:

```
Result = FooBar
Total time : 1003
```

Total waktu yang didapat secara parallel adalah 1 detik yang masing masing akan dijalankan pada
thread yang berbeda menggunakan `executorService`

**Note**

- Gunakan method `.cancel(true)` untuk menghentikan proses dalam kasus seperti validasi atau lainya

# Project Files

### [Parallel Programming](../../../test/kotlin/ParallelProgramming.kt)
