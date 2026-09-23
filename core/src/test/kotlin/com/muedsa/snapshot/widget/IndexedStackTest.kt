package com.muedsa.snapshot.widget

import com.muedsa.snapshot.assertGlobalRect
import com.muedsa.snapshot.assertSize
import com.muedsa.snapshot.expectColorAt
import com.muedsa.snapshot.rootLayout
import com.muedsa.snapshot.snapshotPixels
import org.jetbrains.skia.Color
import kotlin.test.Test
import kotlin.test.assertFailsWith

class IndexedStackTest {

    @Test
    fun all_children_determine_size_but_only_selected_child_is_painted() {
        val layout = rootLayout {
            IndexedStack(index = 1) {
                Container(width = 80f, height = 60f, color = Color.RED)
                Container(width = 30f, height = 20f, color = Color.BLUE)
            }
        }
        layout.assertSize(80f, 60f)
        layout.children[0].assertGlobalRect(0f, 0f, 80f, 60f)
        layout.children[1].assertGlobalRect(0f, 0f, 30f, 20f)

        val pixels = snapshotPixels {
            IndexedStack(index = 1) {
                Container(width = 80f, height = 60f, color = Color.RED)
                Container(width = 30f, height = 20f, color = Color.BLUE)
            }
        }
        expectColorAt(pixels, 10, 10, Color.BLUE)
        expectColorAt(pixels, 50, 40, Color.WHITE)
    }

    @Test
    fun null_index_keeps_size_without_painting_children() {
        val pixels = snapshotPixels {
            IndexedStack(index = null) {
                Container(width = 40f, height = 30f, color = Color.RED)
            }
        }
        expectColorAt(pixels, 20, 15, Color.WHITE)
    }

    @Test
    fun positioned_child_uses_stack_parent_data() {
        val layout = rootLayout {
            IndexedStack(index = 1) {
                Container(width = 80f, height = 60f, color = Color.RED)
                Positioned(left = 12f, top = 8f) {
                    Container(width = 20f, height = 15f, color = Color.BLUE)
                }
            }
        }
        layout.assertSize(80f, 60f)
        layout.children[1].assertGlobalRect(12f, 8f, 20f, 15f)

        val pixels = snapshotPixels {
            IndexedStack(index = 1) {
                Container(width = 80f, height = 60f, color = Color.RED)
                Positioned(left = 12f, top = 8f) {
                    Container(width = 20f, height = 15f, color = Color.BLUE)
                }
            }
        }
        expectColorAt(pixels, 15, 10, Color.BLUE)
        expectColorAt(pixels, 50, 40, Color.WHITE)
    }

    @Test
    fun invalid_indices_fail_before_painting() {
        assertFailsWith<IllegalArgumentException> {
            rootLayout {
                IndexedStack(index = -1) {
                    Container(width = 10f, height = 10f)
                }
            }
        }
        assertFailsWith<IllegalArgumentException> {
            rootLayout {
                IndexedStack(index = 2) {
                    Container(width = 10f, height = 10f)
                }
            }
        }
    }
}
