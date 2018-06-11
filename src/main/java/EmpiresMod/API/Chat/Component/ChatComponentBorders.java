package EmpiresMod.API.Chat.Component;

public class ChatComponentBorders {
//Basically take "FUCK" and factor it into a border =======FUCK======= 
//	with the even amount of equal signs on each side
	//Very crude design, but its 3 AM and the OCD OF INCOMPLETE FUCKING BORDERS IS HURTING ME.
	
	public static String borderEditor(String string) {
					    //0123456789012345678901234567
		String border1 = "==========================[";
		String border2 = "]=========================";
		int border1chars = border1.length();
		int border2chars = border2.length();
		int stringchars = string.length()/2;
		//lets say the word is Empire, it has 6 characters.
		//I want it to take 3 characters from border1
		//			   take 3 characters from border2
		
		int b1diff = (border1chars - stringchars);
		int b2diff = (border2chars - stringchars);
		String leftborder = border1.substring(stringchars, 27); //cut from 3, 27
		String rightborder = border2.substring(0, b2diff); //cut from 0 to 24
		
		String finalstring = (leftborder + string + rightborder); //rejoin all 3 strings.
		return finalstring;
	}
	
}
