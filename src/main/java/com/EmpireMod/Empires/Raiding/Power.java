package com.EmpireMod.Empires.Raiding;

public class Power {

Timer timer = new Timer ();
TimerTask hourlyTask = new TimerTask () {
    @Override
    public void run () {
        // your code here...
    }
};

// schedule the task to run starting now and then every hour...
timer.schedule (hourlyTask, 0l, 1000*60*60);

}

