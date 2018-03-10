package topdownshooting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class PowerUp {
	private double x;
	private double y;
	private int r;
	
	private int type;
	
	private boolean outbound;
	
	private Color color1;
	
	// 1 - power++
	// 2 - power+2
	// 3 - life++
	// 4 - slowDown
	
	//CONSTRUCTOR
	public PowerUp(double x, double y, int type) {
		this.x = x;
		this.y = y;
		this.type = type;
		
		outbound = false;
	}
	
	public double getX() { return x; }
	public double getY() { return y; }
	public int getR() { return r; }
	
	public int getType() { return type; }
	
	public boolean isOutbound() { return outbound; }

	
	/*****************************
			UPDATE
	 *****************************/
	public void update() {
		y += 2;
		if (type == 1) {
			r = 4;
			color1 = Color.ORANGE; 
		}
		if (type == 2) {
			r = 5;
			color1 = Color.YELLOW; 
		}
		if (type == 3) {
			r = 5;
			color1 = Color.WHITE; 
		}
		if (type == 4) {
			r = 5;
			color1 = Color.RED;
		}
		
		if (y > GamePanel.HEIGHT + r) {
			outbound = true;
		}
		
	}
	
	
	/*****************************
				RENDER
	 *****************************/
	public void draw(Graphics2D g) {
		g.setColor(color1);
		g.fillRect((int)(x - r), (int)(y - r), 2 * r, 2 * r);
		g.setStroke(new BasicStroke(3));
		g.setColor(color1.darker());
		g.drawRect((int)(x - r), (int)(y - r), 2 * r, 2 * r);
		g.setStroke(new BasicStroke(1));
	}
	
	
}
