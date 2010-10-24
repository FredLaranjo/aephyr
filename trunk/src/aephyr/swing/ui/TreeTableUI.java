package aephyr.swing.ui;

import javax.swing.plaf.ComponentUI;
import javax.swing.tree.TreePath;

import aephyr.swing.TreeTable;
import aephyr.swing.treetable.DefaultTreeTableCellEditor;
import aephyr.swing.treetable.DefaultTreeTableCellRenderer;
import aephyr.swing.treetable.TreeTableCellEditor;
import aephyr.swing.treetable.TreeTableCellRenderer;

public abstract class TreeTableUI extends ComponentUI {
	
	public abstract TreeInterface getTreeInterface(TreeTable treeTable);
	
	public abstract TableInterface getTableInterface(TreeTable treeTable);

	public abstract void configureCellRenderer(
			DefaultTreeTableCellRenderer renderer, TreeTable treeTable,
			Object value, boolean selected, boolean hasFocus,
			int row, int column);
	

	public abstract void configureCellRenderer(
			DefaultTreeTableCellRenderer renderer, TreeTable treeTable,
			Object value, boolean selected, boolean hasFocus,
			int row, int column, boolean expanded, boolean leaf);
	
	public abstract void configureCellEditor(DefaultTreeTableCellEditor editor,
			TreeTable treeTable, Object value, boolean selected, int row, int column);
	
	public abstract void configureCellEditor(DefaultTreeTableCellEditor editor,
			TreeTable treeTable, Object value, boolean selected,
			int row, int column, boolean expanded, boolean leaf);
	
	public abstract TreeTableCellRenderer getDefaultRenderer(Class<?> columnClass);

	public abstract TreeTableCellEditor getDefaultEditor(Class<?> columnClass);
}
