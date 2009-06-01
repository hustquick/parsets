package edu.uncc.parsets.util;

import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.uncc.parsets.ParallelSets;

/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\
 * Copyright (c) 2009, Robert Kosara, Caroline Ziemkiewicz,
 *                     and others (see Authors.txt for full list)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of UNC Charlotte nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *      
 * THIS SOFTWARE IS PROVIDED BY ITS AUTHORS ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
\* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

/**
 * Property class that provides the means for animating (i.e., interpolating)
 * the value from the old to the new. Largely modeled around the UIView
 * animation capabilities in Cocoa Touch/iPhone.
 * 
 * A property's value can be read using {@link #getValue()} and changed using
 * {@link #setValue(int)}.
 * 
 * Animations are initiated by calling
 * {@link #beginAnimations(float, int, ActionListener)}, making changes to
 * property values, and then calling {@link #commitAnimations()}. The values of
 * all AnimatableProperty objects will be part of that animation. An animation
 * can be canceled using {@link #stopAnimations()}.
 * 
 * The animation is performed so that the steps are on time and delays do not
 * accumulate. That means that steps will be skipped if they are more than 10ms
 * late. The application therefore cannot count on the number of steps performed
 * being the exact number that was specified.
 * 
 * Use of this class is thread-safe.
 * 
 * @author Robert Kosara
 * 
 */
public class AnimatableProperty {

	public enum SpeedProfile {
		linear, linearInSlowOut, slowInLinearOut, slowInSlowOut
	};

	// static variables

	public static int STEPS_PER_SECOND = 30;
	
	private static List<WeakReference<AnimatableProperty>> properties = new ArrayList<WeakReference<AnimatableProperty>>();

	private static boolean animationSetup = false;

	private static AnimationListener listener;

	private static Timer timer;

	private static float numSteps[];

	private static int period;

	private static Lock propertiesLock = new ReentrantLock();

	private static int currentSegment;
	
	/**
	 * The speed profile to use when interpolating the values.
	 */
	private static SpeedProfile speedProfile = SpeedProfile.slowInSlowOut;

	private static int numSegments;

	// instance variables

	private float currentValue;
	
	private float values[];
	
	private boolean interpolate = false;

	/**
	 * Create a property with an initial value of 0.
	 */
	public AnimatableProperty() {
		this(0);
	}

	/**
	 * Create a property with an initial value.
	 * 
	 * @param initialValue
	 */
	public AnimatableProperty(float initialValue) {
		currentValue = initialValue;
		propertiesLock.lock();
		try {
			properties.add(new WeakReference<AnimatableProperty>(this));
		} finally {
			propertiesLock.unlock();
		}
	}

	/**
	 * Remove this object from the list of objects to be updated when it is
	 * about to be destroyed by the garbage collector.
	 */
	protected void finalize() throws Throwable {
		propertiesLock.lock();
		try {
			WeakReference<AnimatableProperty> pref = null;
			for (WeakReference<AnimatableProperty> ref : properties)
				if (ref.get() == this)
					pref = ref;
			properties.remove(pref);
		} finally {
			propertiesLock.unlock();
		}
		super.finalize();
	}

	/**
	 * Begin setup for animations. All changes to all properties between this
	 * method and {@link #commitAnimations()} will be part of the animation.
	 * Ongoing animations will be stopped and will not continue unless the
	 * respective properties' values are changed.
	 * 
	 * @param duration
	 *            Duration of the animation in seconds
	 * @param segments
	 * 			  Number of segments the transition will have. A value over 1 is used for keyframe animation.
	 * @param animationListener
	 *            Listener to be called at every step of the animation (may be
	 *            null)
	 * 
	 * @see #setValue(int)
	 */
	public static void beginAnimations(float duration, int segments, SpeedProfile profile, AnimationListener animationListener) {
		stopAnimations();
		animationSetup = true;
		listener = animationListener;
		numSegments = segments;
		speedProfile = profile;
		currentSegment = 0;
		period = 1000/STEPS_PER_SECOND;
		numSteps = new float[segments];
		for (int i = 0; i < segments; i++)
			numSteps[i] = (int)(STEPS_PER_SECOND * duration);
	}

	/**
	 * Perform animations based on property changes between
	 * {@link #beginAnimations(float, int, ActionListener)} and calling this
	 * method. Animations start immediately.
	 */
	public static void commitAnimations() {
		animationSetup = false;
		currentSegment = 0;
		timer = new Timer();
		timer.scheduleAtFixedRate(new StepTask(period), 0, period);
	}

	/**
	 * Internal method, should not be called by other classes. Has to be protected (rather than private) so
	 * it can be called from {@link StepTask#run()}.
	 */
	protected static void takeStep(int stepNum) {
		if (stepNum > numSteps[currentSegment]) {
			currentSegment++;
			if (currentSegment == numSegments) {
				stopAnimations();
				return;
			} else
				stepNum = 0;
		}
		float interpolationFactor = pace(stepNum / numSteps[currentSegment]);
		propertiesLock.lock();
		try {
			for (WeakReference<AnimatableProperty> pref : properties) {
				AnimatableProperty p = pref.get();
				if (p != null && p.interpolate)
					p.step(interpolationFactor);
			}
		} finally {
			propertiesLock.unlock();
		}
		if (listener != null)
			listener.animationStep(currentSegment, interpolationFactor);
	}

	/**
	 * Stop the animation. If the animation is stopped before all steps have
	 * been performed, all properties snap to their designated final values.
	 */
	public static void stopAnimations() {
		if (timer != null) {
			propertiesLock.lock();
			try {
				timer.cancel();
				timer = null;
				for (WeakReference<AnimatableProperty> pref : properties) {
					AnimatableProperty p = pref.get();
					if (p != null) {
						p.finishAnimation();
						p.interpolate = false;
					}
				}
			} finally {
				propertiesLock.unlock();
			}
		}
	}

	/**
	 * Take a step in the animation. The parameter defines how far along the animation is.
	 * The only guarantees here are that the first call will be with a value of exactly 0.0f
	 * and the last call will be with 1.0f.
	 * 
	 * @param ifact interpolation factor
	 */
	private void step(float ifact) {
		currentValue = values[currentSegment] + (values[currentSegment+1] - values[currentSegment]) * ifact;
		if (ifact == 1.0f)
			values = null;
	}

	/** Set the value to the final value of the animation. This is to provide
	 * a reasonable semantics for the case where a new animation begins before
	 * the previous one ended. The value snaps to the new value, so it is in
	 * the expected state after the end of the animation.
	 * 
	 */
	private void finishAnimation() {
		if (values != null)
			currentValue = values[numSegments];
		values = null;
	}
	
	/**
	 * Get this property's current value.
	 * 
	 * @return value of this property
	 * @see #setValue(int)
	 */
	public float getValue() {
		return currentValue;
	}

	/**
	 * Set this property's new value. If called between
	 * {@link #beginAnimations(float, int, ActionListener)} and
	 * {@link #commitAnimations()}, the change will not be effective
	 * immediately, but the value will change once the animation starts. If
	 * called anywhere else, the value changes immediately.
	 * 
	 * @param newValue
	 *            this property's new value
	 * @see #getValue()
	 */
	public void setValue(float newValue) {
		if (animationSetup == false)
			currentValue = newValue;
		else {
			if (values == null) {
				values = new float[numSegments+1];
				values[0] = currentValue;
				for (int i = 1; i <= numSegments; i++)
					values[i] = Float.NaN;
			}
			float lastValue = values[0];
			for (int i = 1; i <= currentSegment; i++)
				if (Float.isNaN(values[i]))
					values[i] = lastValue;
				else
					lastValue = values[i];

			values[currentSegment+1] = newValue;
			interpolate = true;
		}
	}

	public static void nextSegment(float duration) {
		currentSegment++;
		numSteps[currentSegment] = (int)(STEPS_PER_SECOND * duration);
	}
	
	/**
	 * Pacing function providing slow-in, slow-out animation. Taken from
	 * prefuse.
	 */
	private static float pace(float f) {
		if (f == 0f)
			return 0f;
		else if (f >= 1f)
			return 1f;
		else {
			switch (speedProfile) {
			case linear:
				return f;

			case slowInLinearOut:
				if (f < 0.5f)
					return sigmoid(f);
				else
					return f;

			case linearInSlowOut:
				if (f > 0.5f)
					return sigmoid(f);
				else
					return f;

			case slowInSlowOut:
				return sigmoid(f);
			}
		}
		return 0; // just to make Eclipse happy
	}

	/**
	 * Computes a normalized sigmoid
	 * 
	 * @param x
	 *            input value in the interval [0,1]
	 */
	private static float sigmoid(float x) {
		x = 12f * x - 6f;
		return (1f / (1f + (float) Math.exp(-x)));
	}

	public static int getNumSteps() {
		return (int)numSteps[currentSegment];
	}
}

/**
 * Auxiliary class that just passes events from the timer to static methods.
 * 
 */
class StepTask extends TimerTask {

	private static final int MAX_TARDINESS = 10;

	private int stepNum = 0;

	private long startTime;

	private long periodLength;

	public StepTask(int period) {
		startTime = System.currentTimeMillis();
		periodLength = period;
	}

	@Override
	public void run() {
		long tardiness = System.currentTimeMillis() - scheduledExecutionTime();
		if ((tardiness < MAX_TARDINESS) || (stepNum == AnimatableProperty.getNumSteps()-1)) {
			AnimatableProperty.takeStep(stepNum);
		} else
			ParallelSets.logger.info("Skipping animation step "+stepNum+", tardiness = "+tardiness);
		stepNum++;
	}

	@Override
	public long scheduledExecutionTime() {
		return startTime + (long) stepNum * periodLength;
	}
}
