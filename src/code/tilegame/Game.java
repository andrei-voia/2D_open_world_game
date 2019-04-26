package code.tilegame;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import code.tilegame.display.Display;
import code.tilegame.gfx.Assets;
import code.tilegame.gfx.GameCamera;
import code.tilegame.input.KeyManager;
import code.tilegame.input.MouseManager;
import code.tilegame.states.GameState;
import code.tilegame.states.MenuState;
import code.tilegame.states.State;

public class Game implements Runnable {

	private Display display;
	private String title;
	private int width;
	private int height;
	private Thread thread;
	private boolean running=false;
	
	private BufferStrategy bs;
	private Graphics g;
	
	private GameState gameState;
	private MenuState menuState;
	
	//INPUT
	private KeyManager keyManager;
	private MouseManager mouseManager;
	
	//Camera
	private GameCamera gameCamera;
	
	//Handler
	private Handler handler;

	public Game (String t, int w, int h)
	{
		title=t;
		width=w;
		height=h;
		keyManager= new KeyManager();
		mouseManager= new MouseManager();
		
		//display=new Display(title, width, height);			
	}
	
	private void initialization()
	{
		display=new Display(title, width, height);
		display.getFrame().addKeyListener(keyManager);
		
		//mouse
		display.getFrame().addMouseListener(mouseManager);
		display.getFrame().addMouseMotionListener(mouseManager);
		display.getCanvas().addMouseListener(mouseManager);
		display.getCanvas().addMouseMotionListener(mouseManager);
		
		Assets.init();
		
		
		handler=new Handler(this);
		gameCamera=new GameCamera(handler, 0,0);
		
		//StartState
		gameState=new GameState(handler);
		menuState=new MenuState(handler);
		State.setState(menuState);
	}
	
	private void tick()
	{
		keyManager.tick(); //very important
		if(State.getState()!=null) State.getState().tick();
	}
	
	private void render()
	{
		bs=display.getCanvas().getBufferStrategy();
		
		if(bs==null)
		{
			display.getCanvas().createBufferStrategy(3);
			return;
		}
		
		//drawing here
		g=bs.getDrawGraphics();
		g.clearRect(0, 0, width, height);
		
		if(State.getState()!=null) State.getState().render(g);
		
		
		bs.show();
		g.dispose();		
	}
	
	public void run()
	{
		initialization();
		
		//FPS initialization
		int fps=60;
		double timePerTick=1000000000 / fps;
		double delta=0;
		long now;
		long lastTime=System.nanoTime();
		
		//FPS counter
		long timer=0;
		int ticks=0;
		
		
		while(running)
		{
			now=System.nanoTime();
			delta+=(now-lastTime)/timePerTick;		
			timer=now-lastTime;
			lastTime=now;
			
			if(delta>=1)
			{
				delta--;
				ticks++;
				tick();
				render();
			}
			
			if(timer>=1000000000)
			{
				System.out.println("Ticks and frames: "+ticks);
				ticks=0;
				timer=0;
				
			}
		}
		
		stop();
	}
	
	public KeyManager getKeyManager()
	{
		return keyManager;
	}
	
	public MouseManager getMouseManager()
	{
		return mouseManager;
	}
	
	public GameCamera getGameCamera()
	{
		return gameCamera;
	}
	
	
	//return gameState
	public GameState getGameState()
	{
		return gameState;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public synchronized void start()
	{
		if(running)return;
		
		running=true;
		thread=new Thread(this);
		thread.start();
	}
	
	public synchronized void stop()
	{
		if(!running)return;
		
		running=false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}