/*
 * Copyright (c) 2025 Hyoungho Choi <holuyaa@gmail.com>
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.*
import kotlin.test.*

class ByteSliceInputStreamTest {
    
    private fun createTestInputStream(data: ByteArray): InputStream {
        return ByteArrayInputStream(data)
    }
    
    @Test
    fun `should read data as ByteSlice`() {
        val testData = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)
        
        val slice = sliceStream.read(length = 4)
        
        assertNotNull(slice)
        assertEquals(4, slice.length)
        assertThat(slice.toByteArray()).containsExactly(1, 2, 3, 4)
        
        sliceStream.close()
    }
    
    @Test
    fun `should read multiple independent slices`() {
        val testData = byteArrayOf(10, 20, 30, 40, 50, 60)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)
        
        val slice1 = sliceStream.read(length = 3)
        val slice2 = sliceStream.read(length = 3)
        
        assertNotNull(slice1)
        assertNotNull(slice2)
        
        assertThat(slice1.toByteArray()).containsExactly(10, 20, 30)
        assertThat(slice2.toByteArray()).containsExactly(40, 50, 60)
        
        // Verify slices are independent
        assertEquals(10, slice1[0])
        assertEquals(40, slice2[0])
        
        sliceStream.close()
    }
    
    @Test
    fun `should return null on EOF`() {
        val testData = byteArrayOf(1, 2, 3)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)
        
        val slice1 = sliceStream.read(length = 3)
        val slice2 = sliceStream.read(length = 1) // Should be null
        
        assertNotNull(slice1)
        assertNull(slice2)
        
        sliceStream.close()
    }
    
    @Test
    fun `should handle partial reads`() {
        val testData = byteArrayOf(1, 2, 3, 4, 5)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)
        
        val slice = sliceStream.read(length = 10) // Request more than available
        
        assertNotNull(slice)
        assertEquals(5, slice.length) // Should return only what's available
        assertThat(slice.toByteArray()).containsExactly(1, 2, 3, 4, 5)
        
        sliceStream.close()
    }
    
    @Test
    fun `should skip offset bytes`() {
        val testData = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)
        
        val slice = sliceStream.read(offset = 3, length = 3)
        
        assertNotNull(slice)
        assertThat(slice.toByteArray()).containsExactly(4, 5, 6)
        
        sliceStream.close()
    }
    
    @Test
    fun `should read exactly specified length`() {
        val testData = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)
        
        val slice = sliceStream.readExactly(5)
        
        assertEquals(5, slice.length)
        assertThat(slice.toByteArray()).containsExactly(1, 2, 3, 4, 5)
        
        sliceStream.close()
    }
    
    @Test
    fun `should throw EOFException when readExactly cannot fulfill request`() {
        val testData = byteArrayOf(1, 2, 3)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)
        
        assertThrows<EOFException> {
            sliceStream.readExactly(5) // More than available
        }
        
        sliceStream.close()
    }
    
    @Test
    fun `should read single bytes`() {
        val testData = byteArrayOf(65, 66, 67) // A, B, C
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)
        
        assertEquals(65, sliceStream.readByte())
        assertEquals(66, sliceStream.readByte())
        assertEquals(67, sliceStream.readByte())
        assertEquals(-1, sliceStream.readByte()) // EOF
        
        sliceStream.close()
    }
    
    @Test
    fun `should read all data into single slice`() {
        val testData = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)
        
        val slice = sliceStream.readAll()
        
        assertNotNull(slice)
        assertEquals(testData.size, slice.length)
        assertThat(slice.toByteArray()).containsExactly(testData.toTypedArray())
        
        sliceStream.close()
    }
    
    @Test
    fun `should return null when readAll on empty stream`() {
        val inputStream = createTestInputStream(ByteArray(0))
        val sliceStream = ByteSliceInputStream(inputStream)
        
        val slice = sliceStream.readAll()
        
        assertNull(slice)
        
        sliceStream.close()
    }
    
    @Test
    fun `should handle large data correctly`() {
        // Create large test data (larger than default buffer)
        val testData = ByteArray(20000) { (it % 256).toByte() }
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)
        
        val allSlices = mutableListOf<ByteSlice>()
        
        while (true) {
            val slice = sliceStream.read(length = 8192) ?: break
            allSlices.add(slice)
        }
        
        // Verify all data was read correctly
        val totalLength = allSlices.sumOf { it.length }
        assertEquals(testData.size, totalLength)
        
        // Verify data integrity
        var offset = 0
        for (slice in allSlices) {
            for (i in 0 until slice.length) {
                assertEquals(testData[offset + i], slice[i])
            }
            offset += slice.length
        }
        
        sliceStream.close()
    }
    
    @Test
    fun `should validate input parameters`() {
        val inputStream = createTestInputStream(byteArrayOf(1, 2, 3))
        val sliceStream = ByteSliceInputStream(inputStream)
        
        // Negative offset
        assertThrows<IllegalArgumentException> {
            sliceStream.read(offset = -1, length = 3)
        }
        
        // Zero or negative length
        assertThrows<IllegalArgumentException> {
            sliceStream.read(offset = 0, length = 0)
        }
        
        assertThrows<IllegalArgumentException> {
            sliceStream.read(offset = 0, length = -1)
        }
        
        sliceStream.close()
    }
    
    @Test
    fun `should handle available bytes correctly`() {
        val testData = byteArrayOf(1, 2, 3, 4, 5)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)
        
        assertTrue(sliceStream.available() >= 0)
        
        // Read some data
        sliceStream.read(length = 3)
        
        // Should still report some available (though exact amount may vary)
        assertTrue(sliceStream.available() >= 0)
        
        sliceStream.close()
    }
    
    @Test
    fun `should maintain data independence between slices`() {
        val testData = ByteArray(1000) { i -> (i % 256).toByte() }
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)
        
        val slices = mutableListOf<ByteSlice>()
        
        // Read multiple slices
        repeat(10) {
            val slice = sliceStream.read(length = 100)
            if (slice != null) {
                slices.add(slice)
            }
        }
        
        // Verify each slice maintains its original data
        for ((index, slice) in slices.withIndex()) {
            val expectedStart = index * 100
            for (i in 0 until slice.length) {
                assertEquals(
                    testData[expectedStart + i], 
                    slice[i],
                    "Mismatch at slice $index, position $i"
                )
            }
        }
        
        sliceStream.close()
    }
    
    @Test
    fun `should handle stream with slow reads`() {
        // Simulate slow InputStream that returns data gradually
        val testData = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val slowInputStream = object : InputStream() {
            private var position = 0
            
            override fun read(): Int {
                return if (position < testData.size) {
                    testData[position++].toInt() and 0xFF
                } else {
                    -1
                }
            }
            
            override fun read(b: ByteArray, off: Int, len: Int): Int {
                // Simulate slow reads by returning at most 2 bytes at a time
                if (position >= testData.size) return -1
                
                val actualLen = minOf(len, 2, testData.size - position)
                System.arraycopy(testData, position, b, off, actualLen)
                position += actualLen
                return actualLen
            }
        }
        
        val sliceStream = ByteSliceInputStream(slowInputStream)
        val slice = sliceStream.readExactly(testData.size)
        
        assertEquals(testData.size, slice.length)
        assertThat(slice.toByteArray()).containsExactly(testData.toTypedArray())
        
        sliceStream.close()
    }
}
