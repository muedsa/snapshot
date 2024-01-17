package com.muedsa.snapshot.material

import com.muedsa.geometry.Offset
import com.muedsa.snapshot.paint.decoration.BoxShadow

private const val KeyUmbraOpacity: Int = 0x33000000 // alpha = 0.2
private const val KeyPenumbraOpacity: Int = 0x24000000 // alpha = 0.14
private const val AmbientShadowOpacity: Int = 0x1F000000 // alpha = 0.12

val ELEVATION_MAP: Map<Int, Array<BoxShadow>> = mapOf(
    0 to arrayOf(),
    1 to arrayOf(
        BoxShadow(offset = Offset(0f, 2f), blurRadius = 1f, spreadRadius = -1f, color = KeyUmbraOpacity),
        BoxShadow(offset = Offset(0f, 1f), blurRadius = 1f, color = KeyPenumbraOpacity),
        BoxShadow(offset = Offset(0f, 1f), blurRadius = 3f, color = AmbientShadowOpacity)
    ),
    2 to arrayOf(
        BoxShadow(offset = Offset(0f, 3f), blurRadius = 1f, spreadRadius = -2f, color = KeyUmbraOpacity),
        BoxShadow(offset = Offset(0f, 2f), blurRadius = 2f, color = KeyPenumbraOpacity),
        BoxShadow(offset = Offset(0f, 1f), blurRadius = 5f, color = AmbientShadowOpacity)
    ),
    3 to arrayOf(
        BoxShadow(offset = Offset(0f, 3f), blurRadius = 3f, spreadRadius = -2f, color = KeyUmbraOpacity),
        BoxShadow(offset = Offset(0f, 3f), blurRadius = 4f, color = KeyPenumbraOpacity),
        BoxShadow(offset = Offset(0f, 1f), blurRadius = 8f, color = AmbientShadowOpacity)
    ),
    4 to arrayOf(
        BoxShadow(offset = Offset(0f, 2f), blurRadius = 4f, spreadRadius = -1f, color = KeyUmbraOpacity),
        BoxShadow(offset = Offset(0f, 4f), blurRadius = 5f, color = KeyPenumbraOpacity),
        BoxShadow(offset = Offset(0f, 1f), blurRadius = 10f, color = AmbientShadowOpacity)
    ),
    6 to arrayOf(
        BoxShadow(offset = Offset(0f, 3f), blurRadius = 5f, spreadRadius = -1f, color = KeyUmbraOpacity),
        BoxShadow(offset = Offset(0f, 6f), blurRadius = 10f, color = KeyPenumbraOpacity),
        BoxShadow(offset = Offset(0f, 1f), blurRadius = 18f, color = AmbientShadowOpacity)
    ),
    8 to arrayOf(
        BoxShadow(offset = Offset(0f, 5f), blurRadius = 5f, spreadRadius = -3f, color = KeyUmbraOpacity),
        BoxShadow(offset = Offset(0f, 8f), blurRadius = 10f, spreadRadius = 1f, color = KeyPenumbraOpacity),
        BoxShadow(offset = Offset(0f, 3f), blurRadius = 14f, spreadRadius = 2f, color = AmbientShadowOpacity)
    ),
    9 to arrayOf(
        BoxShadow(offset = Offset(0f, 5f), blurRadius = 6f, spreadRadius = -3f, color = KeyUmbraOpacity),
        BoxShadow(offset = Offset(0f, 9f), blurRadius = 12f, spreadRadius = 1f, color = KeyPenumbraOpacity),
        BoxShadow(offset = Offset(0f, 3f), blurRadius = 16f, spreadRadius = 2f, color = AmbientShadowOpacity)
    ),
    12 to arrayOf(
        BoxShadow(offset = Offset(0f, 7f), blurRadius = 8f, spreadRadius = -4f, color = KeyUmbraOpacity),
        BoxShadow(offset = Offset(0f, 12f), blurRadius = 17f, spreadRadius = 2f, color = KeyPenumbraOpacity),
        BoxShadow(offset = Offset(0f, 5f), blurRadius = 22f, spreadRadius = 4f, color = AmbientShadowOpacity)
    ),
    16 to arrayOf(
        BoxShadow(offset = Offset(0f, 8f), blurRadius = 10f, spreadRadius = -5f, color = KeyUmbraOpacity),
        BoxShadow(offset = Offset(0f, 16f), blurRadius = 24f, spreadRadius = 2f, color = KeyPenumbraOpacity),
        BoxShadow(offset = Offset(0f, 6f), blurRadius = 30f, spreadRadius = 5f, color = AmbientShadowOpacity)
    ),
    24 to arrayOf(
        BoxShadow(offset = Offset(0f, 11f), blurRadius = 15f, spreadRadius = -7f, color = KeyUmbraOpacity),
        BoxShadow(offset = Offset(0f, 24f), blurRadius = 38f, spreadRadius = 3f, color = KeyPenumbraOpacity),
        BoxShadow(offset = Offset(0f, 9f), blurRadius = 46f, spreadRadius = 8f, color = AmbientShadowOpacity)
    ),
)
