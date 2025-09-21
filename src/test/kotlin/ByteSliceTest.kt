/*
 * Copyright (c) 2025 Hyoungho Choi <holuyaa@gmail.com>
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ByteSliceTest {
    
    @Test
    fun `should create ByteSlice with valid parameters`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5)
        val slice = ByteSlice(buffer, 1, 3)
        
        assertEquals(3, slice.length)
        assertEquals(2, slice[0])
        assertEquals(3, slice[1])
        assertEquals(4, slice[2])
    }
    
    @Test
    fun `should throw exception for invalid parameters`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5)
        
        // Negative offset
        assertThrows<IllegalArgumentException> {
            ByteSlice(buffer, -1, 3)
        }
        
        // Negative length
        assertThrows<IllegalArgumentException> {
            ByteSlice(buffer, 0, -1)
        }
        
        // Bounds exceed buffer size
        assertThrows<IllegalArgumentException> {
            ByteSlice(buffer, 3, 5)
        }
    }
    
    @Test
    fun `should access bytes using index operator`() {
        val buffer = byteArrayOf(10, 20, 30, 40, 50)
        val slice = ByteSlice(buffer, 1, 3)
        
        assertEquals(20, slice[0])
        assertEquals(30, slice[1])
        assertEquals(40, slice[2])
    }
    
    @Test
    fun `should throw exception for out of bounds access`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5)
        val slice = ByteSlice(buffer, 1, 3)
        
        assertThrows<IllegalArgumentException> {
            slice[-1]
        }
        
        assertThrows<IllegalArgumentException> {
            slice[3]
        }
    }
    
    @Test
    fun `should create sub-slice using range operator`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val slice = ByteSlice(buffer, 1, 6) // [2, 3, 4, 5, 6, 7]
        
        val subSlice = slice[1..3] // Should get [3, 4, 5]
        
        assertEquals(3, subSlice.length)
        assertEquals(3, subSlice[0])
        assertEquals(4, subSlice[1])
        assertEquals(5, subSlice[2])
    }
    
    @Test
    fun `should create sub-slice using slice method`() {
        val buffer = byteArrayOf(10, 20, 30, 40, 50, 60)
        val slice = ByteSlice(buffer, 1, 4) // [20, 30, 40, 50]
        
        val subSlice = slice.slice(1, 2) // Should get [30, 40]
        
        assertEquals(2, subSlice.length)
        assertEquals(30, subSlice[0])
        assertEquals(40, subSlice[1])
    }
    
    @Test
    fun `should convert to ByteArray`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5)
        val slice = ByteSlice(buffer, 1, 3)
        
        val result = slice.toByteArray()
        
        assertThat(result).containsExactly(2, 3, 4)
        
        // Verify it's a copy, not the same reference
        result[0] = 99
        assertEquals(2, slice[0]) // Original slice should be unchanged
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
        
        assertEquals(0, slice.length)
        assertEquals(ByteArray(0).size, slice.toByteArray().size)
    }
    
    @Test
    fun `should check equality correctly`() {
        val buffer1 = byteArrayOf(1, 2, 3, 4, 5)
        val buffer2 = byteArrayOf(0, 2, 3, 4, 0)
        
        val slice1 = ByteSlice(buffer1, 1, 3) // [2, 3, 4]
        val slice2 = ByteSlice(buffer2, 1, 3) // [2, 3, 4]
        val slice3 = ByteSlice(buffer1, 0, 3) // [1, 2, 3]
        
        assertTrue(slice1 == slice2)
        assertFalse(slice1 == slice3)
        assertEquals(slice1.hashCode(), slice2.hashCode())
    }
    
    @Test
    fun `should handle single byte slice`() {
        val buffer = byteArrayOf(10, 20, 30)
        val slice = ByteSlice(buffer, 1, 1)
        
        assertEquals(1, slice.length)
        assertEquals(20, slice[0])
        
        val subSlice = slice[0..0]
        assertEquals(1, subSlice.length)
        assertEquals(20, subSlice[0])
    }
    
    @Test
    fun `should validate range bounds`() {
        val buffer = byteArrayOf(1, 2, 3, 4, 5)
        val slice = ByteSlice(buffer, 1, 3) // [2, 3, 4]
        
        // Invalid ranges
        assertThrows<IllegalArgumentException> {
            slice[-1..1]
        }
        
        assertThrows<IllegalArgumentException> {
            slice[0..3] // end >= length
        }
        
        assertThrows<IllegalArgumentException> {
            slice[2..1] // start > end
        }
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
