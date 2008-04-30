package sunflare.examples.OrbitGame.src.orbit;

public class Rect {

	public double left;
	public double right;
	public double top;
	public double bottom;
	public double width;
	public double height;

	public Rect() {
		left = right = top = bottom = width = height = 0.0;
	}

	public Rect(double left,double top,double right,double bottom) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		this.width = right-left;
		this.height = bottom-top;
	}
	public void setCenter(Vector2 center)
	{
		double x=center.x,y=center.y;
		left=x-width/2;
		right=x+width/2;
		top=y-height/2;
		bottom=y+height/2;
	}
	public String toString()
	{
		return "<"+(int)left+", "+(int)top+", "+(int)right+", "+(int)bottom+">";
	}
}