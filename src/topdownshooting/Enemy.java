package topdownshooting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Enemy {
	private double x;
	private double y;
	private int r;
	
	private double dx;
	private double dy;
	private double rad;
	private double speed;
	
	private int health;
	private int type;
	private int rank;
	
	private Color colorNow;
	private Color colorNormal;
	private Color colorHit;
	
	private boolean dead;
	
	private boolean hit;
	private long hitTimer;
	
	private boolean slow;
	
	
	//CONSTRUCTOR
	public Enemy(int type, int rank, boolean slow) {
		this.type = type;
		this.rank = rank;
		this.slow = slow;
		
		//default enemy;
		if (type == 1) {
			colorNormal = new Color(0, 0, 255, 128);
			if (rank == 1) {
				r = 5;
				speed = 1;
				health = 1;
			}
			if (rank == 2) {
				r = 10;
				speed = 2;
				health = 2;
			}
			if (rank == 3) {
				r = 20;
				speed = 1.5;
				health = 3;
			}
			if (rank == 4) {
				r = 30;
				speed = 1.5;
				health = 4;
			}
		}
		// stronger, faster
		if (type == 2) {
			colorNormal = new Color(255, 0, 0, 128);
			if (rank == 1) {
				r = 5;
				speed = 3;
				health = 2;
			}
			if (rank == 2) {
				r = 10;
				speed = 3;
				health = 3;
			}
			if (rank == 3) {
				r = 20;
				speed = 2.5;
				health = 3;
			}
			if (rank == 4) {
				r = 30;
				speed = 2.5;
				health = 4;
			}
		}
		// slower, more endurant
		if (type == 3) {
			colorNormal = new Color(0, 255, 0, 128);
			if (rank == 1) {
				r = 5;
				speed = 1.5;
				health = 3;
			}
			if (rank == 2) {
				r = 10;
				speed = 1.5;
				health = 4;
			}
			if (rank == 3) {
				r = 25;
				speed = 1.5;
				health = 5;
			}
			if (rank == 4) {
				r = 45;
				speed = 1.5;
				health = 6;
			}
		}
		
//		spawn length = width / 2 in the center from top;
		x = GamePanel.WIDTH / 4 + Math.random() * GamePanel.WIDTH / 2;
		y = -r;
		
//		spreading angle from 20 - 159 degree randomly,
	//		acc to inverse-direction trigometric graph;
		double angle = Math.random() * 140 + 20;
		rad = Math.toRadians(angle);		
		dx = Math.cos(rad) * speed;
		dy = Math.sin(rad) * speed;

		colorHit = Color.WHITE;
		colorNow = colorNormal;

		hit = false;
		hitTimer = 0;
		
		dead = false;
	}
	
	
	//GETTER
	public double getx() { return x; }
	public double gety() { return y; }
	public int getr() { return r; }	
	
	public int getType() { return type; }
	public int getRank() { return rank; }
	
	public boolean isDead() { return dead; }
	
	//SETTER
	public void setSlow(boolean boo) { slow = boo; } 
	
	
	//HIT
	public void hit() {
		health--;
		if (health <= 0) {
			dead = true;
		}
		hit = true;
		hitTimer = System.nanoTime(); 
	}
	
	//EXPLODE
	public void explode() {
		if (rank > 1) {
			int amount = 0;
			if (type == 1) {
				amount = 3;
			}
			if (type == 2) {
				amount = 3;
			}
			if (type == 3) {
				amount = 4;
			}
			for (int i = 0; i < amount; i++) {
				Enemy e = new Enemy(getType(), getRank() - 1, slow);
				e.x = this.x;
				e.y = this.y;
				double angle = Math.random() * 360;		
				e.rad = Math.toRadians(angle);
				GamePanel.enemies.add(e);			
			}		
		}
	}
	
	
	/*****************************
			UPDATE
	 *****************************/
	public void update() {
		
		if (slow) {
			x += dx * 0.3;
			y += dy * 0.3;
		} else {
			x += dx;
			y += dy;
		}
		
//		BOUNCING
//		dx, dy < 0 are init cdt (initially outbound),
	//		but it's not suppose to bound at initial point,
		//		if cdt are only x, y < r, it will;
		if (x < r && dx < 0) dx = -dx;
		if (y < r && dy < 0) dy = -dy;
		// 		no need of cdt dx, dy > 0 here (not init state)
		if (x > GamePanel.WIDTH - r) dx = -dx;
		if (y > GamePanel.HEIGHT - r) dy = -dy;
	
		if (hit) {
			colorNow = colorHit;
			long elapsed = (System.nanoTime() - hitTimer) / 1000000;
			if(elapsed > 50) {
				colorNow = colorNormal;
				hit = false;
			}
		}
	}

	
	/*****************************
				RENDER
	 *****************************/
	public void draw(Graphics2D g) {
		
		g.setColor(colorNow);
		g.fillOval((int)(x - r), (int)(y - r), 2 * r, 2 * r);
		
		g.setStroke(new BasicStroke(3));
		g.setColor(colorNow.darker());
		g.drawOval((int)(x - r), (int)(y - r), 2 * r, 2 * r);
		g.setStroke(new BasicStroke(1));
		
	}


}
