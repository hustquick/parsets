/*
 * 
 * 
 */

package edu.uncc.parsets.parsets;

import edu.uncc.parsets.data.CategoryHandle;
import java.util.List;

/**
 *
 * @author aacain
 */
public class SelectionChangeEvent {
    public static final String SELECTION_CHANGE = "SELECTION_CHANGE";
    public static final String CREATE_NEW_VIEW = "CREATE_NEW_VIEW";
    
    private final String eventType;
    private final List<CategoryHandle> selectedCategories;
    private final List<CategoryHandle> filteredCategories;

    public SelectionChangeEvent(String eventType, List<CategoryHandle> selectedCategories, List<CategoryHandle> filteredCategories) {
        this.eventType = eventType;
        this.selectedCategories = selectedCategories;
        this.filteredCategories = filteredCategories;
    }

    public String getEventType() {
        return eventType;
    }

    public List<CategoryHandle> getFilteredCategories() {
        return filteredCategories;
    }

    public List<CategoryHandle> getSelectedCategories() {
        return selectedCategories;
    }
}
