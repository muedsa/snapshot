package com.muedsa.snapshot.rendering.flex

import com.muedsa.snapshot.rendering.box.ContainerBoxParentData

class FlexParentData : ContainerBoxParentData() {
    var flex: Int? = null
    var fit: FlexFit? = null
}