package sunflare.examples.OrbitGame.src.orbit;

import java.util.*;

public class ParticleSystem
{
	private ArrayList<ParticleEffect> particles;
	
	public ParticleSystem()
	{
		particles=new ArrayList<ParticleEffect>();
	}
	public void update(int millis)
	{
		for(int i=0;i<particles.size();i++)
		{
			ParticleEffect part=particles.get(i);
			if(part.isAlive())
				part.update(millis);
			else
			{
				particles.remove(i);
				i--;
			}
		}
	}
	public ArrayList<ParticleEffect> getParticles()
	{
		return particles;
	}
	public void addParticle(Vector2 pos,Vector2 vel,String sprite,double diameter)
	{
		particles.add(new ParticleEffect(pos,vel,new Vector2(),sprite,diameter,diameter));
	}
	
}
class ParticleEffect extends SpaceObject
{
	public ParticleEffect(Vector2 p,Vector2 v,Vector2 a,String sprite,double width,double height)
	{
		super(p,v,a,sprite,width,height);
		setAnimationProperties(90,6,true);
	}
	/** Updates the animation
	 *
	 **/
	public void update(int millis)
	{
		//pos=pos.addVector(vel.scale(millis*0.001));
		animate(millis);
	}
	public boolean isAlive()
	{
		return currentFrame!=numFrames-1;
	}
}