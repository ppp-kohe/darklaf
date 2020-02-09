/*
 * MIT License
 *
 * Copyright (c) 2020 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package demo.list;

import demo.ComponentDemo;
import demo.DemoPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class ListDemo implements ComponentDemo {

    public static void main(final String[] args) {
        ComponentDemo.showDemo(new ListDemo());
    }

    @Override
    public JComponent createComponent() {
        String[] week = {"Monday", "Tuesday", "Wednesday",
                "Thursday", "Friday", "Saturday", "Sunday"};
        JList<String> list = new JList<>(week);
        list.setSelectedIndex(2);
        DemoPanel panel = new DemoPanel(list, new BorderLayout());
        JPanel controlPanel = panel.getControls();
        controlPanel.setLayout(new GridLayout(3, 2));
        controlPanel.add(new JLabel());
        controlPanel.add(new JCheckBox("JList.alternateRowColor") {{
            setSelected(Boolean.TRUE.equals(list.getClientProperty("JList.alternateRowColor")));
            addActionListener(e -> list.putClientProperty("JList.alternateRowColor", isSelected()));
        }});
        controlPanel.add(new JLabel("Layout orientation:", JLabel.RIGHT));
        controlPanel.add(new JComboBox<String>() {{
            Map<String, Integer> mapping = new HashMap<String, Integer>() {{
                put("VERTICAL", JList.VERTICAL);
                put("VERTICAL_WRAP", JList.VERTICAL_WRAP);
                put("HORIZONTAL_WRAP", JList.HORIZONTAL_WRAP);
            }};
            addItem("VERTICAL");
            addItem("VERTICAL_WRAP");
            addItem("HORIZONTAL_WRAP");
            setSelectedItem("VERTICAL");
            //noinspection MagicConstant
            addItemListener(e -> list.setLayoutOrientation(mapping.get(e.getItem().toString())));
        }});
        controlPanel.add(new JLabel("Selection mode:", JLabel.RIGHT));
        controlPanel.add(new JComboBox<String>() {{
            Map<String, Integer> mapping = new HashMap<String, Integer>() {{
                put("SINGLE_SELECTION", ListSelectionModel.SINGLE_SELECTION);
                put("MULTIPLE_INTERVAL_SELECTION", ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                put("SINGLE_INTERVAL_SELECTION", ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            }};
            addItem("SINGLE_SELECTION");
            addItem("MULTIPLE_INTERVAL_SELECTION");
            addItem("SINGLE_INTERVAL_SELECTION");
            setSelectedItem("SINGLE_SELECTION");
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            //noinspection MagicConstant
            addItemListener(e -> list.setSelectionMode(mapping.get(e.getItem().toString())));
        }});
        return panel;
    }

    @Override
    public String getTitle() {
        return "List Demo";
    }
}
