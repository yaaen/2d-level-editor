package com.deaddropgames.editor.events;

import java.util.EventObject;

@SuppressWarnings("serial")
public class ElementChangedEvent extends EventObject {
	
	public ElementChangedEvent(Object source) {
		
		super(source);
	}
}