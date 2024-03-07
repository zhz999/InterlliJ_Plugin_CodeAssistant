package code_assistant.common

import code_assistant.common.ResponseNodeRenderer.Factory
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import java.util.regex.Pattern
import java.util.stream.Collectors


object MarkdownUtil {
    /**
     * Splits a given string into a list of strings where each element is either a code block
     * surrounded by triple backticks or a non-code block text.
     *
     * @param inputMarkdown The input markdown formatted string to be split.
     * @return A list of strings where each element is a code block or a non-code block text from the
     * input string.
     */
    fun splitCodeBlocks(inputMarkdown: String): List<String> {
        val result: MutableList<String> = ArrayList()
        val pattern = Pattern.compile("(?s)```.*?```")
        val matcher = pattern.matcher(inputMarkdown)
        var start = 0
        while (matcher.find()) {
            result.add(inputMarkdown.substring(start, matcher.start()))
            result.add(matcher.group())
            start = matcher.end()
        }
        result.add(inputMarkdown.substring(start))
        return result.stream().filter { item: String -> item.isNotBlank() }.collect(Collectors.toList())
    }

    fun convertMdToHtml(message: String?): String {
        val options = MutableDataSet()
        val document = Parser.builder(options).build().parse(
            message!!
        )
        return HtmlRenderer.builder(options)
            .nodeRendererFactory(Factory())
            .build()
            .render(document)
    }
}

