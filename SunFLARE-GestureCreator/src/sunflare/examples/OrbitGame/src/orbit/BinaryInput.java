package sunflare.examples.OrbitGame.src.orbit;

/*
 * BinaryInput models a single button and whether it is on or off. The framework that this class provides encapsulates
 * the logic of handling an on/off button. When our software is industrialized, the input for that "button" can be hooked up to any hardware
 * input device -  whether its an eye blink detector, a foot pedal, or some other device for people with disabilities. 
 */
public class BinaryInput
{
	/*
	 * Whether the button is on or off.
	 */
	private boolean buttonOn;
	
	/*
	 * An integer that also stores the state of the button.
	 */
	private int buttonState;
	
	/*
	 * Handles double clicks (future feature).
	 */
	private long timeMark;
	
	public BinaryInput()
	{
		buttonOn=false;
		buttonState=0;
		timeMark=System.currentTimeMillis();
	}
	
	public void buttonChanged(boolean on)
	{
		long current=System.currentTimeMillis();
		
		//if button was off before and now its pressed
		if(buttonState==0&&on)
		{
			buttonOn=true;
			buttonState=1;
			timeMark=current;
		}
		//if button was on and now its released
		else if(buttonState==1&&!on)
		{
			buttonOn=false;
			buttonState=0;
			timeMark=current;
		}
	}
	public int getButtonState()
	{
		return buttonState;
	}
}