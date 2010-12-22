/*
 * 
 * 
 */
package edu.uncc.parsets.gui;

/**
 *
 * @author aacain
 */
public interface AbstractMainView {

    public String getTitle();

    public void setDSMenuItemsEnabled(boolean enabled);

    public Controller getController();
}
