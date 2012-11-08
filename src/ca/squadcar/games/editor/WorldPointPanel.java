package ca.squadcar.games.editor;

import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JSpinner;
import java.awt.Insets;
import java.util.ResourceBundle;

@SuppressWarnings("serial")
public class WorldPointPanel extends JPanel {

	private WorldPoint point;
	
	/**
	 * Create the panel.
	 */
	public WorldPointPanel(WorldPoint point, String label) {
		
		this.point = point;
		
		setBorder(new TitledBorder(null, label, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblX = new JLabel("X:");
		GridBagConstraints gbc_lblX = new GridBagConstraints();
		gbc_lblX.insets = new Insets(0, 0, 5, 5);
		gbc_lblX.gridx = 0;
		gbc_lblX.gridy = 0;
		add(lblX, gbc_lblX);
		
		JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(point.x, -Globals.SPINNER_EXTENT, Globals.SPINNER_EXTENT, Globals.SPINNER_INC));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 0;
		add(xSpinner, gbc_spinner);
		
		JLabel lblY = new JLabel("Y:");
		GridBagConstraints gbc_lblY = new GridBagConstraints();
		gbc_lblY.insets = new Insets(0, 0, 0, 5);
		gbc_lblY.gridx = 0;
		gbc_lblY.gridy = 1;
		add(lblY, gbc_lblY);
		
		// NOTE: y is flipped!!!
		JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(-point.y, -Globals.SPINNER_EXTENT, Globals.SPINNER_EXTENT, Globals.SPINNER_INC));
		GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.gridx = 1;
		gbc_spinner_1.gridy = 1;
		add(ySpinner, gbc_spinner_1);
	}
}
