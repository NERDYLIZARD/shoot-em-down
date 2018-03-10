package topdownshooting;

import javax.swing.JFrame;

public class Game {

	public static void main(String[] args) {
		
		JFrame window = new JFrame("shoot");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setContentPane(new GamePanel());
//		pack/allocate the setDefualtSize from GamePanel class (JPanel)
	//		otw, just window.setSize(), 
		//		better let JPanel does its work,
			// 		JPanel is placed on top of JFrame;
		window.pack();
		window.setLocationRelativeTo(null);
//		window.setResizable(false);
		window.setVisible(true);
	}

}
