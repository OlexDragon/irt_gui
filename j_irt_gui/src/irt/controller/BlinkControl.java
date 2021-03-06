package irt.controller;

import irt.tools.label.LED;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class BlinkControl implements Runnable {

	private volatile boolean blink;
	private volatile boolean isRunning;
	private int blinkTime = 150;
	private LED led;
	private Timer timer;

	public BlinkControl(LED led) {
		this.led = led;
		timer = new Timer(blinkTime*2, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(blink){
					blink = false;
				}
			}
		});
		timer.setRepeats(false);

		Thread t = new Thread(this, "Blink Control");
		t.setPriority(t.getPriority()-1);
		t.start();
	}

	public boolean isBlink() {
		return blink;
	}

	public void blink() {

		timer.restart();	

		if(!blink){
			synchronized (this) {
				notify();
			}
			blink = true;
		}
	}

	public void setBlinkTime(int time) {
		blinkTime = time;
		timer.setDelay(blinkTime*2);
	}

	public int getBlinkTime() {
		return blinkTime;
	}

	@Override
	public void run() {
		isRunning = true;

		do {
			if (blink) {
				led.setOn(!led.isOn());
				synchronized (this) { try { wait(blinkTime); } catch (InterruptedException e) { }}
			} else if (led.isOn()) {
				led.setOn(false);
				synchronized (this) { try { wait(); } catch (InterruptedException e) { }}
			}
		} while (isRunning);
		blink = false;
		led.setOn(false);

		isRunning = false;
	}
}
