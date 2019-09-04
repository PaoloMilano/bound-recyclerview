package com.magicbluepenguin.boundrecyclerview

interface DifferentiableObject {
    fun hasSameId(other: DifferentiableObject): Boolean
    fun hasSameContents(other: DifferentiableObject): Boolean
}

class DummyDifferentiableObject: DifferentiableObject{
    override fun hasSameContents(other: DifferentiableObject) = false
    override fun hasSameId(other: DifferentiableObject) = false
}