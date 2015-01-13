package de.haw.ttvp.ui;

import java.awt.Point;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.*;

public class Dialog {
  public final static Point DIALOG_POSITION = new Point(0,0);
  
  public static boolean confirm(String question, String title) {
    JOptionPane pane = new JOptionPane(question, QUESTION_MESSAGE, YES_NO_OPTION);
    JDialog dialog = pane.createDialog(title);
    dialog.setLocation(DIALOG_POSITION);
    dialog.setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setVisible(true);
    return pane.getValue().equals(YES_OPTION);    
  }

}
