package dexter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 @author Giovanni Caire - TILAB
 */
class AddPostGui extends JFrame {
    private CompanyAgent myAgent;

    private JTextField ratingField, oldField, salaryField;

    AddPostGui(CompanyAgent a) {
        super(a.getLocalName());

        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(3, 2));
        p.add(new JLabel("Rating:"));
        ratingField = new JTextField(15);
        p.add(ratingField);
        p.add(new JLabel("Age limit:"));
        oldField = new JTextField(15);
        p.add(oldField);
        p.add(new JLabel("Salary:"));
        salaryField = new JTextField(15);
        p.add(salaryField);
        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Add post");
        addButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String rating = ratingField.getText().trim();
                    String price = oldField.getText().trim();
                    String salary = salaryField.getText().trim();
                    myAgent.updateCatalogue(Integer.parseInt(rating), Integer.parseInt(price), Integer.parseInt(salary));
                    ratingField.setText("");
                    oldField.setText("");
                    salaryField.setText("");
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(AddPostGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } );
        p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new	WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        } );

        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }
}
