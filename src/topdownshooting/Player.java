package topdownshooting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class Player {
//	FIELD
	private double x;
	private double y;
	private int r;
	
	private int dx;
	private int dy;
	private int speed;
	private int lives;
	private int score;
	
	private boolean left;
	private boolean right;
	private boolean up;
	private boolean down;	
	
	private boolean firing;
	private long firingTimer;
	private long firingDelay = 200;
	
	private boolean recovering;
	private long recoveringTimer;
	private long recoveringTime = 2000;

	private long powerDepleteTimer;
	private long powerDepleteTimerDiff;
	private long powerDepleteCap = 30 * 1000;

	private int power;
	private int powerLevel;
// way to read: level 0 required 2 powers to be levelUp
//				level 1 required 3 powers to be levelUp
	private int powerRequired[] = { '\0', 2, 3, 4, 5, 6};

	private Color colorNormal;
	private Color colorNow;

	private boolean dead;
	
//	CONSTRUCTOR
	public Player() {
		x = GamePanel.WIDTH / 2;
		y = GamePanel.HEIGHT / 2;
		r = 5;

		dx = 0;
		dy = 0;
		speed = 5;
		lives = 3;
		
		colorNormal = Color.WHITE;
		colorNow = colorNormal;
		
		score = 0;
		
		firing = false;
		firingTimer = System.nanoTime();
		
		power = 0;
		powerLevel = 1;
	
		recovering = false;		
		
		dead = false;
	}
	
	//GETTER
	public double getx() { return x; }
	public double gety() { return y; }
	public int getr() { return r; }
	
	public int getLives() { return lives; }
	
	public int getScore() { return score; }

	public int getPower() { return power; }
	public int getPowerLevel() { return powerLevel; }
	public int getPowerRequired() { return powerRequired[powerLevel]; }

	public boolean isRecovering() { return recovering;}
	
	public boolean isDead() { return dead; }

	//SETTER
	public void setLeft(boolean boo) { left = boo; }
	public void setRight(boolean boo) { right = boo; }
	public void setUp(boolean boo) { up = boo; }
	public void setDown(boolean boo) { down = boo; }
	
	public void setFiring(boolean boo) { firing = boo; }
	
	public void addScore(int i) { score += i; }

	public void loseLife() {
		lives--;
		// POWERLEVEL DECREASE after losing life
		if (powerLevel <= 1) { 
			power = 0;
		} else {
			powerLevel--;
			power = 0;
		}
			
		if (lives <= 0) {
			dead = true;
		}
		recovering = true; 
		recoveringTimer = System.nanoTime();		
	}	
	
	
	public void gainLife() {
		if (lives < 5) {
			lives++;
		}
	}

	//INCREASE POWER
	public void increasePower(int i) {
		power += i;
//		reset count down when powerLevel = 4
		if (powerLevel > 3) {
			powerDepleteTimer = 0;
		}
//		don't increase anymore when max level
		if (powerLevel == 4) {
			if(power >= powerRequired[powerLevel]) {
//				powerLevel - 1 is for 
	//				for graphic representation;
				power = powerRequired[powerLevel - 1];
			}
			return;
		}
		if (power >= powerRequired[powerLevel]) {
			power -= powerRequired[powerLevel];
			powerLevel++;	
		}
	}
	
	
	/*****************************
			UPDATE
	 *****************************/
	public void update() {
//		dx & dy vary only once per loop;
		if(left) {
			dx = -speed;
		}
		if(right) {
			dx = speed;
		}
		if(up) {
			dy = -speed;
		}
		if(down) {
			dy = speed;
		}

		x += dx;
		y += dy;

//		top left 
	//		are 5(r) instead of (0, 0)
		if (x < r) x = r;
		if (y < r) y = r;
//		bottom right 
		if (x > GamePanel.WIDTH - r) x = GamePanel.WIDTH - r;
		if (y > GamePanel.HEIGHT - r) y = GamePanel.HEIGHT - r;
		
		dx = 0;
		dy = 0;
		
		
		if (firing) {
			long elapsed = (System.nanoTime() - firingTimer) / 1000000;
			if (elapsed > firingDelay) {
				firingTimer = System.nanoTime();
				
				if (powerLevel < 2) {
					GamePanel.bullets.add(new Bullet(270, x, y));
				}
				else if (powerLevel < 4) {
					GamePanel.bullets.add(new Bullet(270, x + 5, y));
					GamePanel.bullets.add(new Bullet(270, x - 5, y));
				}
				else {
					GamePanel.bullets.add(new Bullet(270, x, y));
					GamePanel.bullets.add(new Bullet(275, x + 5, y));
					GamePanel.bullets.add(new Bullet(265, x - 5, y));
				}
			}
		}
		
		if (recovering) {
			long elapsed = (System.nanoTime() - recoveringTimer) / 1000000;
//			player is flasing when get hit;
			int alpha = (int) (255 * Math.sin(3.14 * (30 * elapsed) / recoveringTime));
			if (alpha < 0) alpha = 0;
			colorNow = new Color(255, 255, 255, alpha);
			if (elapsed > recoveringTime) {
					colorNow = colorNormal;
					recovering = false;
			}
		}
	
//		POWER DEPLETE 
	//		power keep on depleting when powerLevel = 4, 
		//		in order to force player to keep collecting the powerUp
		if (powerLevel >= 4) {
			if (powerDepleteTimer == 0) {
				powerDepleteTimer = System.nanoTime();
			} else {
				powerDepleteTimerDiff = (System.nanoTime() - powerDepleteTimer) / 1000000;
				if (powerDepleteTimerDiff > powerDepleteCap) {
					powerDepleteTimer = 0;
//					condition
					if (power <= 0) {
						powerLevel--;
						power = powerRequired[powerLevel - 1];
					} else {
						power--;
					}
				} // end elapsed
			} // else
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
		
		if (powerLevel >= 4) {
			g.setColor(Color.WHITE);
			g.setFont(new Font("Century Gothic", Font.PLAIN, 10));
			g.drawString("Power: " + (powerDepleteCap - powerDepleteTimerDiff) / 1000, 20, 60);
		}
	}
	
}


