package io.github.rothes.hustauth.appenders;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.swing.*;
import java.io.Serializable;

@Plugin(name = "GuiConsoleAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class GuiConsoleAppender extends AbstractAppender {

    protected int maxLines = 250;
    protected final JTextArea textArea = new JTextArea();
    protected final JScrollPane scrollPane = new JScrollPane(textArea);

    public GuiConsoleAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
        textArea.setEditable(false);
    }

    public int getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(int maxLines) {
        this.maxLines = maxLines;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    @PluginFactory
    public static GuiConsoleAppender createAppender(@PluginAttribute("name") String name,
                                                    @PluginElement("Filters") Filter filter,
                                                    @PluginElement("PatternLayout") Layout<? extends Serializable> layout,
                                                    @PluginAttribute("ignoreExceptions") boolean ignoreExceptions) {
        return new GuiConsoleAppender(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(LogEvent event) {
        PatternLayout layout = (PatternLayout) getLayout();
        textArea.append(layout.toSerializable(event));
        if ((maxLines > 0) && (textArea.getLineCount() > (maxLines + 1))) {
            String text = textArea.getText();
            int pos = text.indexOf('\n');
            text = text.substring(pos + 1);
            textArea.setText(text);
        }
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

}
