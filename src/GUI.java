import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GUI extends JFrame {
    private final GridBagConstraints c = new GridBagConstraints(); //layout manager
    private int column = 0;
    private JPanel panel;

    GUI(BufferedImage image1, BufferedImage image2, BufferedImage image3) {
        panel = createPanel(20,20,20,20);
        panel.setVisible(true);
        add(panel, BorderLayout.CENTER);
        displayImage(image1);
        displayImage(image2);
        displayImage(image3);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void displayImage(BufferedImage image) {
        ImageIcon imageIcon = new ImageIcon(image);
        JLabel imageLabel = new JLabel(imageIcon);
        grid( imageLabel, panel, 0, column, GridBagConstraints.BOTH,5,5,
                5,5, 1, 1, GridBagConstraints.CENTER);
        column++;
    }

    protected JPanel createPanel(int top, int left, int bottom, int right) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        panel.setVisible(true);
        return panel;
    }

    protected void grid(Component widget, JPanel panel, int row, int col,
                        int sticky, int ipadx, int ipady, int weightx,
                        int weighty, int gridwidth, int gridheight, int anchor) {
        c.gridx = col;
        c.gridy = row;
        c.weightx = weightx;
        c.weighty = weighty;
        c.fill = sticky;
        c.ipadx = ipadx;
        c.ipady = ipady;
        c.gridwidth = gridwidth;
        c.gridheight = gridheight;
        c.anchor = anchor;
        widget.setVisible(true);
        panel.add(widget, c);
    }
}
