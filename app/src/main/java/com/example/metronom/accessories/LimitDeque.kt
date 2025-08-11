package com.example.metronom.accessories

import java.util.ArrayDeque

class LimitDeque<Long>(private val limitSize: Int) : ArrayDeque<Long>() {
    override fun push(p0: Long) {
        if (this.size >= limitSize) pollLast()
        super.push(p0)
    }
}