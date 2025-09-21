package dev.hh46/*
 * Copyright (c) 2025 Hyoungho Choi <holuyaa@gmail.com>
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.EOFException
import java.io.InputStream

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

        assertThat(slice).isNotNull
        assertThat(slice!!.length).isEqualTo(4)
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

        assertThat(slice1).isNotNull
        assertThat(slice2).isNotNull

        assertThat(slice1!!.toByteArray()).containsExactly(10, 20, 30)
        assertThat(slice2!!.toByteArray()).containsExactly(40, 50, 60)

        // Verify slices are independent
        assertThat(slice1[0]).isEqualTo(10.toByte())
        assertThat(slice2[0]).isEqualTo(40.toByte())

        sliceStream.close()
    }

    @Test
    fun `should return null on EOF`() {
        val testData = byteArrayOf(1, 2, 3)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)

        val slice1 = sliceStream.read(length = 3)
        val slice2 = sliceStream.read(length = 1) // Should be null

        assertThat(slice1).isNotNull
        assertThat(slice2).isNull()

        sliceStream.close()
    }

    @Test
    fun `should handle partial reads`() {
        val testData = byteArrayOf(1, 2, 3, 4, 5)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)

        val slice = sliceStream.read(length = 10) // Request more than available

        assertThat(slice).isNotNull
        assertThat(slice!!.length).isEqualTo(5) // Should return only what's available
        assertThat(slice.toByteArray()).containsExactly(1, 2, 3, 4, 5)

        sliceStream.close()
    }

    @Test
    fun `should skip offset bytes`() {
        val testData = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)

        val slice = sliceStream.read(offset = 3, length = 3)

        assertThat(slice).isNotNull
        assertThat(slice!!.toByteArray()).containsExactly(4, 5, 6)

        sliceStream.close()
    }

    @Test
    fun `should read exactly specified length`() {
        val testData = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)

        val slice = sliceStream.readExactly(5)

        assertThat(slice.length).isEqualTo(5)
        assertThat(slice.toByteArray()).containsExactly(1, 2, 3, 4, 5)

        sliceStream.close()
    }

    @Test
    fun `should throw EOFException when readExactly cannot fulfill request`() {
        val testData = byteArrayOf(1, 2, 3)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)

        assertThatThrownBy {
            sliceStream.readExactly(5) // More than available
        }.isInstanceOf(EOFException::class.java)

        sliceStream.close()
    }

    @Test
    fun `should read single bytes`() {
        val testData = byteArrayOf(65, 66, 67) // A, B, C
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)

        assertThat(sliceStream.readByte()).isEqualTo(65)
        assertThat(sliceStream.readByte()).isEqualTo(66)
        assertThat(sliceStream.readByte()).isEqualTo(67)
        assertThat(sliceStream.readByte()).isEqualTo(-1) // EOF

        sliceStream.close()
    }

    @Test
    fun `should read all data into single slice`() {
        val testData = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)

        val slice = sliceStream.readAll()

        assertThat(slice).isNotNull
        assertThat(slice!!.length).isEqualTo(testData.size)
        assertThat(slice.toByteArray()).containsExactly(*testData)

        sliceStream.close()
    }

    @Test
    fun `should return null when readAll on empty stream`() {
        val inputStream = createTestInputStream(ByteArray(0))
        val sliceStream = ByteSliceInputStream(inputStream)

        val slice = sliceStream.readAll()

        assertThat(slice).isNull()

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
        assertThat(totalLength).isEqualTo(testData.size)

        // Verify data integrity
        var offset = 0
        for (slice in allSlices) {
            for (i in 0 until slice.length) {
                assertThat(slice[i]).isEqualTo(testData[offset + i])
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
        assertThatThrownBy {
            sliceStream.read(offset = -1, length = 3)
        }.isInstanceOf(IllegalArgumentException::class.java)

        // Zero or negative length
        assertThatThrownBy {
            sliceStream.read(offset = 0, length = 0)
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThatThrownBy {
            sliceStream.read(offset = 0, length = -1)
        }.isInstanceOf(IllegalArgumentException::class.java)

        sliceStream.close()
    }

    @Test
    fun `should handle available bytes correctly`() {
        val testData = byteArrayOf(1, 2, 3, 4, 5)
        val inputStream = createTestInputStream(testData)
        val sliceStream = ByteSliceInputStream(inputStream)

        assertThat(sliceStream.available()).isGreaterThanOrEqualTo(0)

        // Read some data
        sliceStream.read(length = 3)

        // Should still report some available (though exact amount may vary)
        assertThat(sliceStream.available()).isGreaterThanOrEqualTo(0)

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
                assertThat(slice[i])
                    .withFailMessage("Mismatch at slice $index, position $i")
                    .isEqualTo(testData[expectedStart + i])
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

        assertThat(slice.length).isEqualTo(testData.size)
        assertThat(slice.toByteArray()).containsExactly(*testData)

        sliceStream.close()
    }
}