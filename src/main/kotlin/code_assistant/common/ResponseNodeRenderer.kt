package code_assistant.common

import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.vladsch.flexmark.ast.*
import com.vladsch.flexmark.html.HtmlWriter
import com.vladsch.flexmark.html.renderer.NodeRenderer
import com.vladsch.flexmark.html.renderer.NodeRendererContext
import com.vladsch.flexmark.html.renderer.NodeRendererFactory
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler
import com.vladsch.flexmark.util.data.DataHolder


class ResponseNodeRenderer : NodeRenderer {
    override fun getNodeRenderingHandlers(): Set<NodeRenderingHandler<*>> {
        return java.util.Set.of(
            NodeRenderingHandler(
                Paragraph::class.java
            ) { node: Paragraph, context: NodeRendererContext, html: HtmlWriter ->
                renderParagraph(
                    node,
                    context,
                    html
                )
            },
            NodeRenderingHandler(
                Code::class.java
            ) { node: Code, context: NodeRendererContext, html: HtmlWriter ->
                renderCode(
                    node,
                    context,
                    html
                )
            },
            NodeRenderingHandler(
                CodeBlock::class.java
            ) { node: CodeBlock, context: NodeRendererContext, html: HtmlWriter ->
                renderCodeBlock(
                    node,
                    context,
                    html
                )
            },
            NodeRenderingHandler(
                BulletListItem::class.java
            ) { node: BulletListItem, context: NodeRendererContext, html: HtmlWriter ->
                renderBulletListItem(
                    node,
                    context,
                    html
                )
            },
            NodeRenderingHandler(
                Heading::class.java
            ) { node: Heading, context: NodeRendererContext, html: HtmlWriter ->
                renderHeading(
                    node,
                    context,
                    html
                )
            },
            NodeRenderingHandler(
                OrderedListItem::class.java
            ) { node: OrderedListItem, context: NodeRendererContext, html: HtmlWriter ->
                renderOrderedListItem(
                    node,
                    context,
                    html
                )
            }
        )
    }

    private fun renderCodeBlock(node: CodeBlock, context: NodeRendererContext, html: HtmlWriter) {
        html.attr("style", "white-space: pre-wrap;")
        context.delegateRender()
    }

    private fun renderHeading(node: Heading, context: NodeRendererContext, html: HtmlWriter) {
        if (node.level == 3) {
            html.attr("style", "margin-top: 4px; margin-bottom: 4px;")
        }
        context.delegateRender()
    }

    private fun renderParagraph(node: Paragraph, context: NodeRendererContext, html: HtmlWriter) {
        if (node.parent is BulletListItem || node.parent is OrderedListItem) {
            html.attr("style", "margin: 0; padding:0;")
        } else {
            html.attr("style", "margin-top: 4px; margin-bottom: 4px;")
        }
        context.delegateRender()
    }

    private fun renderCode(node: Code, context: NodeRendererContext, html: HtmlWriter) {
        html.attr("style", "color: " + ColorUtil.toHex(JBColor(0x00627A, 0xCC7832)))
        context.delegateRender()
    }

    private fun renderBulletListItem(
        node: BulletListItem,
        context: NodeRendererContext,
        html: HtmlWriter
    ) {
        html.attr("style", "margin-bottom: 4px;")
        context.delegateRender()
    }

    private fun renderOrderedListItem(
        node: OrderedListItem,
        context: NodeRendererContext,
        html: HtmlWriter
    ) {
        html.attr("style", "margin-bottom: 4px;")
        context.delegateRender()
    }

    class Factory : NodeRendererFactory {
        override fun apply(options: DataHolder): NodeRenderer {
            return ResponseNodeRenderer()
        }
    }
}