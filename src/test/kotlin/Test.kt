import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Test {
    @Test
    fun threadTest() {
        val threadName = Thread.currentThread().name
        println("Thread name = $threadName")
    }

    @Test
    fun testSuspendFunction() = runTest {
        val result = count()
        assertEquals(42, result)
    }

    private suspend fun count(): Int {
        delay(1_000)
        return 42
    }
}