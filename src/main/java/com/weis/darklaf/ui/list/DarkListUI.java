package com.weis.darklaf.ui.list;

import com.weis.darklaf.util.DarkUIUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author Jannis Weis
 */
public class DarkListUI extends DarkListUIBridge {

    static {
        UIManager.put("List.cellRenderer", new DarkListCellRenderer());
    }

    protected void paintImpl(final Graphics g, final JComponent c) {
        switch (layoutOrientation) {
            case JList.VERTICAL_WRAP:
                if (list.getHeight() != listHeight) {
                    updateLayoutStateNeeded |= heightChanged;
                    redrawList();
                }
                break;
            case JList.HORIZONTAL_WRAP:
                if (list.getWidth() != listWidth) {
                    updateLayoutStateNeeded |= widthChanged;
                    redrawList();
                }
                break;
            default:
                break;
        }
        maybeUpdateLayoutState();

        ListCellRenderer<Object> renderer = list.getCellRenderer();
        ListModel<Object> dataModel = list.getModel();
        ListSelectionModel selModel = list.getSelectionModel();

        if ((renderer == null) || dataModel.getSize() == 0) {
            return;
        }

        // Determine how many columns we need to paint
        Rectangle paintBounds = g.getClipBounds();

        int startColumn, endColumn;
        if (c.getComponentOrientation().isLeftToRight()) {
            startColumn = convertLocationToColumn(paintBounds.x,
                                                  paintBounds.y);
            endColumn = convertLocationToColumn(paintBounds.x +
                                                        paintBounds.width,
                                                paintBounds.y);
        } else {
            startColumn = convertLocationToColumn(paintBounds.x +
                                                          paintBounds.width,
                                                  paintBounds.y);
            endColumn = convertLocationToColumn(paintBounds.x,
                                                paintBounds.y);
        }
        int maxY = paintBounds.y + paintBounds.height;
        int leadIndex = adjustIndex(list.getLeadSelectionIndex(), list);
        int rowIncrement = (layoutOrientation == JList.HORIZONTAL_WRAP) ?
                           columnCount : 1;


        for (int colCounter = startColumn; colCounter <= endColumn;
             colCounter++) {
            // And then how many rows in this columnn
            int row = convertLocationToRowInColumn(paintBounds.y, colCounter);
            int rowCount = Math.max(rowsPerColumn, getRowCount(colCounter));
            int index = getModelIndex(colCounter, row);
            Rectangle rowBounds = getCellBounds(list, index, index);

            if (rowBounds == null) {
                // Not valid, bail!
                return;
            }
            while (row < rowCount && rowBounds.y < maxY) {
                rowBounds.height = getHeight(colCounter, row);
                g.setClip(rowBounds.x, rowBounds.y, rowBounds.width,
                          rowBounds.height);
                g.clipRect(paintBounds.x, paintBounds.y, paintBounds.width,
                           paintBounds.height);
                paintCell(g, index, rowBounds, renderer, dataModel, selModel,
                          leadIndex, row);
                rowBounds.y += rowBounds.height;
                index += rowIncrement;
                row++;
            }
        }
        // Empty out the renderer pane, allowing renderers to be gc'ed.
        rendererPane.removeAll();
    }

    @NotNull
    @Contract("_ -> new")
    public static ComponentUI createUI(final JComponent list) {
        return new DarkListUI();
    }

    protected void paintCell(final Graphics g, final int index, @NotNull final Rectangle rowBounds,
                             @NotNull final ListCellRenderer<Object> cellRenderer,
                             @NotNull final ListModel<Object> dataModel,
                             @NotNull final ListSelectionModel selModel, final int leadIndex, final int row) {
        boolean empty = index >= list.getModel().getSize();
        Object value = empty ? null : dataModel.getElementAt(index);
        boolean cellHasFocus = list.hasFocus() && (index == leadIndex);
        boolean isSelected = selModel.isSelectedIndex(index);

        int cx = rowBounds.x;
        int cy = rowBounds.y;
        int cw = rowBounds.width;
        int ch = rowBounds.height;

        if (empty) {
            boolean alternativeRow = Boolean.TRUE.equals(list.getClientProperty("JList.alternateRowColor"));
            Color alternativeRowColor = UIManager.getColor("List.alternateRowBackground");
            Color normalColor = list.getBackground();
            var background = alternativeRow && row % 2 == 1 ? alternativeRowColor : normalColor;
            var c = g.getColor();
            g.setColor(background);
            g.fillRect(cx, cy, cw, ch);
            g.setColor(c);
        } else {
            Component rendererComponent =
                    cellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (Boolean.TRUE.equals(list.getClientProperty("JList.shrinkWrap"))) {
                // Shrink renderer to preferred size. This is mostly used on Windows
                // where selection is only shown around the file name, instead of
                // across the whole list cell.
                int w = Math.min(cw, rendererComponent.getPreferredSize().width + 4);
                if (!list.getComponentOrientation().isLeftToRight()) {
                    cx += (cw - w);
                }
                cw = w;
            }

            rendererPane.paintComponent(g, rendererComponent, list, cx, cy, cw, ch, true);
        }
    }

    @Override
    protected void installDefaults() {
        super.installDefaults();
        list.putClientProperty("JList.alternateRowColor", UIManager.getBoolean("List.alternateRowColor"));
    }

    @Override
    protected Handler getHandler() {
        if (handler == null) {
            handler = new DarkHandler();
        }
        return handler;
    }


    protected class DarkHandler extends Handler {

        @Override
        protected void adjustSelection(final MouseEvent e) {
            int row = list.locationToIndex(e.getPoint());
            if (row < 0) {
                // If shift is down in multi-select, we should do nothing.
                // For single select or non-shift-click, clear the selection
                if (isFileList && !Boolean.TRUE.equals(list.getClientProperty("JList.fullRowSelection"))
                        && e.getID() == MouseEvent.MOUSE_PRESSED &&
                        (!e.isShiftDown() || list.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION)) {
                    list.clearSelection();
                }
            } else {
                int anchorIndex = adjustIndex(list.getAnchorSelectionIndex(), list);
                boolean anchorSelected;
                if (anchorIndex == -1) {
                    anchorIndex = 0;
                    anchorSelected = false;
                } else {
                    anchorSelected = list.isSelectedIndex(anchorIndex);
                }

                if (DarkUIUtil.isMenuShortcutKeyDown(e)) {
                    if (e.isShiftDown()) {
                        if (anchorSelected) {
                            list.addSelectionInterval(anchorIndex, row);
                        } else {
                            list.removeSelectionInterval(anchorIndex, row);
                            if (isFileList) {
                                list.addSelectionInterval(row, row);
                                list.getSelectionModel().setAnchorSelectionIndex(anchorIndex);
                            }
                        }
                    } else if (list.isSelectedIndex(row)) {
                        list.removeSelectionInterval(row, row);
                    } else {
                        list.addSelectionInterval(row, row);
                    }
                } else if (e.isShiftDown()) {
                    list.setSelectionInterval(anchorIndex, row);
                } else {
                    list.setSelectionInterval(row, row);
                }
            }
        }
    }
}
