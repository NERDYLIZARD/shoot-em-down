package topdownshooting;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable, KeyListener {
//	FIELD 
	public static final int WIDTH = 560, HEIGHT = WIDTH;    // public = global access,  static = no copy,  final = const
	private Thread thread;
	private boolean running;
	
	private BufferedImage image;
	private Graphics2D g;
	
	private final int FPS = 30;
	private final long TARGETTIME = 1000 / FPS;
	private double averageFPS;
	
	private boolean gamePause;
	
	public static Player player;
	public static ArrayList<Bullet> bullets;
	public static ArrayList<Enemy> enemies;
	public static ArrayList<PowerUp> powerUps;
	public static ArrayList<Explosion> explosions;
	public static ArrayList<Text> texts;

	
	private int waveNumber;
	private boolean waveStart;
	private long waveStartTimer;
	private long waveStartTimerDiff;
	private int waveDelay = 2000;
	
	private long slowDownTimer;
	private long slowDownTimerDiff;
	private int slowDownLength = 6000;
	
	private boolean slowEnemy;
	
	private long timeStart;
	private long timeDiff;
	private long timeCap;
	
	private boolean timeUp;
	
	private long countDown;
	private long countDownDiff;
	private long countDownCap = 15 * 1000;
	
	
//	CONSTRUCTOR 	
	public GamePanel() {
		super();
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		requestFocus();
	}
	
	
	
//	Makes this Component displayable when
	//	 connecting it to a native screen resource	
	public void addNotify() {
		super.addNotify();
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
		
		addKeyListener(this);
	}
	

	//	INIT 
	public void init() {
		
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
		g.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		gamePause = false;
	
		
//		CLASS INSTANCE
		player = new Player();
		bullets = new ArrayList<Bullet>();
		enemies = new ArrayList<Enemy>();
		powerUps = new ArrayList<PowerUp>();
		explosions = new ArrayList<Explosion>();
		texts = new ArrayList<Text>();
		
		waveStart = false;
		waveNumber = 0;
		
		slowEnemy = false;
		
		timeUp = false;
		
		running = true;
	}
	
	
	@Override
	public void run() {
		init();
		
		long startTime;
		long URDTimeMillis;
		long waitTime;
		
		long totalTime = 0;		
		int frameCount = 0;
		
		while(running) {
			
			startTime = System.nanoTime();	
			if (!gamePause) {
				gameUpdate();
			}
			gameRender();
			gameDraw();
			URDTimeMillis = (System.nanoTime() - startTime) / 1000000;	
			
			waitTime = TARGETTIME - URDTimeMillis;    // TARGETTIME = 33.33;	
						
			try {
				Thread.sleep(waitTime);
			} catch(Exception e) { }
			
//			for display FPS
	//			total of loop including sleep;
			totalTime += System.nanoTime() - startTime;
			frameCount++;
			if(frameCount >= FPS) {
				averageFPS = 1000.0 / ((totalTime / frameCount) / 1000000);
				frameCount = 0;
				totalTime = 0;
			}	
		}
		
//		FINAL SCREEN
		g.setColor(new Color(0, 100, 255));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setColor(Color.WHITE);
		g.setFont(new Font("Century Gothic", Font.PLAIN, 16));
		String s = "G A M E  O V E R";
		int length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
		g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2);
		s = "Final Score: " + player.getScore();
		length = (int) g.getFontMetrics().getStringBounds(s, g).getWidth();
		g.drawString(s, (WIDTH - length) / 2, HEIGHT / 2 + 30);
		gameDraw();
		
	}
	
	private void gameUpdate() {

//		PLAYER UPDATE 
		player.update();
		
//		CREATE WAVE 
//		just a delay before upcomming stage;
		if (waveStartTimer == 0 && enemies.size() == 0) {
			waveNumber++;
			waveStart = false;
			waveStartTimer = System.nanoTime();
//			reset countdown;
			timeUp = false;
			
		} else {
			waveStartTimerDiff = (System.nanoTime() - waveStartTimer) / 1000000;
			if (waveStartTimerDiff > waveDelay) {
				waveStart = true;
				waveStartTimer = 0;
				waveStartTimerDiff = 0;
			}
		}

		
//		CREATE ENEMIES 
		if (waveStart && enemies.size() == 0) {
			createNewEnemies();
//			set time;
			timeStart = System.nanoTime();	
		}
		
//		ENEMY UPDATE 
		for (int i = 0; i < enemies.size(); i++) {
			enemies.get(i).update();
		}
		
//      TIME RESTRICTION 
// 		time Start when waveStart and Stop after no enemy
		if (timeStart != 0 && enemies.size() != 0) {
			timeDiff = (System.nanoTime() - timeStart) / 1000000;
			timeCap = waveNumber * 8000;
			if (timeDiff > timeCap ) {
				createTimeUpEnemies();
				timeStart = 0;
				timeUp = true;
//				there is countDown time left from previous loop,
	//				so, everytime that timeUp, such time has to be reset;
				countDown = 0;
			}				
		}

		if (timeUp) {
			if (countDown == 0) {
				countDown = System.nanoTime();
			} else {
				countDownDiff = (System.nanoTime() - countDown) / 1000000;
				if (countDownDiff > countDownCap) {
					createTimeUpEnemies();
					countDown = 0;
				}
			}
		}
		

//		SLOWDOWN 
		if (slowDownTimer != 0) {
			slowDownTimerDiff = (System.nanoTime() - slowDownTimer) / 1000000;
			if (slowDownTimerDiff > slowDownLength) {
				slowDownTimer = 0;
				slowEnemy = false;
				for (int j = 0; j < enemies.size(); j++) {
					enemies.get(j).setSlow(slowEnemy);
				}
			}
		}
		
		

		
//		BULLET UPDATE 
		for (int i = 0; i < bullets.size(); i++) {
			bullets.get(i).update();
//			remove when bullet is outbound
			if (bullets.get(i).isOutbound()) {
				bullets.remove(i);
//				i--, still wanna check the same index after 
	//				list has been removed, it will be replaced 
		//				by other;
				i--;
			}
		}
		

		
//		BULLET - ENEMY COLLISION 
		for (int i = 0; i < bullets.size(); i++) {
			Bullet b = bullets.get(i);
			double bx = b.getx();
			double by = b.gety();
			double br = b.getr();
			
			for (int j = 0; j < enemies.size(); j++) {
				Enemy e = enemies.get(j);
				double ex = e.getx();
				double ey = e.gety();
				int er = e.getr();
				
				double dx = bx - ex;
				double dy = by - ey;
				double dist = Math.sqrt(dx * dx + dy * dy);
//				dist < sum of radius == collision
				if (dist < br + er) {
					e.hit();
//					remove bullet
					bullets.remove(i);
					i--;
//					Check Dead Enemies 
					if (enemies.get(j).isDead()) {
//						Add Score
						player.addScore(e.getType() + e.getRank());
//						Release PowerUp
						// 3 - life++ (white)
						// 2 - power+2 (yellow)
						// 1 - power++ (orange)
						// 4 - slowDown
						double random = Math.random();
						if (random < 0.007) powerUps.add(new PowerUp(ex, ey, 3));
						else if (random < 0.002) powerUps.add(new PowerUp(ex, ey, 2));
						else if (random < 0.120) powerUps.add(new PowerUp(ex, ey, 1));
						else if (random < 0.130) powerUps.add(new PowerUp(ex, ey, 4));

//						else powerUps.add(new PowerUp(ex, ey, 1));
						
						enemies.remove(j);
						j--;
						
//						Explode 
						e.explode();
//						Explosion Effect
						explosions.add(new Explosion(ex, ey, er, er + 30));
						
					}
					break;
				}
			} // for j	
		} // for i

//		EXPLOSION UPDATE 
		for (int i = 0; i < explosions.size(); i++) {
			explosions.get(i).update();
			if (explosions.get(i).isExploded()) {
				explosions.remove(i);
				i--;
			}
		}

		
//		POWERUP OUTBOUND 	
		for (int i = 0; i < powerUps.size(); i++) {
			powerUps.get(i).update();
			if (powerUps.get(i).isOutbound()) {
				powerUps.remove(i);
				i--;
			}
		}
		
		
//		PLAYER - ENEMIES COLLISION 
		if (!player.isRecovering()) {
			double px = player.getx();
			double py = player.gety();
			double pr = player.getr();
			
			for (int i = 0; i < enemies.size(); i++) {
				Enemy e = enemies.get(i);
				double ex = e.getx();
				double ey = e.gety();
				double er = e.getr();
				double dx = px - ex;
				double dy = py - ey; 
				double dist = Math.sqrt(dx * dx + dy * dy);
				if (dist < er + pr) {
					player.loseLife();
				}
			} // for
		}  // if
		

//		PLAYER - POWERUP COLLISION 	
		double px = player.getx();
		double py = player.gety();
		double pr = player.getr();
		
		for (int i = 0; i < powerUps.size(); i++) {
			PowerUp pow = powerUps.get(i);
			double powX = pow.getX();
			double powY = pow.getY();
			double powR = pow.getR();
			double dx = px - powX;
			double dy = py - powY; 
			double dist = Math.sqrt(dx * dx + dy * dy);
			if (dist < powR + pr) {
				// 3 - life++ (white)
				// 2 - power+2 (yellow)
				// 1 - power++ (orange)
				// 4 - slowdown
				if (pow.getType() == 3) {
					player.gainLife();
					texts.add(new Text(px, py, 500, "life"));
				}
				if (pow.getType() == 2) {
					player.increasePower(2);
					texts.add(new Text(px, py, 500, "double power"));
				}
				if (pow.getType() == 1) {
					player.increasePower(1);					
					texts.add(new Text(px, py, 500, "power"));
				}
				if (pow.getType() == 4) {
					slowEnemy = true;
					slowDownTimer = System.nanoTime();
					for (int j = 0; j < enemies.size(); j++) {
						enemies.get(j).setSlow(slowEnemy);
					}
					texts.add(new Text(px, py, 500, "slow"));
				}
				
				powerUps.remove(i);
				i--;
			}
		} // for
		

//		PLAYER IS DEAD? 
		if(player.isDead()) {
			running = false;
		}
		
		
//		TEXT UPDATE 
		for (int i = 0; i < texts.size(); i++) {
			texts.get(i).update();
			if ( ! texts.get(i).isDisplaying()) {
				texts.remove(i);
				i--;
			}
		}
				
		
	}	//end of update
	
	
	

	/*****************************
	     		RENDER
	 *****************************/
	private void gameRender() {
//		draw component and combine in "image";
		//  	won't be shown on screen unless "image" is drawn;
		
//		Draw Background 
		g.setColor(new Color(0, 100, 255));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		
//		Draw Slowdown Screen 
		if(slowDownTimer != 0) {
			g.setColor(new Color(255, 255, 255, 64));
			g.fillRect(0, 0, WIDTH, HEIGHT);
		}
		

//		Draw Score, Time, Countdown 
		g.setColor(Color.WHITE);
		g.setFont(new Font("Century Gothic", Font.PLAIN, 14));
		g.drawString("SCORE: " + player.getScore(), WIDTH - 100, 30);
		//stop drawing after all enemies die 
		if (timeStart != 0 && enemies.size() != 0) {
			g.drawString("Time: "+ (timeCap - timeDiff) / 1000, WIDTH - 100, 45);
		}
		if (timeUp) {
			g.drawString("Count: " + (countDownCap - countDownDiff) / 1000, WIDTH - 100, 45);
		}
	

//		Draw Player Lives 
		for (int i = 0; i < player.getLives(); i++) {
			g.setColor(Color.WHITE);
			g.fillOval(20 + (20 * i), 20, player.getr() * 2, player.getr() * 2);
			g.setStroke(new BasicStroke(3));	
			g.setColor(Color.WHITE.darker());
			g.drawOval(20 + (20 * i), 20, player.getr() * 2, player.getr() * 2);
			g.setStroke(new BasicStroke(1));
		}
		
		
//		Draw SlowDown 
		if (slowDownTimer != 0) {
			g.setColor(Color.WHITE);
			g.drawRect(20, 65, 100, 8);
			g.fillRect(20, 65, 
					(int)(100 - 100.0 * slowDownTimerDiff / slowDownLength), 8);
		}
	
		
//		Draw WaveString 
		if (waveStartTimer != 0 && !gamePause) {
			g.setFont(new Font("Century Gothic", Font.PLAIN, 18));
			String waveString = "- W A V E  " + waveNumber + " -";
			int length = (int) g.getFontMetrics().getStringBounds(waveString, g).getWidth();
//			set transparency animation and bound within 255,
	//			when startTime = 0 & startTime = waveDalay  ->  alpha = 0,  (sin 0 and sin 3.14 = 0)
		//			when startTime is in the middle [sin(90)]  ->  alpha = 255,
			//			0 - 255 - 0;
			int alpha = (int)(255 * Math.sin(3.14 * waveStartTimerDiff / waveDelay));
			if (alpha > 255) alpha = 255;
//			alpha varies by timeDiff each loop
			g.setColor(new Color(255, 255, 255, alpha));
			g.drawString(waveString, WIDTH / 2 - length / 2, HEIGHT / 2);

			// draw shooting instruction in the beginning of the game
			if (waveNumber == 1) {
				drawShootingInstruction();
			}

		}
		
		
//		Draw Player 
		player.draw(g);
		
//		Draw Bullet 
		for (Bullet bullet : bullets) {
			bullet.draw(g);
		}		
//		Draw Enemies
		for (int i = 0; i < enemies.size(); i++) {
			enemies.get(i).draw(g);
		}
		
//		Draw Powerup 
		for (int i = 0; i < powerUps.size(); i++) {
			powerUps.get(i).draw(g);
		}	
		
		
//		Draw Explosion 
		for (int i = 0; i < explosions.size(); i++) {
			explosions.get(i).draw(g);
		}
		

//		Draw Player Power 
		g.setColor(Color.YELLOW);
		g.fillRect(20, 40, (player.getPower()) * 8, 8);
		g.setColor(Color.YELLOW.darker());
		g.setStroke(new BasicStroke(2));
//		getPowerRequired - 1 is for  
	//		graphic representation;
		for (int i = 0; i < player.getPowerRequired() - 1; i++) {
			g.drawRect(20 + 8 * i, 40, 8, 8);
		}
		g.setStroke(new BasicStroke(1));

		
//		Draw Text 
		for (int i = 0; i < texts.size(); i++) {
			texts.get(i).draw(g);
		}
		
//		Draw when gamePause
		if (gamePause) {
			g.setColor(Color.WHITE);
			String pauseString = "- P A U S E -";
			g.setFont(new Font("Century Gothic", Font.PLAIN, 14));
			int length = (int) g.getFontMetrics().getStringBounds(pauseString, g).getWidth();
			g.drawString(pauseString, WIDTH / 2 - length / 2, HEIGHT / 2);
			
			drawShootingInstruction();
		}
		
		
	}	// end of draw
	
	
	
	private void drawShootingInstruction() {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Century Gothic", Font.PLAIN, 18));
		String shootingInstruction = "Press \"Z\" to shoot";
		int length = (int) g.getFontMetrics().getStringBounds(shootingInstruction, g).getWidth();
		g.drawString(shootingInstruction, WIDTH / 2 - length / 2, HEIGHT / 2 + 30);
	}
	
	
	private void createNewEnemies() {
		enemies.clear();
		if(waveNumber == 1) {
			for(int i = 0; i < 4; i++) {
				enemies.add(new Enemy(1, 1, slowEnemy));
			}
		}
		if(waveNumber == 2) {
			for(int i = 0; i < 8; i++) {
				enemies.add(new Enemy(1, 1, slowEnemy));
			}
		}
		if(waveNumber == 3) {
			for(int i = 0; i < 4; i++) {
				enemies.add(new Enemy(1, 1, slowEnemy));
			}
			enemies.add(new Enemy(1, 2, slowEnemy));
			enemies.add(new Enemy(1, 2, slowEnemy));
			
		}
		if(waveNumber == 4) {
			enemies.add(new Enemy(1, 3, slowEnemy));
			enemies.add(new Enemy(1, 4, slowEnemy));
			for(int i = 0; i < 4; i++) {
				enemies.add(new Enemy(2, 1, slowEnemy));
			}
		}
		if(waveNumber == 5) {
			enemies.add(new Enemy(1, 4, slowEnemy));
			enemies.add(new Enemy(1, 3, slowEnemy));
			enemies.add(new Enemy(2, 3, slowEnemy));
		}
		if(waveNumber == 6) {
			enemies.add(new Enemy(1, 3, slowEnemy));
			for(int i = 0; i < 4; i++) {
				enemies.add(new Enemy(2, 1, slowEnemy));
				enemies.add(new Enemy(3, 1, slowEnemy));
			}
		}
		if(waveNumber == 7) {
			enemies.add(new Enemy(1, 3, slowEnemy));
			enemies.add(new Enemy(2, 3, slowEnemy));
			enemies.add(new Enemy(3, 3, slowEnemy));
		}
		if(waveNumber == 8) {
			enemies.add(new Enemy(1, 4, slowEnemy));
			enemies.add(new Enemy(2, 4, slowEnemy));
			enemies.add(new Enemy(3, 4, slowEnemy));
		}
		if(waveNumber == 9) {
			running = false;
		}	
	}
	
	
	private void createTimeUpEnemies() {
		if(waveNumber == 1) {
			enemies.add(new Enemy(1, 1, slowEnemy));
		}
		if(waveNumber == 2) {
			enemies.add(new Enemy(1, 1, slowEnemy));
		}
		if(waveNumber == 3) {
			enemies.add(new Enemy(1, 2, slowEnemy));						
		}
		if(waveNumber == 4) {
			enemies.add(new Enemy(1, 2, slowEnemy));
		}
		if(waveNumber == 5) {
			enemies.add(new Enemy(2, 1, slowEnemy));
		}
		if(waveNumber == 6) {
			enemies.add(new Enemy(2, 2, slowEnemy));
		}
		if(waveNumber == 7) {
			enemies.add(new Enemy(2, 2, slowEnemy));
			enemies.add(new Enemy(3, 1, slowEnemy));
		}
		if(waveNumber == 8) {
			enemies.add(new Enemy(2, 1, slowEnemy));
			enemies.add(new Enemy(3, 2, slowEnemy));
		}
	}
	
	
	
	private void gameDraw() {
//		draw image(whole screen);
		Graphics g2 = this.getGraphics();
//		"g2" is paint brush for this class;
		g2.drawImage(image, 0, 0, null);
		g2.dispose();
	}

	@Override
	public void keyTyped(KeyEvent key ) {}

	@Override
	public void keyPressed(KeyEvent key) {
		int keyCode = key.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT) {
			player.setLeft(true);
		}
		if (keyCode == KeyEvent.VK_RIGHT) {
			player.setRight(true);
		}
		if (keyCode == KeyEvent.VK_UP) {
			player.setUp(true);
		}
		if (keyCode == KeyEvent.VK_DOWN) {
			player.setDown(true);
		}
		if (keyCode == KeyEvent.VK_Z) {
			player.setFiring(true);
			// pressing Z also resumes the game
			if (gamePause) gamePause = false;
		}
		if (keyCode == KeyEvent.VK_SPACE) {
			gamePause = !gamePause;
		}
		if (keyCode == KeyEvent.VK_ESCAPE) {
			System.exit(0);
		}
	}

	@Override
	public void keyReleased(KeyEvent key) {
		int keyCode = key.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT) {
			player.setLeft(false);
		}
		if (keyCode == KeyEvent.VK_RIGHT) {
			player.setRight(false);
		}
		if (keyCode == KeyEvent.VK_UP) {
			player.setUp(false);
		}
		if (keyCode == KeyEvent.VK_DOWN) {
			player.setDown(false);
		}
		if (keyCode == KeyEvent.VK_Z) {
			player.setFiring(false);
		}
	}
	

}
