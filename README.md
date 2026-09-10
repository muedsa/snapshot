![logo](logo.png)

抄[Flutter](https://github.com/flutter/flutter)实现了一个可以直观~~简单快捷~~地构建图片的工具

之前像 [taffy-pvp-card-sw](https://github.com/muedsa/taffy-pvp-card-sw) 这样直接使用Canvas的方式，会让后期维护和修改变的十分困难,
所以急需一个可以结构化构建图像的工具

**Demo**: https://snapshot.muedsa.com

> 📖 **文档导航**
> - [使用说明](docs/usage/README.md)：环境与构建、快速上手、Widget 参考、布局/绘制/文本、类 DOM 解析器全部标签与属性、错误处理、FAQ
> - [测试手册](docs/testing/README.md)：testkit、四层断言、golden 三态

<details>
 <summary><code>POST</code> <code>/snapshot</code> 生成图片</summary>

##### Parameters

dom like text as row request body, example:
```html
<Snapshot background="#FFFFFFFF" type="png">
    <Column>
        <Row>
            <Container color="#FF0000" width="200" height="200" />
            <Container color="#FFFFFF" width="200" height="200">
                <Text color="#0000FF" fontSize="20">哈哈 233<![CDATA[ken_test <a></a> 233 哈哈]]>哈🤣🤣🤣
                </Text>
            </Container>
        </Row>
        <Row>
            <Image width="200" height="200" url="https://samples-files.com/samples/images/png/480-360-sample.png"
                fit="COVER" />
            <Container color="#FFFF00" width="200" height="200" />
        </Row>
    </Column>
</Snapshot>
```

##### Responses

Image ByteArray data, reponse header `Content-Type`: `image/png`, `image/jpeg`, `image/webp`

Example:

![response](https://github.com/muedsa/snapshot/raw/main/sample_parse_dom_like.png)

</details>


### Sample: Container
```kotlin
File("sample_container.png").writeBytes(
    SnapshotPNG {
        Container(
            width = 200f,
            height = 200f,
            color = Color.RED
        )
    }
)
```

![Sample: Container](sample_container.png)

### Sample: Layout

```kotlin
File("sample_layout.png").writeBytes(
    SnapshotPNG {
        Column {
            Row {
                Container(
                    width = 200f,
                    height = 200f,
                    color = Color.RED
                )
                Container(
                    width = 200f,
                    height = 200f,
                    color = Color.GREEN
                )
            }
            Row {
                Container(
                    width = 200f,
                    height = 200f,
                    color = Color.BLUE
                )
                Container(
                    width = 200f,
                    height = 200f,
                    color = Color.YELLOW
                )
            }
        }
    }
)
```

![Sample: Layout](sample_layout.png)

### Sample: Image & Text

```kotlin
File("sample_image_and_text.png").writeBytes(
    SnapshotPNG {
        Stack {
            CachedNetworkImage(
                url = "https://samples-files.com/samples/images/jpg/1280-720-sample.jpg",
                width = 400f,
                height = 400f,
            )
            RichText {
                TextSpan(
                    text = "Hello",
                    style = TextStyle(
                        color = Color.RED,
                        fontSize = 40f
                    )
                )
                TextSpan(
                    text = " World",
                    style = TextStyle(
                        color = Color.GREEN,
                        fontSize = 30f
                    )
                )
            }
        }
    }
)
```

![Sample: Image & Text](sample_image_and_text.png)

### Sample: Parse DOM-LIKE TEXT

```kotlin
val text = """
<Snapshot background="#FFFFFFFF" type="png">
    <Column>
        <Row>
            <Container color="#FF0000" width="200" height="200"/>
            <Container color="#FFFFFF" width="200" height="200">
                <Text color="#0000FF" fontSize="20">哈哈 233<![CDATA[ken_test <a></a> 233 哈哈]]>哈🤣🤣🤣</Text>
            </Container>
        </Row>
        <Row>
            <Image width="200" height="200" url="https://samples-files.com/samples/images/jpg/480-360-sample.jpg"/>
            <Container color="#FFFF00" width="200" height="200"/>
        </Row>
    </Column>
</Snapshot>
""".trimIndent()
File("sample_parse_dom_like.png").writeBytes(Parser().parse(StringReader(text)).snapshot())
```

![Sample: Parse DOM-LIKE TEXT](sample_parse_dom_like.png)

> 上述样例图由被 `@Tag("sample")` 标注的样例/配图再生成测试(Sample、LogoCreator、ParserSample)生成,已从默认测试排除(避免默认 `./gradlew test` 改写仓库根文件;样例图的外网下载只在显式再生成时发生)。手动再生成:`./gradlew :core:test -PincludeSamples` 与 `./gradlew :parser:test -PincludeSamples`。注:`-PincludeSamples` 只运行这些被标注的类,不执行完整测试套件。
>
> 依赖外网的测试(网络图片缓存、`CachedNetworkImage`、含 `ImageEmojiSpan`/`<Emoji>` 的用例)统一标注 `@Tag("network")`,同样从默认 `./gradlew test` 排除;手动运行:`./gradlew :core:test -PincludeNetwork` 与 `./gradlew :parser:test -PincludeNetwork`(语义与 `-PincludeSamples` 相同:只跑该标签)。
