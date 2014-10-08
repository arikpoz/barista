package barista.model;

import javafx.collections.ObservableList;

/**
 * Used to mark an object as hierarchical data.
 * This object can then be used as data source for an hierarchical control, like the {@link javafx.scene.control.TreeView}.
 *
 * based on the work of Christian Schudt, see http://myjavafx.blogspot.co.il/2012/03/treeview-with-data-source.html
 * 
 * @author Christian Schudt
 */
public interface HierarchyData<T extends HierarchyData> {
    /**
     * The children collection, which represents the recursive nature of the hierarchy.
     * Each child is again a {@link HierarchyData}.
     *
     * @return A list of children.
     */
    ObservableList<T> getChildren();
}
