package com.zhufucdev.mcre.utility

import java.math.BigInteger

enum class DataUnit {
    B, KB, MB, GB;
    lateinit var byte: BigInteger
    lateinit var formatedValue: BigInteger
    companion object {
        fun from(byte: BigInteger) = format(byte).let {
            it.first.also { r ->
                r.byte = byte
                r.formatedValue = it.second
            }
        }
        fun format(x: BigInteger): Pair<DataUnit, BigInteger>{
            return when {
                x.compareTo(1024.toBigInteger()) == -1 -> {
                    B to x
                }
                x.compareTo(BigInteger.valueOf(1024).pow(2)) == -1 -> {
                    KB to x.divide(1024.toBigInteger())
                }
                x.compareTo(BigInteger.valueOf(1024).pow(3)) == -1 -> {
                    MB to x.divide((1024).toBigInteger().pow(2))
                }
                x.compareTo(BigInteger.valueOf(1024).pow(4)) == -1 -> {
                    GB to x.divide((1024).toBigInteger().pow(3))
                }
                else -> B to x
            }
        }
    }
}