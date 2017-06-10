package com.EmpireMod.Empires.Raiding;

import java.util.*;

public class Power {
	private int power = 0;
	private int maxpower = 20;
	private void StartTimer(){
		Timer timer = new Timer ();
		TimerTask hourlyTask = new TimerTask () {
		    @Override
		    public void run () {
		        if(power <= maxpower){
		        	power++;
		        }
		    }
		};
		timer.schedule (hourlyTask, 0l, 1000*60*60);
	};
};

