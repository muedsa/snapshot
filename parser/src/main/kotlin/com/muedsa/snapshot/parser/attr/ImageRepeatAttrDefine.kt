package com.muedsa.snapshot.parser.attr

import com.muedsa.snapshot.paint.ImageRepeat

class ImageRepeatAttrDefine(name: String, defaultValue: ImageRepeat = ImageRepeat.NO_REPEAT) :
    DefaultValueAttrDefine<ImageRepeat>(name = name, defaultValue = defaultValue) {

    override fun parseValue(valueStr: String?): ImageRepeat {
        requireNotNull(valueStr)
        return ImageRepeat.valueOf(valueStr)
    }

    override fun copyWith(name: String, defaultValue: ImageRepeat): DefaultValueAttrDefine<ImageRepeat> =
        ImageRepeatAttrDefine(name, defaultValue)
}