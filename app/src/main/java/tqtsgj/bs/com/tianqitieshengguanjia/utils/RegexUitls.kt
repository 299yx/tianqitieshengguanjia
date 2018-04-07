package tqtsgj.bs.com.tianqitieshengguanjia.utils

import java.util.regex.Pattern

/**
 * Created by Administrator on 2018/3/9 0009.
 */
object RegexUitls {

     val CDATA = "<!\\[CDATA\\[(.*)]]>"

    fun compile(string: String, regex: String): String {
        val compile = Pattern.compile(regex)
        val matcher = compile.matcher(string)
        return if (matcher.matches()) {
            matcher.group(1)
        } else {
            ""
        }
    }
}