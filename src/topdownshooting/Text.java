package topdownshooting;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class Text {
//	FILED 
	private double x;
	private double y;
	private long time;
	private String s;
	
	private long start;
	
	private boolean display;
	
//	CONSTRUCTOR 
	public Text(double x, double y, long time, String s) {
		this.x = x;
		this.y = y;
		this.time = time;
		this.s = s;
		start = System.nanoTime();
		
		display = true;
	}
	
//	GETTER 
	public boolean isDisplaying() { return display; }
	
	
	/*****************************
			UPDATE
	 *****************************/
	public void update() {
		long elapsed = (System.nanoTime() - start) / 1000000;
		if (elapsed > time) {
			display = false;
		}
	}
	
	
	/*****************************
			RENDER
	 *****************************/
	public void draw(Graphics2D g) {
		g.setFont(new Font("Century Gothic", Font.PLAIN, 12));
		long elapsed = (System.nanoTime() - start) / 1000000;
		int alpha = (int) (255 * Math.sin(3.14 * elapsed / time));
		if (alpha > 255) alpha = 255;
		if (alpha < 0) alpha = 0;
		g.setColor(new Color(255, 255, 255, alpha));
		int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
		g.drawString(s, (int) (x - (length / 2)), (int) y);
	}
	
}
