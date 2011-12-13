/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uncc.parsets.parsets;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

/**
 *
 * @author lucy
 */
public abstract class PopupPresenter implements SelectionChangeListener{
    public abstract JMenuItem getJMenuItem();
}
