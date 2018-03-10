package topdownshooting;

import java.awt.Color;
import java.awt.Graphics2D;

public class Bullet {
	//FIELD
	private double x;
	private double y;
	private int r;
	
	private double dx;
	private double dy;
	private double rad;
	private double speed;
	
	private boolean outbound;
	
	private Color color1;
	
	//CONSTRUCTOR
	public Bullet (double angle, double x, double y) {
//		set x, y, means shoot a bullet from player position;
		this.x = x;
		this.y = y;
		r = 2;
		
		speed = 10;
		outbound = false;
		
//		init angle 270,, means from the front of play (0, -1),
	//		need to be converted to Radian because cos,sin(radian);
		rad = Math.toRadians(angle);
		dx = Math.cos(rad) * speed;
		dy = Math.sin(rad) * speed;
		
		color1 = Color.YELLOW;
	}
	
	//GETTER
	public double getx() { return x; }
	public double gety() { return y; }
	public double getr() { return r; }	
	
	public boolean isOutbound() { return outbound; }
	
	/*****************************
			UPDATE
	 *****************************/
	public void update() {
		x += dx;
		y += dy;
//		removing cdt, when bullet is outbound,	
	// 	-ve value, just to ensure that it's out bound;
		if (x < -r || x > GamePanel.WIDTH + r ||
			y < -r || y > GamePanel.HEIGHT + r) {
			outbound = true;
		}
	}
	
	/*****************************
				RENDER
	 *****************************/
	public void draw(Graphics2D g) {
		g.setColor(color1);
		g.fillOval((int)(x - r), (int)(y - r), 2 * r, 2 * r);
	}
	

}
