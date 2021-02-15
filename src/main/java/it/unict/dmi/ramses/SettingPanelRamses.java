package it.unict.dmi.ramses;

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

public class SettingPanelRamses extends JPanel {

  private static final long serialVersionUID = 1L;

  private final JLabel labelJSpinner = new JLabel();
  private final JSpinner side = new JSpinner();
  private final SpinnerNumberModel model = new SpinnerNumberModel(15, 5, 100, 1);//(valore iniziale,valore minimo,valore massimo,step)

  private final Border bordoPannelloSetting;
  private final TitledBorder titoloBordoPannelloSetting;

  private final JPanel PannelloCheckBox = new JPanel();
  private final GridLayout LayoutPannelloCheckBox = new GridLayout();

  private final BorderLayout BorderLayoutPannelloSetting = new BorderLayout();

  private final JCheckBox colorize = new JCheckBox();
  private final JCheckBox uniformDistribution = new JCheckBox();
  private final JCheckBox mask = new JCheckBox();

  public SettingPanelRamses() {
    bordoPannelloSetting = BorderFactory.createEtchedBorder(Color.white, new Color(165, 163, 151));
    titoloBordoPannelloSetting = new TitledBorder(bordoPannelloSetting, "Settings ");

    side.setModel(model);
    labelJSpinner.setBorder(BorderFactory.createEtchedBorder());
    labelJSpinner.setText(" Side ");

    this.setBorder(titoloBordoPannelloSetting);
    this.setLayout(BorderLayoutPannelloSetting);

    colorize.setText("Colorize");
    colorize.setSelected(true);

    uniformDistribution.setText("Uniform Distribution");
    mask.setText("Create Alpha Mask Image");

    PannelloCheckBox.setLayout(LayoutPannelloCheckBox);
    LayoutPannelloCheckBox.setRows(1);
    LayoutPannelloCheckBox.setColumns(3);

    this.add(labelJSpinner, BorderLayout.WEST);
    this.add(side, BorderLayout.CENTER);
    this.add(PannelloCheckBox, java.awt.BorderLayout.EAST);//EAST SOUTH

    PannelloCheckBox.add(colorize);
    PannelloCheckBox.add(uniformDistribution);
    PannelloCheckBox.add(mask);
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
    side.setEnabled(b);
    colorize.setEnabled(b);
    uniformDistribution.setEnabled(b);
    mask.setEnabled(b);
  }
}
