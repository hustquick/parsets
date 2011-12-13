/*
 * 
 * 
 */

package edu.uncc.parsets.parsets;

import edu.uncc.parsets.data.CategoryHandle;
import edu.uncc.parsets.data.CategoryNode;
import java.util.List;

/**
 *
 * @author aacain
 */
public class SelectionChangeEvent {
    public static final String SELECTION_CHANGE = "SELECTION_CHANGE";
    public static final String CREATE_NEW_VIEW = "CREATE_NEW_VIEW";
    
    private final String eventType;
    private final CategoryNode selectedNode;
    private final List<CategoryHandle> filteredCategories;
    private final boolean isOnCategoryBar;

    public SelectionChangeEvent(String eventType, CategoryNode selectedNode, List<CategoryHandle> filteredCategories, boolean isOnCategoryBar) {
        this.eventType = eventType;
        this.selectedNode = selectedNode;
        this.filteredCategories = filteredCategories;
        this.isOnCategoryBar = isOnCategoryBar;
    }

    public String getEventType() {
        return eventType;
    }

    public List<CategoryHandle> getFilteredCategories() {
        return filteredCategories;
    }

    public CategoryNode getSelectedCategory() {
        return selectedNode;
    }
    
    public boolean isOnCategoryBar(){
        return this.isOnCategoryBar;
    }
}
