package com.williambl.raycastengine.collision

import com.williambl.raycastengine.util.math.Vec2d

class AABBQuadTree(minX: Double, minY: Double, maxX: Double, maxY: Double) : AxisAlignedBoundingBox(minX, minY, maxX, maxY, null), MutableSet<AxisAlignedBoundingBox> {

    companion object {
        private const val maxColliders = 8
    }

    constructor() : this(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)

    private var childTrees = arrayOf<AABBQuadTree>()
    private val colliders = mutableSetOf<AxisAlignedBoundingBox>()

    private val isGlobal = minX == Double.NEGATIVE_INFINITY || minY == Double.NEGATIVE_INFINITY || maxX == Double.POSITIVE_INFINITY || maxY == Double.POSITIVE_INFINITY

    override val size: Int
        get() = colliders.size + childTrees.map { it.size }.sum()

    override fun add(element: AxisAlignedBoundingBox): Boolean {
        if (childTrees.isEmpty()) {
            return colliders.add(element)
        } else for (child in childTrees) {
            if (element.fitsWithin(child)) {
                return child.add(element)
            }
        }
        val result = colliders.add(element)
        reallocateColliders()
        return result
    }

    override fun remove(element: AxisAlignedBoundingBox): Boolean {
        if (!colliders.remove(element)) {
            for (child in childTrees)
                if (child.remove(element))
                    return true
            return false
        }
        return true
    }

    override fun contains(element: AxisAlignedBoundingBox): Boolean {
        return colliders.contains(element) || childTrees.any { it.contains(element) }
    }

    override fun containsAll(elements: Collection<AxisAlignedBoundingBox>): Boolean {
        return allElements().containsAll(elements)
    }

    override fun isEmpty(): Boolean {
        return allElements().isEmpty()
    }

    override fun iterator(): MutableIterator<AxisAlignedBoundingBox> {
        return QuadTreeIterator(this)
    }

    override fun addAll(elements: Collection<AxisAlignedBoundingBox>): Boolean {
        return elements.any { this.add(it) }
    }

    override fun clear() {
        colliders.clear()
        childTrees = arrayOf()
    }

    override fun removeAll(elements: Collection<AxisAlignedBoundingBox>): Boolean {
        val childrenRemove = childTrees.any { it.removeAll(elements) }
        return colliders.removeAll(elements) || childrenRemove
    }

    override fun retainAll(elements: Collection<AxisAlignedBoundingBox>): Boolean {
        val childrenRetain = childTrees.any { it.retainAll(elements) }
        return colliders.retainAll(elements) || childrenRetain
    }

    fun allElements(): Set<AxisAlignedBoundingBox> {
        val elements = colliders.toMutableSet()
        for (child in childTrees) {
            elements.addAll(child.allElements())
        }
        return elements
    }

    operator fun get(index: Vec2d): List<AxisAlignedBoundingBox> {
        val quadrant = childTrees.first { it.collidesWith(index) }[index].toMutableList()
        quadrant.addAll(colliders.filter { it.collidesWith(index) })
        return quadrant.toList()
    }

    operator fun get(x: Double, y: Double): List<AxisAlignedBoundingBox> {
        val quadrant = childTrees.first { it.collidesWith(x, y) }[x, y].toMutableList()
        quadrant.addAll(colliders.filter { it.collidesWith(x, y) })
        return quadrant.toList()
    }

    private fun reallocateColliders() {
        if (colliders.size > maxColliders) {
            if (childTrees.isEmpty()) {
                childTrees = arrayOf(
                        AABBQuadTree(minX, minY, getCentreX(), getCentreY()),
                        AABBQuadTree(getCentreX(), minY, maxX, getCentreY()),
                        AABBQuadTree(minX, getCentreY(), getCentreX(), maxY),
                        AABBQuadTree(getCentreX(), getCentreY(), maxX, maxY)
                )
            }

            colliders.mapNotNullTo(colliders) {
                for (child in childTrees) {
                    if (it.fitsWithin(child)) {
                        child.colliders.add(child)
                        return@mapNotNullTo null
                    }
                }
                it
            }
        }
    }

    private fun getCentreX(): Double {
        return if (isGlobal) 0.0 else minX / 2.0 + maxX / 2.0
    }

    private fun getCentreY(): Double {
        return if (isGlobal) 0.0 else minY / 2.0 + maxY / 2.0
    }

    private inner class QuadTreeIterator(tree: AABBQuadTree) : MutableIterator<AxisAlignedBoundingBox> {
        private var index = 0
        private val iterators = getIterators(tree)

        private fun getIterators(tree: AABBQuadTree): List<MutableIterator<AxisAlignedBoundingBox>> {
            val list = mutableListOf(tree.colliders.iterator())
            for (child in childTrees) {
                list.addAll(getIterators(child))
            }
            return list.toList()
        }

        override fun hasNext(): Boolean {
            if (index == iterators.size - 1)
                return iterators[index].hasNext()
            return true
        }

        override fun next(): AxisAlignedBoundingBox {
            if (iterators[index].hasNext())
                return iterators[index].next()
            index++
            return iterators[index].next()
        }

        override fun remove() {
            iterators[index].remove()
        }
    }

}