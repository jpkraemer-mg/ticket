package quest.darkoro.transcripts;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Formatter {

    private final Pattern STRONG = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private final Pattern EM = Pattern.compile("\\*(.+?)\\*");
    private final Pattern S = Pattern.compile("~~(.+?)~~");
    private final Pattern U = Pattern.compile("__(.+?)__");
    private final Pattern CODE = Pattern.compile("```(.+?)```");
    private final Pattern CODE_1 = Pattern.compile("`(.+?)`");
    private final Pattern QUOTE = Pattern.compile("^>{1,3} (.*)$");
    private final Pattern LINK = Pattern.compile("\\[([^\\[]+)\\](\\((www|http:|https:)+[^\\s]+[\\w]\\))");
    private final Pattern NEW_LINE = Pattern.compile("\\n");

    public String formatBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = String.valueOf("KMGTPE".charAt(exp - 1));
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public String format(String originalText) {
        Matcher matcher = STRONG.matcher(originalText);
        String newText = originalText;
        while (matcher.find()) {
            String group = matcher.group();
            newText = newText.replace(group,
                    "<strong>" + group.replace("**", "") + "</strong>");
        }
        matcher = EM.matcher(newText);
        while (matcher.find()) {
            String group = matcher.group();
            newText = newText.replace(group,
                    "<em>" + group.replace("*", "") + "</em>");
        }
        matcher = S.matcher(newText);
        while (matcher.find()) {
            String group = matcher.group();
            newText = newText.replace(group,
                    "<s>" + group.replace("~~", "") + "</s>");
        }
        matcher = U.matcher(newText);
        while (matcher.find()) {
            String group = matcher.group();
            newText = newText.replace(group,
                    "<u>" + group.replace("__", "") + "</u>");
        }
        matcher = QUOTE.matcher(newText);
        while (matcher.find()) {
            String group = matcher.group();
            newText = newText.replace(group,
                    "<span class=\"quote\">" + group.replaceFirst(">>>", "").replaceFirst(">", "") + "</span>");
        }
        matcher = LINK.matcher(newText);
        while (matcher.find()) {
            String group = matcher.group(1);
            String link = matcher.group(2);
            String raw = "[" + group + "]" + link;

            newText = newText.replace(raw, "<a href=\"" + link.replace("(", "").replace(")", "") + "\">" + group + "</a>");
        }

        matcher = CODE.matcher(newText);
        boolean findCode = false;
        while (matcher.find()) {
            String group = matcher.group();
            newText = newText.replace(group,
                    "<div class=\"pre pre--multiline nohighlight\">"
                            + group.replace("```", "") + "</div>");
            findCode = true;
        }
        if (!findCode) {
            matcher = CODE_1.matcher(newText);
            while (matcher.find()) {
                String group = matcher.group();
                newText = newText.replace(group,
                        "<span class=\"pre pre--inline\">" + group.replace("`", "") + "</span>");
            }
        }
        matcher = NEW_LINE.matcher(newText);
        while (matcher.find()) {
            newText = newText.replace(matcher.group(), "<br />");
        }
        return newText;
    }

    public String toHex(Color color) {
        String hex = Integer.toHexString(color.getRGB() & 0xffffff);
        while (hex.length() < 6) {
            hex = "0" + hex;
        }
        return hex;
    }
}
