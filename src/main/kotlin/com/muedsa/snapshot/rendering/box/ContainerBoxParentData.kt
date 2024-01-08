package com.muedsa.snapshot.rendering.box

abstract class ContainerBoxParentData : BoxParentData() {

    var previousSibling: RenderBox? = null

    var nextSibling: RenderBox? = null
}