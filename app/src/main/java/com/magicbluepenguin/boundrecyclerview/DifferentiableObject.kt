package com.magicbluepenguin.boundrecyclerview

interface DifferentiableObject {
    fun hasSameId(other: DifferentiableObject): Boolean
    fun hasSameContents(other: DifferentiableObject): Boolean
}
