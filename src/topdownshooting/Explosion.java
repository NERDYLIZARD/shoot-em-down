package topdownshooting;

import java.awt.Color;
import java.awt.Graphics2D;

public class Explosion {
//	 FIELD 
	private double x;
	private double y;
	private int r;
	
	private int maxRadius;
	
	private boolean explode;
	
	
//	SETTER
	public boolean isExploded() { return explode; }
	
//	 CONSTRUCTOR 
	public Explosion(double x, double y, int r, int max) {
		this.x = x;
		this.y = y;
		this.r = r;
		
		maxRadius = max;
		
		explode = false;
	}
	
	
	/*****************************
			UPDATE
	 *****************************/
	public void update() {
		r += 2;
		if (r >= maxRadius) {
			explode = true;
		}
	}
	
	
	/*****************************
			RENDER
	 *****************************/
	public void draw(Graphics2D g) {
		g.setColor(new Color(255, 255, 255, 128));
		g.drawOval((int)(x - r), (int)(y -r), 2 * r, 2 * r);
	}	
	
}
