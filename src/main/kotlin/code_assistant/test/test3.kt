package code_assistant.test

import java.io.StringReader
import javax.swing.text.MutableAttributeSet
import javax.swing.text.html.HTML
import javax.swing.text.html.HTMLEditorKit
import javax.swing.text.html.HTMLEditorKit.ParserCallback
import javax.swing.text.html.parser.ParserDelegator

class CustomParserCallback(private val callback: HTMLEditorKit.ParserCallback) : ParserCallback() {
    override fun handleStartTag(tag: HTML.Tag, attributes: MutableAttributeSet, pos: Int) {
        if (tag == HTML.Tag.SCRIPT) {
            // 这里处理script标签的内容
            // 例如，你可以从attributes中获取src属性，然后加载和执行脚本
        } else {
            super.handleStartTag(tag, attributes, pos)
        }
    }
}

fun main() {
    // 使用方法
    val htmlString = "<html><body><script>alert('Hello, world!');</script></body></html>"
    val source = StringReader(htmlString)
    val parserDelegator = ParserDelegator()
    val callback = HTMLEditorKit.ParserCallback()
    val customCallback = CustomParserCallback(callback)
    parserDelegator.parse(source, customCallback, true)
}
