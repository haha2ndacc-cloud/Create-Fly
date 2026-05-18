package com.zurrtum.create.client.catnip.lang;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.LightCoordsUtil;

import java.text.BreakIterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ClientFontHelper {

    public static List<String> cutString(Font font, String text, int maxWidthPerLine) {
        // Split words
        List<String> words = new LinkedList<>();
        String selected = Minecraft.getInstance().getLanguageManager().getSelected();
        final String[] langSplit = selected.split("_", 2);
        Locale locale = langSplit.length == 1 ? Locale.of(langSplit[0]) : Locale.of(langSplit[0], langSplit[1]);
        BreakIterator iterator = BreakIterator.getLineInstance(locale);
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String word = text.substring(start, end);
            words.add(word);
        }
        // Apply hard wrap
        List<String> lines = new LinkedList<>();
        StringBuilder currentLine = new StringBuilder();
        int width = 0;
        for (String word : words) {
            int newWidth = font.width(word);
            if (width + newWidth > maxWidthPerLine) {
                if (width > 0) {
                    String line = currentLine.toString();
                    lines.add(line);
                    currentLine = new StringBuilder();
                    width = 0;
                } else {
                    lines.add(word);
                    continue;
                }
            }
            currentLine.append(word);
            width += newWidth;
        }
        if (width > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }

    public static void submitSplitString(
        GuiGraphicsExtractor graphics,
        Font font,
        String text,
        int x,
        int y,
        int width,
        int color
    ) {
        List<String> list = cutString(font, text, width);
        boolean rightToLeft = font.isBidirectional();
        for (String s : list) {
            int f = x;
            if (rightToLeft) {
                int i = font.width(font.bidirectionalShaping(s));
                f += width - i;
            }
            graphics.text(font, s, f, y, color, false);
            y += 9;
        }
    }

    public static void submitSplitString(
        SubmitNodeCollector queue,
        PoseStack matrixStack,
        Font font,
        String text,
        int x,
        int y,
        int width,
        int color
    ) {
        List<String> list = cutString(font, text, width);
        boolean rightToLeft = font.isBidirectional();
        Language language = Language.getInstance();
        for (String s : list) {
            int f = x;
            if (rightToLeft) {
                int i = font.width(font.bidirectionalShaping(s));
                f += width - i;
            }
            queue.submitText(
                matrixStack,
                f,
                y,
                language.getVisualOrder(FormattedText.of(s)),
                false,
                Font.DisplayMode.NORMAL,
                LightCoordsUtil.FULL_BRIGHT,
                color,
                0,
                0
            );
            y += 9;
        }
    }
}
