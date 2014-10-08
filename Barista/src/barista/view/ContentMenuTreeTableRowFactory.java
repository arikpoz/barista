package barista.view;
//package edu.marshall.denvir.examples;
// based on code by James Denvir, see http://www.marshall.edu/genomicjava/2013/12/30/reusable-row-and-cell-factories-for-tableviews-that-generate-contextmenus/

import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.util.Callback;

public class ContentMenuTreeTableRowFactory<T> implements Callback<TreeTableView<T>, TreeTableRow<T>> {

    private final Callback<T, List<MenuItem>> menuItemFactory;
    private final Callback<TreeTableView<T>, TreeTableRow<T>> rowFactory;

    public ContentMenuTreeTableRowFactory(Callback<TreeTableView<T>, TreeTableRow<T>> rowFactory, Callback<T, List<MenuItem>> menuItemFactory) {
        this.rowFactory = rowFactory;
        this.menuItemFactory = menuItemFactory;
    }

    public ContentMenuTreeTableRowFactory(Callback<T, List<MenuItem>> menuItemFactory) {
        this(null, menuItemFactory);
    }

    @Override
    public TreeTableRow<T> call(TreeTableView<T> table) {
        final TreeTableRow<T> row;
        if (rowFactory == null) {
            row = new TreeTableRow<T>();
        } else {
            row = rowFactory.call(table);
        }
        row.itemProperty().addListener(new ChangeListener<T>() {
            @Override
            public void changed(ObservableValue<? extends T> observable,
                    T oldValue, T newValue) {
                if (newValue == null) {
                    row.setContextMenu(null);
                } else {
                    row.setContextMenu(createContextMenu(row));
                }
            }
        });
        return row;
    }

    private ContextMenu createContextMenu(final TreeTableRow<T> row) {
        ContextMenu menu = new ContextMenu();
        ContextMenu tableMenu = row.getTreeTableView().getContextMenu();
        if (tableMenu != null) {
            menu.getItems().addAll(tableMenu.getItems());
            menu.getItems().add(new SeparatorMenuItem());
        }
        menu.getItems().addAll(menuItemFactory.call(row.getItem()));
        return menu;
    }
}
