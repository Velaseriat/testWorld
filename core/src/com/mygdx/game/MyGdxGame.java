package com.mygdx.game;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

//Let me just say this. FUCK vector math. It's hard. It's a Walter thing.

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {
	    TiledMap tiledMap; //our map, on which things will exist
	    PerspectiveCamera camera; //the camera
	    TiledMapRenderer tiledMapRenderer; //the thing that renders our map
		private Vector3 target, tileDragOriginLoc; //target isn't used right now, tileDragTarget is
		boolean wasDragged = false;

	    
	    @Override
	    public void create () {
	    	camera = new PerspectiveCamera(65, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); //persepctive camera with a fieldOfView of 65 degrees I think
	        camera.update(); //update it
	        camera.position.set(0f, -100f, 150f); //sets it -100 units back, and 150 units above the bottom-left corner of the world
	        camera.lookAt(0f, 0f, 0f); //from the camera's location, look at the bottom-left corner of the world
	        camera.near = 0.1f;  //render things between .1f to 4000f stuff away
	        camera.far = 4000.0f;
	        
	        target = camera.position.cpy();
	        
	        tileDragOriginLoc = camera.position.cpy(); //set tileDragOriginLoc location
	        
	        tiledMap = new TmxMapLoader().load("level.tmx"); //loads the level.tmx file. Needs level.jpg and level.tmx to work, kinda like a sprite sheet
	        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap); //funny considering we aren't using an orthogonal camera
	        Gdx.input.setInputProcessor(this);
	    }

	    @Override
	    public void render () {
	        Gdx.gl.glClearColor(0, 0, 0, 1);
	        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	        camera.update();
	        tiledMapRenderer.setView(camera.combined, 0f, 0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	        tiledMapRenderer.render();
	        
	        //depending on what we do, we may use these but for dragging the map around, we don't need it.
	        
	       camera.position.lerp(target, 4f*Gdx.graphics.getDeltaTime());
	        //camera.position.set(target);
	        
	    }

	    @Override
	    public boolean keyDown(int keycode) {
	        return false;
	    }

	    @Override
	    public boolean keyUp(int keycode) {
	        if(keycode == Input.Keys.LEFT)
	        	camera.position.add(-32f, 0f, 0f);
	        if(keycode == Input.Keys.RIGHT)
	        	camera.position.add(32f, 0f, 0f);
	        if(keycode == Input.Keys.UP)
	        	camera.position.add(0f, 32f, 0f);
	        if(keycode == Input.Keys.DOWN)
	        	camera.position.add(0f, -32f, 0f);
	        System.out.println("Camera: " + camera.position); //cam pos
	        return false;
	    }

	    @Override
	    public boolean keyTyped(char character) {

	        return false;
	    }

	    @Override
	    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
	    	//On touchdown, it picks a tileDragTarget as a starting reference point.
	    	
	    	wasDragged = false; //set wasDragged to false because we didn't start dragging across the screen yet
	    	//The next three lines converts screenCoords to worldMap coords
	    	Ray ray = camera.getPickRay(screenX, screenY); //origin and direction
			float scale = -camera.position.z/ray.direction.z; //scale it
	    	tileDragOriginLoc = ray.direction.scl(scale).cpy().add(camera.position); //make it so it touches the worldMap, and add the original camera position since it was pushed x:0 y:-100 z:250
	    	
	    	System.out.println("Touching down" + tileDragOriginLoc); //starting dragTileTarget print
	    	if (((TiledMapTileLayer)tiledMap.getLayers().get(0)).getCell((int)tileDragOriginLoc.x/32, (int) tileDragOriginLoc.y/32).getTile().getProperties().get("blocked") != null)
	    		System.out.println("BLOCKED TILE"); //just says that it's a blocked tile if it is.... BLOCKED TILES ARE THE MOSTLY PICK ONE WITH A HINT OF YELLOW AT THE BOTTOM RIGHT CORNER

	        return false;
	    }

	    @Override
	    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
	    	//This is to lerp to targets on the map
	    	
	    	if (!wasDragged){ //if dragging didn't happen then don't do this.
	    	Ray ray = camera.getPickRay(screenX, screenY);
	    	float scale = -camera.position.z/ray.direction.z;
	    	Vector3 temp = ray.direction.scl(scale);
	    	temp.add(camera.position);
	    	temp.z = camera.position.z;
	    	temp.y = temp.y - 100f;
	    	target = temp.cpy();
	    	}
	        return false;
	    }

		@Override
	    public boolean touchDragged(int screenX, int screenY, int pointer) {
			wasDragged = true; //because, well... yeah.
			
			//Same deal as touchuplibgdx inpu
			Ray ray = camera.getPickRay(screenX, screenY);
			float scale = -camera.position.z/ray.direction.z;
	    	Vector3 temp3 = ray.direction.scl(scale);
	    	temp3.add(camera.position);
	    	
	    	
	    	Vector3 diff = new Vector3(-tileDragOriginLoc.x + temp3.x, -tileDragOriginLoc.y + temp3.y, 0f);// Get the difference between tileDragTarget and this new location, z is zero cuz we don't want the camera height  to change
	    	// NewLoc MINUS tileDragTarget location 
	    	target.set(camera.position.cpy().sub(diff)); //set the camera's position to the opposite direction of diff, I actually don't understand my own shit
			System.out.println(diff);//shows distance from tileDragTarget (the initial touchDown location)
	        return true;
	    }

	    @Override
	    public boolean mouseMoved(int screenX, int screenY) {
	        return false;
	    }

	    @Override
	    public boolean scrolled(int amount) {
	    	/*Vector3 lookat = camera.position;
	    	camera.position.set(camera.position.cpy().add(0f, 0f, amount*20f));
	    	camera.lookAt(lookat);*/
	    	
	    	Vector3 lookat = camera.position; //whatever it was looking at earlier, save it
	    	target.set(camera.position.cpy().add(0f, 0f, amount*55f)); //change height of the camera
	    	camera.lookAt(lookat); //make it look at the same shit
	        return false;
	    }

}
