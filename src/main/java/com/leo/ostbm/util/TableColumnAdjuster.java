package com.leo.ostbm.util;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * Class to manage the widths of columns in a table.
 * <p>
 *
 * <a href=
 * "http://tips4java.wordpress.com/2008/11/10/table-column-adjuster/">http://tips4java.wordpress.com/2008/11/10/table-column-adjuster/</a>
 * <p>
 *
 * Original source can be found
 * <a href="https://gist.github.com/tarple/dfebce9502b92559dd63">here</a>.
 *
 * @author tarple@github
 */

public class TableColumnAdjuster {

	// the amount of padding to add to each column after determining the width
	// of its data
	private static final int SPACING = 10;

	// this TableColumnAdjuster's JTable
	private final JTable table;

	/**
	 * Constructs a new TableColumnnAdjuster, which specifies this Adjuster's table
	 *
	 * @param table - this Adjuster's JTable
	 */
	public TableColumnAdjuster(final JTable table) {
		this.table = table;
	}

	/**
	 * Adjusts the widths of all the columns in the table based off the length of
	 * the longest String of data in each column
	 */
	public void adjustColumns() {
		final TableColumnModel tcm = table.getColumnModel();
		for (int i = 0; i < tcm.getColumnCount(); i++)
			adjustColumn(i);
	}

	// Adjust the width of the specified column in the table
	private void adjustColumn(final int column) {
		final TableColumn tableColumn = table.getColumnModel().getColumn(column);

		if (!tableColumn.getResizable())
			return;

		final int columnHeaderWidth = getColumnHeaderWidth(column);
		final int columnDataWidth = getColumnDataWidth(column);
		final int preferredWidth = Math.max(columnHeaderWidth, columnDataWidth);

		updateTableColumn(column, preferredWidth);
	}

	// Calculates the given column's width based on header name
	private int getColumnHeaderWidth(final int column) {

		final TableColumn tableColumn = table.getColumnModel().getColumn(column);
		final Object value = tableColumn.getHeaderValue();
		TableCellRenderer renderer = tableColumn.getHeaderRenderer();

		if (renderer == null)
			renderer = table.getTableHeader().getDefaultRenderer();

		final Component c = renderer.getTableCellRendererComponent(table, value, false, false, -1, column);
		return c.getPreferredSize().width;
	}

	// Calculates the width based on the widest cell renderer for the given
	// column.
	private int getColumnDataWidth(final int column) {
		int preferredWidth = 0;

		for (int row = 0; row < table.getRowCount(); row++)
			preferredWidth = Math.max(preferredWidth, getCellDataWidth(row, column));

		return preferredWidth;
	}

	// Gets the preferred width for the specified cell
	private int getCellDataWidth(final int row, final int column) {
		// Invoke the renderer for the cell to calculate the preferred width
		final TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
		final Object value = table.getValueAt(row, column);
		final Component c = cellRenderer.getTableCellRendererComponent(table, value, false, false, row, column);
		final int width = c.getPreferredSize().width + table.getIntercellSpacing().width;

		return width;
	}

	// Updates the TableColumn with the newly calculated width
	private void updateTableColumn(final int column, final int width) {
		final TableColumn tableColumn = table.getColumnModel().getColumn(column);

		if (!tableColumn.getResizable())
			return;

		table.getTableHeader().setResizingColumn(tableColumn);
		tableColumn.setWidth(width + SPACING);
	}
}
