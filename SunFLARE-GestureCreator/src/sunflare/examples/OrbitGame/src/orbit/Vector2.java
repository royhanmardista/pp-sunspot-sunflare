/*
	This is the useful 2D vector struct. Everything's public
	to make things easy

*/

package sunflare.examples.OrbitGame.src.orbit;

public class Vector2 {

	public double x;
	public double y;

	public Vector2() {
		x = 0.0;
		y = 0.0;
	}

	public Vector2(double x,double y) {
		this.x = x;
		this.y = y;
	}

	public double getLength() {
		return Math.sqrt(x*x + y*y);
	}

	public Vector2 getNormalized() {
		double length = getLength();
		Vector2 v = new Vector2(x/length,y/length);
		return v;
	}

	public Vector2 addVector(Vector2 v) {
		Vector2 v2 = new Vector2(x + v.x,y+v.y);
		return v2;
	}

	public Vector2 subVector(Vector2 v) {
		return new Vector2(x - v.x,y - v.y);
	}

	public double dot(Vector2 v) {
		return x*v.x + y*v.y;
	}

	public Vector2 scale(double s) {
		return new Vector2(x*s,y*s);
	}
	
	public String toString()
	{
		return "<"+(int)x+", "+(int)y+">";
	}
}