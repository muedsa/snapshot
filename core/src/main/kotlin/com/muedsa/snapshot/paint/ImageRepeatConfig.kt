package com.muedsa.snapshot.paint

/** 图片重复绘制的全局配置。解析文本不能覆盖此配置。 */
object ImageRepeatConfig {

    const val DEFAULT_MAX_TILE_COUNT: Int = 100_000

    /** 单次图片重复绘制允许生成的最大平铺矩形数，必须大于零。 */
    @Volatile
    var maxTileCount: Int = DEFAULT_MAX_TILE_COUNT
        set(value) {
            require(value > 0) { "Image repeat maxTileCount must be greater than zero." }
            field = value
        }
}
