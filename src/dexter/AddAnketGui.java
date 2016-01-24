package dexter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


class AddAnketGui extends JFrame {
    private EmployerAgent myAgent;

    private JTextField nameField, ratingField, ageField;

    AddAnketGui(EmployerAgent a) {
        super(a.getLocalName());

        myAgent = a;

        JPanel p = new JPanel();
        p.setLayout(new GridLayout(3, 2));
        p.add(new JLabel(("Name:")));
        nameField = new JTextField(15);
        p.add(nameField);
        p.add(new JLabel("Rating:"));
        ratingField = new JTextField(15);
        p.add(ratingField);
        p.add(new JLabel("Age:"));
        ageField = new JTextField(15);
        p.add(ageField);
        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Add resume");
        addButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String name = nameField.getText().trim();
                    String rating = ratingField.getText().trim();
                    String age = ageField.getText().trim();
                    myAgent.onAddedAnket(name, Integer.parseInt(rating), Integer.parseInt(age));
                    nameField.setText("");
                    ratingField.setText("");
                    ageField.setText("");
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(AddAnketGui.this,
                            "Invalid values. "+e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
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
