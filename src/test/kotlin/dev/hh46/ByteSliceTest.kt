package dev.hh46/*
 * Copyright (c) 2025 Hyoungho Choi <holuyaa@gmail.com>
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class ByteSliceTest {

    @Test
    fun `should create ByteSlice with valid parameters`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5)
        val slice = ByteSlice(buffer, 1, 3)

        assertThat(slice.length).isEqualTo(3)
        assertThat(slice[0]).isEqualTo(2.toByte())
        assertThat(slice[1]).isEqualTo(3.toByte())
        assertThat(slice[2]).isEqualTo(4.toByte())
    }

    @Test
    fun `should throw exception for invalid parameters`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5)

        // Negative offset
        assertThatThrownBy {
            ByteSlice(buffer, -1, 3)
        }.isInstanceOf(IllegalArgumentException::class.java)

        // Negative length
        assertThatThrownBy {
            ByteSlice(buffer, 0, -1)
        }.isInstanceOf(IllegalArgumentException::class.java)

        // Bounds exceed buffer size
        assertThatThrownBy {
            ByteSlice(buffer, 3, 5)
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `should access bytes using index operator`() {
        val buffer = byteArrayOf(10, 20, 30, 40, 50)
        val slice = ByteSlice(buffer, 1, 3)

        assertThat(slice[0]).isEqualTo(20.toByte())
        assertThat(slice[1]).isEqualTo(30.toByte())
        assertThat(slice[2]).isEqualTo(40.toByte())
    }

    @Test
    fun `should throw exception for out of bounds access`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5)
        val slice = ByteSlice(buffer, 1, 3)

        assertThatThrownBy {
            slice[-1]
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThatThrownBy {
            slice[3]
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `should create sub-slice using range operator`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val slice = ByteSlice(buffer, 1, 6) // [2, 3, 4, 5, 6, 7]

        val subSlice = slice[1..3] // Should get [3, 4, 5]

        assertThat(subSlice.length).isEqualTo(3)
        assertThat(subSlice[0]).isEqualTo(3.toByte())
        assertThat(subSlice[1]).isEqualTo(4.toByte())
        assertThat(subSlice[2]).isEqualTo(5.toByte())
    }

    @Test
    fun `should create sub-slice using slice method`() {
        val buffer = byteArrayOf(10, 20, 30, 40, 50, 60)
        val slice = ByteSlice(buffer, 1, 4) // [20, 30, 40, 50]

        val subSlice = slice.slice(1, 2) // Should get [30, 40]

        assertThat(subSlice.length).isEqualTo(2)
        assertThat(subSlice[0]).isEqualTo(30.toByte())
        assertThat(subSlice[1]).isEqualTo(40.toByte())
    }

    @Test
    fun `should convert to ByteArray`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5)
        val slice = ByteSlice(buffer, 1, 3)

        val result = slice.toByteArray()

        assertThat(result).containsExactly(2, 3, 4)

        // Verify it's a copy, not the same reference
        result[0] = 99
        assertThat(slice[0]).isEqualTo(2.toByte()) // Original slice should be unchanged
    }

    @Test
    fun `should convert range to ByteArray`() {
        val buffer = byteArrayOf(10, 20, 30, 40, 50, 60)
        val slice = ByteSlice(buffer, 1, 4) // [20, 30, 40, 50]

        val result = slice.toByteArray(1..2) // Should get [30, 40]

        assertThat(result).containsExactly(30, 40)
    }

    @Test
    fun `should convert offset and length to ByteArray`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5, 6)
        val slice = ByteSlice(buffer, 1, 4) // [2, 3, 4, 5]

        val result = slice.toByteArray(1, 2) // Should get [3, 4]

        assertThat(result).containsExactly(3, 4)
    }

    @Test
    fun `should copy data to destination array`() {
        val buffer = byteArrayOf(10, 20, 30, 40, 50)
        val slice = ByteSlice(buffer, 1, 3) // [20, 30, 40]
        val dest = ByteArray(5)

        slice.copyTo(dest, destOffset = 1, copyLength = 2)

        assertThat(dest).containsExactly(0, 20, 30, 0, 0)
    }

    @Test
    fun `should handle empty slice`() {
        val buffer = byteArrayOf(1, 2, 3)
        val slice = ByteSlice(buffer, 1, 0)

        assertThat(slice.length).isZero()
        assertThat(slice.toByteArray()).isEmpty()
    }

    @Test
    fun `should check equality correctly`() {
        val buffer1 = byteArrayOf(1, 2, 3, 4, 5)
        val buffer2 = byteArrayOf(0, 2, 3, 4, 0)

        val slice1 = ByteSlice(buffer1, 1, 3) // [2, 3, 4]
        val slice2 = ByteSlice(buffer2, 1, 3) // [2, 3, 4]
        val slice3 = ByteSlice(buffer1, 0, 3) // [1, 2, 3]

        assertThat(slice1).isEqualTo(slice2)
        assertThat(slice1).isNotEqualTo(slice3)
        assertThat(slice1.hashCode()).isEqualTo(slice2.hashCode())
    }

    @Test
    fun `should handle single byte slice`() {
        val buffer = byteArrayOf(10, 20, 30)
        val slice = ByteSlice(buffer, 1, 1)

        assertThat(slice.length).isEqualTo(1)
        assertThat(slice[0]).isEqualTo(20.toByte())

        val subSlice = slice[0..0]
        assertThat(subSlice.length).isEqualTo(1)
        assertThat(subSlice[0]).isEqualTo(20.toByte())
    }

    @Test
    fun `should validate range bounds`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5)
        val slice = ByteSlice(buffer, 1, 3) // [2, 3, 4]

        // Invalid ranges
        assertThatThrownBy {
            slice[-1..1]
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThatThrownBy {
            slice[0..3] // end >= length
        }.isInstanceOf(IllegalArgumentException::class.java)

        assertThatThrownBy {
            slice[2..1] // start > end
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `should have meaningful toString`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5)
        val slice = ByteSlice(buffer, 2, 2)

        val result = slice.toString()

        assertThat(result).contains("ByteSlice")
        assertThat(result).contains("offset=2")
        assertThat(result).contains("length=2")
    }
}