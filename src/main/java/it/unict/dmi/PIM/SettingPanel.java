package it.unict.dmi.PIM;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public class SettingPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private JSpinner side = new JSpinner();
  private SpinnerNumberModel model = new SpinnerNumberModel(15, 5, 100, 1);
  private JLabel jLabel1 = new JLabel();
  private Border border1;
  private TitledBorder titledBorder1;
  private BorderLayout borderLayout1 = new BorderLayout();
  private JCheckBox colorize = new JCheckBox();
  private JPanel jPanel1 = new JPanel();
  private JCheckBox mask = new JCheckBox();
  private GridLayout gridLayout1 = new GridLayout();
  private JCheckBox uniformDistribution = new JCheckBox();

  @SuppressWarnings("CallToPrintStackTrace")
  public SettingPanel() {
    try {
      jbInit();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public int getSide() {
    return ((Number) side.getValue()).intValue();
  }

  public boolean isColorize() {
    return colorize.isSelected();
  }

  public boolean isCreateAlphaMaskImage() {
    return mask.isSelected();
  }

  public boolean isUniformDistribution() {
    return uniformDistribution.isSelected();
  }

  public void setComponentsEnabled(boolean b) {
    colorize.setEnabled(b);
    side.setEnabled(b);
    mask.setEnabled(b);
    uniformDistribution.setEnabled(b);
  }

  private void jbInit() throws Exception {
    border1 = BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151));
    titledBorder1 = new TitledBorder(border1, "Settings");
    side.setModel(model);
    jLabel1.setBorder(BorderFactory.createEtchedBorder());
    jLabel1.setText("Side");
    this.setBorder(titledBorder1);
    this.setLayout(borderLayout1);
    colorize.setText("Colorize");
    colorize.setSelected(true);
    mask.setText("Create Alpha Mask Image");
    jPanel1.setLayout(gridLayout1);
    gridLayout1.setRows(3);
    uniformDistribution.setText("Uniform Distribution");
    this.add(jLabel1, BorderLayout.WEST);
    this.add(side, BorderLayout.CENTER);
    this.add(jPanel1, java.awt.BorderLayout.SOUTH);
    jPanel1.add(colorize);
    jPanel1.add(mask);
    jPanel1.add(uniformDistribution);
  }
}
