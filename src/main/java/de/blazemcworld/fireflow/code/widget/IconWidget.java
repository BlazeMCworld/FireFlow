package de.blazemcworld.fireflow.code.widget;

import de.blazemcworld.fireflow.code.CodeInteraction;
import de.blazemcworld.fireflow.util.TextWidth;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class IconWidget extends Widget {

    private final double size;
    private final ItemWidget item;
    private final List<TextWidget> label;

    public IconWidget(WidgetVec pos, ItemStack item, String label, double size) {
        super(pos);
        this.size = size;
        this.item = new ItemWidget(pos, item, size);
        this.label = wordWrap(label, (int) (size * 40));
    }

    @Override
    public WidgetVec size() {
        WidgetVec out = new WidgetVec(pos().editor(), size, size);
        for (TextWidget t : label) {
            WidgetVec s = t.size();
            out = out.add(0, s.y()).max(s.x(), 0);
        }
        return out;
    }

    @Override
    public void update() {
        WidgetVec iconSize = size();

        item.pos(pos().sub(iconSize.x() * 0.5 - item.size().x() * 0.5, 0));
        item.update();

        BorderWidget<Widget> border = new BorderWidget<>(item);
        border.padding = 0;
        border.margin = 0;

        double yShift = size;
        for (TextWidget t : label) {
            WidgetVec textSize = t.size();
            t.pos(pos().sub(iconSize.x() * 0.5 - textSize.x() * 0.5, yShift));
            t.update();
            yShift += textSize.y();
        }
    }

    @Override
    public void remove() {
        item.remove();
        for (TextWidget t : label) t.remove();
    }

    private List<TextWidget> wordWrap(String label, int max) {
        String[] words = label.split(" ");
        List<TextWidget> out = new ArrayList<>();

        double width = 0;
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            double next = TextWidth.calculate(word, false);
            if (next + width > max && !current.isEmpty()) {
                out.add(new TextWidget(pos(), Text.literal(current.toString())).stretch(0.5, 0.5));
                current = new StringBuilder();
                width = 0;
            }
            width += next;
            if (current.isEmpty()) current = new StringBuilder(word);
            else current.append(" ").append(word);
        }
        if (!current.isEmpty()) {
            out.add(new TextWidget(pos(), Text.literal(current.toString())).stretch(0.5, 0.5));
        }

        return out;
    }

    @Override
    public List<Widget> getChildren() {
        List<Widget> out = new ArrayList<>();
        out.add(item);
        out.addAll(label);
        return out;
    }

    @Override
    public boolean interact(CodeInteraction i) {
        return false;
    }
}
